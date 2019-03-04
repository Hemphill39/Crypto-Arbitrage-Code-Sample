package com.krakenarbitrage.bot.services.order;

import org.knowm.xchange.dto.Order;

import java.util.List;

public interface OrderService {

    void updateOrders();
    void addListener(OrderListener listener);
    void notifyListeners(List<Order> orders);
}
