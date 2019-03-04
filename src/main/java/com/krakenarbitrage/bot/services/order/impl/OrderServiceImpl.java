package com.krakenarbitrage.bot.services.order.impl;

import com.krakenarbitrage.bot.domains.trade.LocalOrder;
import com.krakenarbitrage.bot.domains.trade.Trade;
import com.krakenarbitrage.bot.exchanges.KrakenSingleton;
import com.krakenarbitrage.bot.services.order.OrderListener;
import com.krakenarbitrage.bot.services.order.OrderService;
import com.krakenarbitrage.bot.services.trade.ActiveTradeManager;
import com.krakenarbitrage.bot.services.trade.TradeManager;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);
    private final List<OrderListener> listeners;
    private final ActiveTradeManager activeTradeManager;
    private final TradeService tradeService;

    private CompletableFuture<Void> orderFuture;

    public OrderServiceImpl(
            ActiveTradeManager activeTradeManager,
            KrakenSingleton kraken) {
        this.activeTradeManager = activeTradeManager;
        this.listeners = new ArrayList<>();
        this.tradeService = kraken.getTradeService();
    }

    @Override
    @Scheduled(fixedRate = 100)
    public void updateOrders() {
        if (orderFuture == null || orderFuture.isDone()) {
            orderFuture = CompletableFuture.runAsync(() -> {
                Date start = new Date();
                List<Trade> activeTrades = activeTradeManager.getActiveTrades();
                List<String> orderIds = new ArrayList<>();
                for (Trade trade : activeTrades) {
                    orderIds.addAll(trade.getEntryOrders().stream().map(LocalOrder::getId).collect(Collectors.toList()));
                    orderIds.addAll(trade.getExitOrders().stream().map(LocalOrder::getId).collect(Collectors.toList()));
                }

                if (orderIds.size() > 0) {
                    try {
                        Collection<Order> orders = tradeService.getOrder(orderIds.toArray(new String[orderIds.size()]));
                        List<Order> orderList = new ArrayList<>();
                        orderList.addAll(orders);
                        notifyListeners(orderList);
                    } catch (Exception ex) {
                        LOGGER.error("could not update order status", ex);
                    }
                }
                Date end = new Date();
                if (end.getTime() - start.getTime() < 1000) {
                    try {
                        Long elapsed = end.getTime() - start.getTime();
                        Long sleep = 1000L - elapsed;
                        Thread.sleep(sleep);
                    } catch (Exception ex) {
                        LOGGER.error("could not sleep order service", ex);
                    }
                }
            });
        }
    }

    @Override
    public void addListener(OrderListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void notifyListeners(List<Order> orders) {
        for (OrderListener listener : listeners) {
            listener.ordersUpdated(orders);
        }
    }

}
