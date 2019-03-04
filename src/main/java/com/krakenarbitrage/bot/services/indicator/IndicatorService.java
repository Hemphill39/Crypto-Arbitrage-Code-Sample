package com.krakenarbitrage.bot.services.indicator;

import com.krakenarbitrage.bot.domains.indicator.Indicator;
import com.krakenarbitrage.bot.services.ticker.TickerListener;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.kraken.dto.marketdata.KrakenTicker;

import java.util.List;
import java.util.Map;

public interface IndicatorService extends TickerListener {
    List<Indicator> getIndicators();
    Indicator getIndicator(CurrencyPair entry, CurrencyPair exit);
    void updateIndicators(Map<CurrencyPair, KrakenTicker> tickers);
    void addListener(IndicatorListener listener);
    void notifyListeners();

}
