package com.krakenarbitrage.bot.services.ticker;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.kraken.dto.marketdata.KrakenTicker;

import java.util.Map;

public interface TickerListener {
    void tickersUpdated(Map<CurrencyPair, KrakenTicker> tickers);
}
