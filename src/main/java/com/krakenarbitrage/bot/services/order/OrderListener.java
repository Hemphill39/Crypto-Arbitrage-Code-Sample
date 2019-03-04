package com.krakenarbitrage.bot.services.order;

import org.knowm.xchange.dto.Order;

import java.util.List;

public interface OrderListener {
    void ordersUpdated(List<Order> orders);
}
