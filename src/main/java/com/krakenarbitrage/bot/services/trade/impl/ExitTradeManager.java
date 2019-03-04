package com.krakenarbitrage.bot.services.trade.impl;

import com.krakenarbitrage.bot.domains.trade.LocalOrder;
import com.krakenarbitrage.bot.domains.trade.Trade;
import com.krakenarbitrage.bot.exchanges.KrakenSingleton;
import com.krakenarbitrage.bot.repositories.TradeRepository;
import com.krakenarbitrage.bot.services.currency.CurrencyService;
import com.krakenarbitrage.bot.services.currency.impl.CurrencyServiceImpl;
import com.krakenarbitrage.bot.services.trade.ActiveTradeManager;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ExitTradeManager {

    private final Logger LOGGER = LoggerFactory.getLogger(ExitTradeManager.class);
    private final TradeService tradeService;
    private final TradeRepository tradeRepository;
    private final CurrencyServiceImpl currencyService;
    private final ActiveTradeManager activeTradeManager;
    private final Long timeout;
    private final BigDecimal tickBufferFactor;

    public ExitTradeManager(
            TradeRepository tradeRepository,
            CurrencyServiceImpl currencyService,
            ActiveTradeManager activeTradeManager,
            KrakenSingleton kraken,
            @Value("${exit-order-timeout}") Long timeout,
            @Value("${exit-order-tick-buffer}") BigDecimal tickBufferFactor) {
        this.tradeRepository = tradeRepository;
        this.currencyService = currencyService;
        this.activeTradeManager = activeTradeManager;
        this.timeout = timeout;
        this.tickBufferFactor = tickBufferFactor;
        this.tradeService = kraken.getTradeService();
    }

    void initializeExitTrade(Trade trade) {
        LOGGER.info("initializing exit trade " + trade.getIndicator().getEntryPair() + " -> " +  trade.getIndicator().getExitPair() + " trade " + trade.getId());
        BigDecimal amt = trade.getCryptoPurchased();
        CurrencyPair exitPair = CurrencyService.getCurrencyPairFromString(trade.getIndicator().getExitPair());
        Integer tickScale = CurrencyService.getTick(exitPair).intValue();
        BigDecimal tick = tickBufferFactor.multiply(BigDecimal.ONE.divide(BigDecimal.TEN.pow(tickScale)));
        BigDecimal price = trade.getIndicator().getExitPrice().subtract(tick);

        LimitOrder limitOrder = new LimitOrder(
                Order.OrderType.ASK,
                amt,
                exitPair,
                null,
                null,
                price
        );
        placeTrade(trade, limitOrder);
    }

    CompletableFuture<Void> manageTrades() {
        return CompletableFuture.runAsync(() -> {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Trade trade : activeTradeManager.getExitTrades()) {
                futures.add(manageTrade(trade));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
        });
    }

    private CompletableFuture<Void> manageTrade(Trade trade) {
        return CompletableFuture.runAsync(() -> {
           if (trade.getExitOrders().size() == 0) {
               initializeExitTrade(trade);
           } else {
               LocalOrder order = trade.getExitOrders().get(trade.getExitOrders().size() - 1);
               if (order.getStatus().equals(Order.OrderStatus.FILLED)) {
                    handleFilledOrder(trade, order);
               } else if (order.getStatus().equals(Order.OrderStatus.CANCELED)) {
                    handleCanceledOrder(trade, order);
               } else if (order.getTimestamp().getTime() < new Date().getTime() - timeout) {
                    handleExpiredOrder(trade, order);
               }
           }
           tradeRepository.save(trade);
        });
    }

    private void placeTrade(Trade trade, LimitOrder order) {
        try {
            String orderId = tradeService.placeLimitOrder(order);
            LocalOrder localOrder = new LocalOrder();
            localOrder.setId(orderId);
            localOrder.setOriginalAmount(order.getOriginalAmount());
            localOrder.setOriginalPrice(order.getLimitPrice());
            localOrder.setStatus(Order.OrderStatus.NEW);
            localOrder.setPair(CurrencyService.getStringFromCurrencyPair(order.getCurrencyPair()));
            trade.getExitOrders().add(localOrder);
        } catch (Exception ex) {
            LOGGER.error("could not place exit trade for trade " + trade.getId(), ex);
        }
    }

    private void handleFilledOrder(Trade trade, LocalOrder order) {
        LOGGER.info("exit order filled " + trade.getId());
        trade.setExitComplete(true);
        trade.setActive(false);
        trade.setCryptoSold(trade.getCryptoSold().add(order.getAmountFilled()));
        trade.setExitCostBtc(getExitCost(trade));
        trade.setPnlBtc(trade.getExitCostBtc().subtract(trade.getEntryCostBtc()));
        trade.setFillQuality(trade.getExitCostBtc().divide(trade.getEntryCostBtc(), 8, BigDecimal.ROUND_HALF_DOWN).subtract(BigDecimal.ONE));
        activeTradeManager.removeActiveTrade(trade);
    }

    private void handleExpiredOrder(Trade trade, LocalOrder order) {
        try {
            LOGGER.info("canceling exit order " + order.getId() + " trade " + trade.getId());
            tradeService.cancelOrder(order.getId());
        } catch (Exception ex) {
            LOGGER.error("could not cancel order " + order.getId());
        }
    }

    private void handleCanceledOrder(Trade trade, LocalOrder order) {
        LOGGER.info("handling canceled order " + order.getId() + " trade " + trade.getId());
        trade.setCryptoSold(trade.getCryptoSold().add(order.getAmountFilled()));
        BigDecimal minLotSize = CurrencyService.getMinimumLotSize(trade.getIndicator().getCrypto());
        CurrencyPair exitPair = CurrencyService.getCurrencyPairFromString(trade.getIndicator().getExitPair());
        if (trade.getCryptoPurchased().subtract(trade.getCryptoSold()).compareTo(minLotSize) > 0) {
            // market sell crypto
            BigDecimal amt = trade.getCryptoPurchased().subtract(trade.getCryptoSold());
            MarketOrder marketOrder = new MarketOrder(
                    Order.OrderType.ASK,
                    amt,
                    exitPair
            );
            try {
                String orderId = tradeService.placeMarketOrder(marketOrder);
                LocalOrder localOrder = new LocalOrder();
                localOrder.setStatus(Order.OrderStatus.NEW);
                localOrder.setOriginalAmount(amt);
                localOrder.setPair(order.getPair());
                localOrder.setId(orderId);
                trade.getExitOrders().add(localOrder);
            } catch (Exception ex) {
                LOGGER.error("could not place market order to sell crypto", ex);
            }
        } else {
            // close this trade out
            LOGGER.error("not enough crypto to sell. canceling trade");
            trade.setActive(false);
            activeTradeManager.removeActiveTrade(trade);
        }
    }

    private BigDecimal getExitCost(Trade trade) {
        BigDecimal totalCost = BigDecimal.ZERO;
        for (LocalOrder order : trade.getExitOrders()) {
            totalCost = totalCost.add(order.getAmountFilled().multiply(order.getAveragePrice()));
        }

        CurrencyPair exitPair = CurrencyService.getCurrencyPairFromString(trade.getIndicator().getExitPair());
        BigDecimal rate = currencyService.getExchangeRate(new CurrencyPair(Currency.BTC, exitPair.counter));
        return totalCost.divide(rate, 8, BigDecimal.ROUND_HALF_DOWN);
    }
}
