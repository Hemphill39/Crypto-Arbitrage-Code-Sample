package com.krakenarbitrage.bot.services.currency.impl;

import com.krakenarbitrage.bot.exchanges.KrakenSingleton;
import com.krakenarbitrage.bot.services.ticker.TickerListener;
import com.krakenarbitrage.bot.services.ticker.TickerService;
import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.pricing.Price;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.kraken.dto.marketdata.KrakenTicker;
import org.knowm.xchange.kraken.service.KrakenMarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class CurrencyServiceImpl implements TickerListener {

    private static final String EURUSDINSTRUMENT = "EUR_USD";
    private final Logger LOGGER = LoggerFactory.getLogger(CurrencyServiceImpl.class);
    private final Map<CurrencyPair, BigDecimal> exchangeRates;
    private final KrakenMarketDataService krakenMarketDataService;
    private final AccountID accountID;
    private final String oandaApiKey;

    private CompletableFuture<Void> oandaFuture;

    public CurrencyServiceImpl(
            KrakenSingleton kraken,
            TickerService tickerService,
            @Value("${oanda-api-key}") String oandaApiKey,
            @Value("${oanda-account-id}") String oandaAccountId
    ) {
        this.krakenMarketDataService = (KrakenMarketDataService) kraken.getMarketDataService();
        this.accountID = new AccountID(oandaAccountId);
        this.oandaApiKey = oandaApiKey;
        tickerService.addListener(this);
        exchangeRates = new HashMap<>();
        updateFxRates();
    }

    public BigDecimal getExchangeRate(CurrencyPair pair) {
        if (pair.counter.equals(pair.base))
            return BigDecimal.ONE;
        return exchangeRates.get(pair);
    }

    @Async
    @Scheduled(fixedDelay = 2500)
    public void updateFxRates() {
        if (oandaFuture == null || oandaFuture.isDone())
            oandaFuture = updateEurUsdRate();
    }

    public void updateXbtRates(KrakenTicker btcusd, KrakenTicker btceur) {
        BigDecimal two = new BigDecimal("2");
        BigDecimal btcusdRate, btceurRate;

        btcusdRate = (btcusd.getBid().getPrice().add(btcusd.getAsk().getPrice())).divide(two, 2, BigDecimal.ROUND_HALF_UP);
        btceurRate = (btceur.getBid().getPrice().add(btceur.getAsk().getPrice())).divide(two, 2, BigDecimal.ROUND_HALF_UP);

        if (!exchangeRates.containsKey(CurrencyPair.BTC_EUR))
            exchangeRates.put(CurrencyPair.BTC_EUR, BigDecimal.ZERO);

        if (!exchangeRates.containsKey(CurrencyPair.BTC_USD))
            exchangeRates.put(CurrencyPair.BTC_USD, BigDecimal.ZERO);

        exchangeRates.replace(CurrencyPair.BTC_USD, btcusdRate);
        exchangeRates.replace(CurrencyPair.BTC_EUR, btceurRate);
    }

    @Override
    public void tickersUpdated(Map<CurrencyPair, KrakenTicker> tickers) {
        updateXbtRates(tickers.get(CurrencyPair.BTC_USD), tickers.get(CurrencyPair.BTC_EUR));
    }

    private CompletableFuture<Void> updateEurUsdRate() {
        return CompletableFuture.runAsync(() -> {
            try {
                Context ctx = new Context(
                        "https://api-fxpractice.oanda.com",
                        this.oandaApiKey);
                List<Price> prices = ctx.pricing.get(this.accountID, Arrays.asList(EURUSDINSTRUMENT)).getPrices();
                if (prices.size() > 0) {
                    Price price = prices.get(0);
                    BigDecimal rate = (price.getBids().get(0).getPrice().bigDecimalValue().add(price.getAsks().get(0).getPrice().bigDecimalValue())).divide(BigDecimal.valueOf(2), 8, BigDecimal.ROUND_HALF_UP);
                    if (!exchangeRates.containsKey(CurrencyPair.EUR_USD))
                        exchangeRates.put(CurrencyPair.EUR_USD, BigDecimal.ZERO);
                    exchangeRates.replace(CurrencyPair.EUR_USD, rate);
                }
            } catch (Exception ex) {
                LOGGER.error("error updating oanda exchange rates", ex);
            }
        });
    }
}
