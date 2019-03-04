package com.krakenarbitrage.bot.domains.trade;

import org.knowm.xchange.dto.Order;

import java.math.BigDecimal;
import java.util.Date;

public class LocalOrder {

    private String id;
    private Date timestamp;
    private BigDecimal originalAmount;
    private BigDecimal amountFilled;
    private BigDecimal remainingAmount;
    private BigDecimal originalPrice;
    private BigDecimal averagePrice;
    private String pair;
    private Order.OrderStatus status;


    public LocalOrder() {
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

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(BigDecimal averagePrice) {
        this.averagePrice = averagePrice;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public Order.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(Order.OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getAmountFilled() {
        return amountFilled;
    }

    public void setAmountFilled(BigDecimal amountFilled) {
        this.amountFilled = amountFilled;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public void updateOrder(Order order) {
        this.averagePrice = order.getAveragePrice();
        this.status = order.getStatus();
        this.amountFilled = order.getCumulativeAmount();
        this.remainingAmount = order.getRemainingAmount();
    }
}
