package com.krakenarbitrage.bot.services.trade;

import com.krakenarbitrage.bot.domains.indicator.Indicator;
import com.krakenarbitrage.bot.services.indicator.IndicatorListener;

import java.util.List;

public interface TradeFinder extends IndicatorListener {

    void findTrades(List<Indicator> indicators);
}
