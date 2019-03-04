package com.krakenarbitrage.bot.services.ticker;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.kraken.dto.marketdata.KrakenTicker;

import java.util.Map;

public interface TickerService {

    Map<CurrencyPair, KrakenTicker> getTickers();
    KrakenTicker getTicker(CurrencyPair pair);
    void updateTickers();
    void notifyListeners();
    void addListener(TickerListener listener);
}
