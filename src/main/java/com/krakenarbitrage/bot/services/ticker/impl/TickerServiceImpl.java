package com.krakenarbitrage.bot.services.ticker.impl;

import com.krakenarbitrage.bot.exchanges.KrakenSingleton;
import com.krakenarbitrage.bot.services.currency.CurrencyService;
import com.krakenarbitrage.bot.services.ticker.TickerListener;
import com.krakenarbitrage.bot.services.ticker.TickerService;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.kraken.dto.marketdata.KrakenTicker;
import org.knowm.xchange.kraken.service.KrakenMarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class TickerServiceImpl implements TickerService {

    private static Logger LOGGER = LoggerFactory.getLogger(TickerServiceImpl.class);
    private final List<TickerListener> listeners = new ArrayList<>();
    private final KrakenMarketDataService marketDataService;
    private CompletableFuture<Void> tickerFuture;
    private Map<CurrencyPair, KrakenTicker> tickers;

    public TickerServiceImpl(KrakenSingleton kraken) {
        this.marketDataService = (KrakenMarketDataService) kraken.getMarketDataService();
    }

    @Override
    public Map<CurrencyPair, KrakenTicker> getTickers() {
        if (tickers == null) {
            updateTickers();
        }
        return tickers;
    }

    @Override
    public KrakenTicker getTicker(CurrencyPair pair) {
        if (tickers.containsKey(pair))
            return tickers.get(pair);
        LOGGER.error("tickers does not contain pair " + pair);
        return null;
    }

    @Override
    @Scheduled(fixedDelay = 1250)
    public void updateTickers() {
        if (tickerFuture == null || tickerFuture.isDone()) {
            tickerFuture = getKrakenTickers();
            tickerFuture.join();
        }
    }

    @Override
    public void notifyListeners() {
        for (TickerListener listener : listeners) {
            listener.tickersUpdated(tickers);
        }
    }

    @Override
    public void addListener(TickerListener listener) {
        this.listeners.add(listener);
    }

    private void adaptKrakenTickers(Map<String, KrakenTicker> stringTickers) {
        Map<CurrencyPair, KrakenTicker> newTickers = new HashMap<>();
        for (String key : stringTickers.keySet()) {
            newTickers.put(CurrencyService.getCurrencyPairFromString(key), stringTickers.get(key));
        }
        this.tickers = newTickers;
    }

    private CompletableFuture<Void> getKrakenTickers() {
        return CompletableFuture.runAsync(() -> {
            try {
                adaptKrakenTickers(marketDataService.getKrakenTickers(CurrencyService.PAIRS.toArray(new CurrencyPair[CurrencyService.PAIRS.size()])));
                notifyListeners();
            } catch (Exception ex) {
                LOGGER.error("could not update kraken tickers", ex);
            }
        });
    }
}
