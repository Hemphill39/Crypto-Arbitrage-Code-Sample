package com.krakenarbitrage.bot.domains.indicator;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public class Indicator {

    private static BigDecimal NEGONE = new BigDecimal("-1");
    @Id
    private String id;
    @Indexed
    private Date timestamp;
    private String entryPair;
    private String exitPair;
    private String crypto;
    private Boolean forward;
    private BigDecimal exchangeRate;
    private BigDecimal value;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private BigDecimal entryBestBid;
    private BigDecimal entryFloatingPrice;
    private BigDecimal spread;

    public Indicator() {
        this.timestamp = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getEntryPair() {
        return entryPair;
    }

    public void setEntryPair(String entryPair) {
        this.entryPair = entryPair;
    }

    public String getExitPair() {
        return exitPair;
    }

    public void setExitPair(String exitPair) {
        this.exitPair = exitPair;
    }

    public String getCrypto() {
        return crypto;
    }

    public void setCrypto(String crypto) {
        this.crypto = crypto;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(BigDecimal entryPrice) {
        this.entryPrice = entryPrice;
    }

    public BigDecimal getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(BigDecimal exitPrice) {
        this.exitPrice = exitPrice;
    }

    public BigDecimal getEntryBestBid() {
        return entryBestBid;
    }

    public void setEntryBestBid(BigDecimal entryBestBid) {
        this.entryBestBid = entryBestBid;
    }

    public BigDecimal getEntryFloatingPrice() {
        return entryFloatingPrice;
    }

    public void setEntryFloatingPrice(BigDecimal entryFloatingPrice) {
        this.entryFloatingPrice = entryFloatingPrice;
    }

    public BigDecimal getSpread() {
        return spread;
    }

    public void setSpread(BigDecimal spread) {
        this.spread = spread;
    }

    public void setValues(BigDecimal spread) {
        this.value = exitPrice.divide(entryPrice, 8, BigDecimal.ROUND_HALF_UP).divide(exchangeRate, 8, RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
        this.entryFloatingPrice = exitPrice.divide(spread.add(BigDecimal.ONE), 8, BigDecimal.ROUND_HALF_UP).divide(exchangeRate, 8, RoundingMode.HALF_UP);
        if (!this.forward) {
            this.value = this.value.multiply(NEGONE);
        }
    }

    public Boolean isStandardOpp(BigDecimal center) {
        if (forward)
            return value.compareTo(center.add(spread)) > 0;
        return value.compareTo(center.subtract(spread)) < 0;
    }

    public Boolean isFloatingOpp() {
        return entryFloatingPrice.compareTo(entryBestBid) > 0 && entryFloatingPrice.compareTo(entryPrice) < 0;
    }

    public Boolean getForward() {
        return forward;
    }

    public void setForward(Boolean forward) {
        this.forward = forward;
    }
}
