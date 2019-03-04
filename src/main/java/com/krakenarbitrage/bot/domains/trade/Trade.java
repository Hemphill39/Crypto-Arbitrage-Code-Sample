package com.krakenarbitrage.bot.domains.trade;

import com.krakenarbitrage.bot.domains.indicator.Indicator;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Trade {

    @Id
    private String id;
    private Date timestamp;
    private Indicator indicator;
    private Boolean active;
    private Boolean entryComplete;
    private Boolean exitComplete;
    private BigDecimal cryptoAmt;
    private BigDecimal cryptoPurchased;
    private BigDecimal cryptoSold;
    private BigDecimal entryCostBtc;
    private BigDecimal exitCostBtc;
    private BigDecimal pnlBtc;
    private BigDecimal fillQuality;
    private List<LocalOrder> entryOrders;
    private List<LocalOrder> exitOrders;

    public Trade() {
        entryComplete = false;
        exitComplete = false;
        cryptoAmt = BigDecimal.ZERO;
        cryptoPurchased = BigDecimal.ZERO;
        cryptoSold = BigDecimal.ZERO;
        entryOrders = new ArrayList<>();
        exitOrders = new ArrayList<>();
        timestamp = new Date();
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

    public Indicator getIndicator() {
        return indicator;
    }

    public void setIndicator(Indicator indicator) {
        this.indicator = indicator;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getEntryComplete() {
        return entryComplete;
    }

    public void setEntryComplete(Boolean entryComplete) {
        this.entryComplete = entryComplete;
    }

    public Boolean getExitComplete() {
        return exitComplete;
    }

    public void setExitComplete(Boolean exitComplete) {
        this.exitComplete = exitComplete;
    }

    public BigDecimal getCryptoAmt() {
        return cryptoAmt;
    }

    public void setCryptoAmt(BigDecimal cryptoAmt) {
        this.cryptoAmt = cryptoAmt;
    }

    public BigDecimal getCryptoPurchased() {
        return cryptoPurchased;
    }

    public void setCryptoPurchased(BigDecimal cryptoPurchased) {
        this.cryptoPurchased = cryptoPurchased;
    }

    public BigDecimal getCryptoSold() {
        return cryptoSold;
    }

    public void setCryptoSold(BigDecimal cryptoSold) {
        this.cryptoSold = cryptoSold;
    }

    public List<LocalOrder> getEntryOrders() {
        return entryOrders;
    }

    public void setEntryOrders(List<LocalOrder> entryOrders) {
        this.entryOrders = entryOrders;
    }

    public List<LocalOrder> getExitOrders() {
        return exitOrders;
    }

    public void setExitOrders(List<LocalOrder> exitOrders) {
        this.exitOrders = exitOrders;
    }

    public BigDecimal getEntryCostBtc() {
        return entryCostBtc;
    }

    public BigDecimal getPnlBtc() {
        return pnlBtc;
    }

    public void setPnlBtc(BigDecimal pnlBtc) {
        this.pnlBtc = pnlBtc;
    }

    public BigDecimal getFillQuality() {
        return fillQuality;
    }

    public void setFillQuality(BigDecimal fillQuality) {
        this.fillQuality = fillQuality;
    }

    public void setEntryCostBtc(BigDecimal entryCostBtc) {
        this.entryCostBtc = entryCostBtc;
    }

    public BigDecimal getExitCostBtc() {
        return exitCostBtc;
    }

    public void setExitCostBtc(BigDecimal exitCostBtc) {
        this.exitCostBtc = exitCostBtc;
    }
}
