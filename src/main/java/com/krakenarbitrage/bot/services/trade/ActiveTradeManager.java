package com.krakenarbitrage.bot.services.trade;

import com.krakenarbitrage.bot.domains.trade.Trade;
import org.knowm.xchange.currency.Currency;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ActiveTradeManager {

    List<Trade> getActiveTrades();
    List<Trade> getEntryTrades();
    List<Trade> getExitTrades();
    List<String> getActiveCryptos();
    Map<Currency, BigDecimal> getTradeCountByCurrency();
    void addActiveTrade(Trade trade);
    void removeActiveTrade(Trade trade);
    Date getLastActiveTradeDate();
}
