package com.krakenarbitrage.bot.services.trade.impl;

import com.krakenarbitrage.bot.domains.indicator.Indicator;
import com.krakenarbitrage.bot.domains.trade.LocalOrder;
import com.krakenarbitrage.bot.domains.trade.Trade;
import com.krakenarbitrage.bot.exchanges.KrakenSingleton;
import com.krakenarbitrage.bot.repositories.TradeRepository;
import com.krakenarbitrage.bot.services.account.BalanceService;
import com.krakenarbitrage.bot.services.currency.CurrencyService;
import com.krakenarbitrage.bot.services.currency.impl.CurrencyServiceImpl;
import com.krakenarbitrage.bot.services.indicator.DltService;
import com.krakenarbitrage.bot.services.indicator.IndicatorListener;
import com.krakenarbitrage.bot.services.indicator.IndicatorService;
import com.krakenarbitrage.bot.services.trade.ActiveTradeManager;
import com.krakenarbitrage.bot.services.trade.TradeFinder;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.kraken.dto.trade.KrakenOrderFlags;
import org.knowm.xchange.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TradeFinderImpl implements TradeFinder, IndicatorListener {

    private final Logger LOGGER = LoggerFactory.getLogger(TradeFinderImpl.class);
    private final TradeService tradeService;
    private final BalanceService balanceService;
    private final CurrencyServiceImpl currencyService;
    private final ActiveTradeManager activeTradeManager;
    private final TradeRepository tradeRepository;
    private final DltService dltService;

    private final BigDecimal minUsdAccountBalance;
    private final BigDecimal xbtLotSize;

    private final BigDecimal spread;

    private final Integer maxActiveTrades;
    private final Map<Currency, BigDecimal> minBalances;

    public TradeFinderImpl(
            ActiveTradeManager activeTradeManager,
            BalanceService balanceService,
            CurrencyServiceImpl currencyService,
            TradeRepository tradeRepository,
            KrakenSingleton kraken,
            IndicatorService indicatorService,
            DltService dltService,
            @Value("${min-usd-account-balance}") BigDecimal minimumAccountBalance,
            @Value("${xbt-lot-size}") BigDecimal xbtLotSize,
            @Value("${spread}") BigDecimal spread,
            @Value("${max-active-trades}") Integer maxActiveTrades,
            @Value("${min-usd-balance}") BigDecimal minUsdBalance,
            @Value("${min-eur-balance}") BigDecimal minEurBalance) {
        this.activeTradeManager = activeTradeManager;
        this.balanceService = balanceService;
        this.minUsdAccountBalance = minimumAccountBalance;
        this.xbtLotSize = xbtLotSize;
        this.spread = spread;
        this.maxActiveTrades = maxActiveTrades;
        this.currencyService = currencyService;
        this.dltService = dltService;
        this.tradeRepository = tradeRepository;

        this.tradeService = kraken.getTradeService();

        minBalances = new HashMap<>();
        minBalances.put(Currency.BTC, xbtLotSize);
        minBalances.put(Currency.EUR, minEurBalance);
        minBalances.put(Currency.USD, minUsdBalance);

        indicatorService.addListener(this);
    }

    @Override
    public void findTrades(List<Indicator> indicators) {

        BigDecimal center = dltService.isInitialized() ? dltService.getCenter() : BigDecimal.ZERO;

        if (!sanityCheck())
            return;

        if (!dltService.isInitialized())
            return;

        Map<Currency, BigDecimal> balances = balanceService.getBalances();
        Map<Currency, BigDecimal> tradeCount = activeTradeManager.getTradeCountByCurrency();

        List<String> activeCryptos = activeTradeManager.getActiveCryptos();
        for (Indicator indicator : indicators) {
            CurrencyPair entryPair = CurrencyService.getCurrencyPairFromString(indicator.getEntryPair());
            if (!activeCryptos.contains(indicator.getCrypto()) &&
                    balances.containsKey(entryPair.counter) &&
                    ((!tradeCount.containsKey(entryPair.counter) && balances.get(entryPair.counter).compareTo(minBalances.get(entryPair.counter)) > 0) ||
                            (tradeCount.containsKey(entryPair.counter) && balances.get(entryPair.counter).compareTo(tradeCount.get(entryPair.counter).multiply(minBalances.get(entryPair.counter))) > 0))) {
                if (indicator.isStandardOpp(center)) {
                    initializeStandardTrade(indicator);
                    return;
                } else if (indicator.isFloatingOpp()) {
                    initializeFloatingTrade(indicator);
                    return;
                }
            }
        }
    }

    @Override
    public void indicatorsUpdated(List<Indicator> indicators) {
        findTrades(indicators);
    }

    /**
     * convert xbt lot size to the currency lot size
     * @param currency
     * @return
     */
    private BigDecimal getLotSize(Currency currency) {
        BigDecimal rate = currencyService.getExchangeRate(new CurrencyPair(Currency.BTC, currency));
        return xbtLotSize.multiply(rate);
    }

    private void initializeStandardTrade(Indicator indicator) {
        CurrencyPair entryPair = CurrencyService.getCurrencyPairFromString(indicator.getEntryPair());
        BigDecimal lotSize = getLotSize(entryPair.counter);
        BigDecimal cryptoAmt = lotSize.divide(indicator.getEntryPrice(), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal price = indicator.getEntryPrice().setScale(CurrencyService.getTick(entryPair).intValue(), BigDecimal.ROUND_HALF_UP);

        LimitOrder limitOrder = new LimitOrder(
                Order.OrderType.BID,
                cryptoAmt,
                entryPair,
                null,
                null,
                price
        );

        if (placeTrade(limitOrder, indicator))
            LOGGER.info("initializing standard trade. " + indicator.getEntryPair() + " -> " + indicator.getExitPair());
    }

    private void initializeFloatingTrade(Indicator indicator) {
        CurrencyPair entryPair = CurrencyService.getCurrencyPairFromString(indicator.getEntryPair());
        BigDecimal lotSize = getLotSize(entryPair.counter);
        BigDecimal cryptoAmt = lotSize.divide(indicator.getEntryFloatingPrice(), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal price = indicator.getEntryFloatingPrice().setScale(CurrencyService.getTick(entryPair).intValue(), BigDecimal.ROUND_HALF_UP);

        LimitOrder limitOrder = new LimitOrder(
                Order.OrderType.BID,
                cryptoAmt,
                entryPair,
                null,
                null,
                price
        );
        limitOrder.addOrderFlag(KrakenOrderFlags.POST);

        if (placeTrade(limitOrder, indicator))
            LOGGER.info("initializing floating trade. " + indicator.getEntryPair() + " -> " + indicator.getExitPair());
    }

    private Boolean placeTrade(LimitOrder order, Indicator indicator) {
        Trade trade = new Trade();
        try {
            String id = tradeService.placeLimitOrder(order);
            LocalOrder localOrder = new LocalOrder();
            localOrder.setId(id);
            localOrder.setOriginalAmount(order.getOriginalAmount());
            localOrder.setOriginalPrice(order.getLimitPrice());
            localOrder.setPair(CurrencyService.getStringFromCurrencyPair(order.getCurrencyPair()));
            localOrder.setStatus(Order.OrderStatus.NEW);
            trade.getEntryOrders().add(localOrder);
            trade.setActive(true);
            trade.setCryptoAmt(order.getOriginalAmount());
            trade.setIndicator(indicator);

            trade = tradeRepository.save(trade);
            activeTradeManager.addActiveTrade(trade);
            return true;
        } catch (Exception ex) {
            balanceService.updateLastTradeTime();
            LOGGER.error("could not place order", ex);

            return false;
        }
    }

    /**
     * Perform basic checks to ensure we're ready to trade
     * @return
     */
    private Boolean sanityCheck() {
        Map<Currency, BigDecimal> balances = balanceService.getBalances();
        if (balances == null || !balances.containsKey(BalanceService.TOTALUSDKEY) || balances.get(BalanceService.TOTALUSDKEY).compareTo(minUsdAccountBalance) < 0)
            return false;

        if (activeTradeManager.getActiveTrades().size() >= maxActiveTrades)
            return false;

        return true;
    }

}
