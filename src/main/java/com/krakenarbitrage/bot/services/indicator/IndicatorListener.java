package com.krakenarbitrage.bot.services.indicator;

import com.krakenarbitrage.bot.domains.indicator.Indicator;

import java.util.List;

public interface IndicatorListener {
    void indicatorsUpdated(List<Indicator> indicators);
}
