package com.krakenarbitrage.bot.services.indicator.impl;

import com.krakenarbitrage.bot.domains.indicator.Indicator;
import com.krakenarbitrage.bot.repositories.IndicatorRepository;
import com.krakenarbitrage.bot.services.indicator.DltService;
import com.krakenarbitrage.bot.services.indicator.IndicatorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DltServiceImpl implements IndicatorListener, DltService {

    private final Logger LOGGER = LoggerFactory.getLogger(DltServiceImpl.class);
    private final List<BigDecimal> values;
    private final Long lookback;
    private Date firstDataPointDate;
    private BigDecimal center;
    private Boolean initialized;
    private Integer cnt = 0;


    public DltServiceImpl(
            IndicatorRepository indicatorRepository,
            @Value("${dlt-lookback}") Long lookback)
    {
        this.lookback = lookback;
        values = new ArrayList<>();
        firstDataPointDate = new Date();
        center = BigDecimal.ZERO;
        initialized = false;
        Date fifteenMinutes = new Date();
        fifteenMinutes.setTime(fifteenMinutes.getTime() - lookback);

        List<Indicator> indicators = indicatorRepository.findAllByTimestampAfterOrderByTimestampAsc(fifteenMinutes);
        if (indicators.size() > 0) {
            firstDataPointDate = indicators.get(0).getTimestamp();
            values.addAll(indicators.stream().map(Indicator::getValue).collect(Collectors.toList()));
            addDataPoints(indicators);
        }
    }

    @Override
    public BigDecimal getCenter() {
        return center;
    }

    @Override
    public void addDataPoints(List<Indicator> indicators) {
        values.addAll(indicators.stream().map(Indicator::getValue).collect(Collectors.toList()));

        if (firstDataPointDate.getTime() + lookback < new Date().getTime()) {
            removeOldDataPoints(indicators.size());
            initialized = true;
            if (cnt % 20 == 0) {
                setCenter();
            }
        }
    }

    @Override
    public void indicatorsUpdated(List<Indicator> indicators) {
        addDataPoints(indicators);
        cnt++;
    }

    @Override
    public Boolean isInitialized() {
        return initialized;
    }

    private void setCenter() {
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        center =sum.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : sum.divide(BigDecimal.valueOf(values.size()), 10, BigDecimal.ROUND_HALF_DOWN);
        LOGGER.info("dlt center " + center);
    }

    private void removeOldDataPoints(Integer size) {
        for (int i = 0; i < size; i++) {
            values.remove(0);
        }
    }
}
