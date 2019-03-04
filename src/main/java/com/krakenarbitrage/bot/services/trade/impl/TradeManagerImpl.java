package com.krakenarbitrage.bot.services.trade.impl;

import com.krakenarbitrage.bot.domains.trade.LocalOrder;
import com.krakenarbitrage.bot.domains.trade.Trade;
import com.krakenarbitrage.bot.services.order.OrderListener;
import com.krakenarbitrage.bot.services.order.OrderService;
import com.krakenarbitrage.bot.services.trade.ActiveTradeManager;
import com.krakenarbitrage.bot.services.trade.TradeManager;
import org.knowm.xchange.dto.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TradeManagerImpl implements TradeManager, OrderListener {

    private final Logger LOGGER = LoggerFactory.getLogger(TradeManagerImpl.class);
    private final ActiveTradeManager activeTradeManager;
    private final EntryTradeManager entryTradeManager;
    private final ExitTradeManager exitTradeManager;

    public TradeManagerImpl(
            ActiveTradeManager activeTradeManager,
            EntryTradeManager entryTradeManager,
            ExitTradeManager exitTradeManager,
            OrderService orderService) {
        this.activeTradeManager = activeTradeManager;
        this.entryTradeManager = entryTradeManager;
        this.exitTradeManager = exitTradeManager;
        orderService.addListener(this);
    }

    @Override
    public void ordersUpdated(List<Order> orders) {
        manageTrades(orders);
    }

    @Override
    public void manageTrades(List<Order> orders) {
        for (Order order : orders) {
            for (Trade trade : activeTradeManager.getActiveTrades()) {
                for (LocalOrder localOrder : trade.getEntryOrders()) {
                    if (localOrder.getId().equalsIgnoreCase(order.getId())) {
                        localOrder.updateOrder(order);
                    }
                }
                for (LocalOrder localOrder : trade.getExitOrders()) {
                    if (localOrder.getId().equalsIgnoreCase(order.getId())) {
                        localOrder.updateOrder(order);
                    }
                }
            }
        }
        CompletableFuture<Void> entryFuture = entryTradeManager.manageTrades();
        CompletableFuture<Void> exitFuture = exitTradeManager.manageTrades();
        CompletableFuture.allOf(entryFuture, exitFuture).join();
    }
}
