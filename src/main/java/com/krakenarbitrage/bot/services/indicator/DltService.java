package com.krakenarbitrage.bot.services.indicator;

import com.krakenarbitrage.bot.domains.indicator.Indicator;

import java.math.BigDecimal;
import java.util.List;

public interface DltService {
    BigDecimal getCenter();
    Boolean isInitialized();
    void addDataPoints(List<Indicator> indicators);
}
