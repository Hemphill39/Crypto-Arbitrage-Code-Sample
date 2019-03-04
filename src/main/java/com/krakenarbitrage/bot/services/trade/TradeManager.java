package com.krakenarbitrage.bot.services.trade;

import com.krakenarbitrage.bot.services.order.OrderListener;
import org.knowm.xchange.dto.Order;

import java.util.List;

public interface TradeManager extends OrderListener {

    void manageTrades(List<Order> orders);
}
