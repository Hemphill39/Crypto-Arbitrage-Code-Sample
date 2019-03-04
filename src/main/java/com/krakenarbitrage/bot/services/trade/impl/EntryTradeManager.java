package com.krakenarbitrage.bot.services.trade.impl;

import com.krakenarbitrage.bot.domains.indicator.Indicator;
import com.krakenarbitrage.bot.domains.trade.LocalOrder;
import com.krakenarbitrage.bot.domains.trade.Trade;
import com.krakenarbitrage.bot.exchanges.KrakenSingleton;
import com.krakenarbitrage.bot.repositories.TradeRepository;
import com.krakenarbitrage.bot.services.currency.CurrencyService;
import com.krakenarbitrage.bot.services.currency.impl.CurrencyServiceImpl;
import com.krakenarbitrage.bot.services.indicator.DltService;
import com.krakenarbitrage.bot.services.indicator.IndicatorService;
import com.krakenarbitrage.bot.services.trade.ActiveTradeManager;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class EntryTradeManager {

    private final Logger LOGGER = LoggerFactory.getLogger(EntryTradeManager.class);
    private final TradeRepository tradeRepository;
    private final TradeService tradeService;
    private final MarketDataService marketDataService;
    private final ActiveTradeManager activeTradeManager;
    private final ExitTradeManager exitTradeManager;
    private final CurrencyServiceImpl currencyService;
    private final IndicatorService indicatorService;
    private final DltService dltService;
    private final BigDecimal spread;

    public EntryTradeManager(
            TradeRepository tradeRepository,
            ActiveTradeManager activeTradeManager,
            ExitTradeManager exitTradeManager,
            CurrencyServiceImpl currencyService,
            IndicatorService indicatorService,
            DltService dltService,
            KrakenSingleton kraken,
            @Value("${spread}") BigDecimal spread) {
        this.tradeRepository = tradeRepository;
        this.activeTradeManager = activeTradeManager;
        this.exitTradeManager = exitTradeManager;
        this.currencyService = currencyService;
        this.indicatorService = indicatorService;
        this.tradeService = kraken.getTradeService();
        this.dltService = dltService;
        this.marketDataService = kraken.getMarketDataService();
        this.spread = spread;
    }

    public CompletableFuture<Void> manageTrades() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        return CompletableFuture.runAsync(() -> {
            for (Trade trade : activeTradeManager.getEntryTrades()) {
                futures.add(manageTrade(trade));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
        });
    }

    private CompletableFuture<Void> manageTrade(Trade trade) {
        return CompletableFuture.runAsync(() -> {

            LocalOrder order = trade.getEntryOrders().get(trade.getEntryOrders().size() - 1);

            if (order.getStatus().equals(Order.OrderStatus.FILLED)) {
                handleFilledOrder(trade, order);
            } else if (order.getStatus().equals(Order.OrderStatus.CANCELED)) {
                if (order.getAmountFilled().compareTo(BigDecimal.ZERO) > 0) {
                    handlePartiallyFilledOrder(trade, order);
                } else {
                    activeTradeManager.removeActiveTrade(trade);
                    tradeRepository.delete(trade);
                }
            } else {
                handleUntouchedOrder(trade, order);
            }
            tradeRepository.save(trade);
        });
    }

    /**
     * update some stats and send off the trade
     * @param trade
     * @param order
     */
    private void handleFilledOrder(Trade trade, LocalOrder order) {
        LOGGER.info("entry order filled for pair " + order.getPair() + " trade " + trade.getId());
        trade.setCryptoPurchased(trade.getCryptoPurchased().add(order.getAmountFilled()));
        trade.setEntryComplete(true);
        trade.setEntryCostBtc(getEntryCost(trade));
        exitTradeManager.initializeExitTrade(trade);
    }

    /**
     * Decide if we should buy more crypto or if we should just sell what we have
     * @param trade
     * @param order
     */
    private void handlePartiallyFilledOrder(Trade trade, LocalOrder order) {
        LOGGER.info("entry order partially filled for pair " + order.getPair());
        trade.setCryptoPurchased(trade.getCryptoPurchased().add(order.getAmountFilled()));
        BigDecimal minLotSize = CurrencyService.getMinimumLotSize(trade.getIndicator().getCrypto());
        if (trade.getCryptoPurchased().compareTo(minLotSize) > 0) {
            trade.setEntryComplete(true);
            trade.setEntryCostBtc(getEntryCost(trade));
            exitTradeManager.initializeExitTrade(trade);
        } else {
            LOGGER.info("not enough crypto sell. buying more " + order.getPair());
            CurrencyPair pair = CurrencyService.getCurrencyPairFromString(trade.getIndicator().getEntryPair());
            MarketOrder marketOrder = new MarketOrder(
                    Order.OrderType.BID,
                    minLotSize,
                    pair
            );
            try {
                String orderId = tradeService.placeMarketOrder(marketOrder);
                LocalOrder newOrder = new LocalOrder();
                newOrder.setId(orderId);
                newOrder.setPair(order.getPair());
                newOrder.setOriginalAmount(minLotSize);
                trade.getEntryOrders().add(newOrder);
            } catch (Exception ex) {
                LOGGER.error("could not place market entry order");
            }
        }
    }

    private void handleUntouchedOrder(Trade trade, LocalOrder order) {
        if (isTradeExpired(trade)) {
            LOGGER.info("canceling expired trade " + trade.getId());
            try {
                tradeService.cancelOrder(order.getId());
            } catch (Exception ex) {
                LOGGER.error("could not cancel order " + order.getId(), ex);
            }
        }
    }

    /**
     * check if the opportunity is gone or if the exit price drops at all
     * @param trade
     */
    private Boolean isTradeExpired(Trade trade) {
        CurrencyPair entryPair = CurrencyService.getCurrencyPairFromString(trade.getIndicator().getEntryPair());
        CurrencyPair exitPair = CurrencyService.getCurrencyPairFromString(trade.getIndicator().getExitPair());
        Indicator indicator = indicatorService.getIndicator(entryPair, exitPair);
        BigDecimal spread = this.dltService.getCenter().add(this.spread);
        if (indicator == null) {
            LOGGER.info("null indicator");
            return true;
        }


        if (!indicator.isFloatingOpp() && !indicator.isStandardOpp(spread)) {
            return true;
        }

        try {
            OrderBook orderBook = marketDataService.getOrderBook(exitPair);
            return trade.getIndicator().getExitPrice().compareTo(orderBook.getBids().get(0).getLimitPrice()) > 0;
        } catch (Exception ex) {
            LOGGER.error("could not get order book", ex);
            return true;
        }
    }

    private BigDecimal getEntryCost(Trade trade) {
        BigDecimal totalCost = BigDecimal.ZERO, totalPurchased = BigDecimal.ZERO;
        for (LocalOrder order : trade.getEntryOrders()) {
            totalCost = totalCost.add(order.getAmountFilled().multiply(order.getAveragePrice()));
            totalPurchased = totalPurchased.add(order.getAmountFilled());
        }

        trade.setCryptoPurchased(totalPurchased);
        CurrencyPair entryPair = CurrencyService.getCurrencyPairFromString(trade.getIndicator().getEntryPair());
        BigDecimal rate = currencyService.getExchangeRate(new CurrencyPair(Currency.BTC, entryPair.counter));
        return totalCost.divide(rate, 8, BigDecimal.ROUND_HALF_DOWN);
    }
}
