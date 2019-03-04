package com.krakenarbitrage.bot.services.trade.impl;

import com.krakenarbitrage.bot.domains.trade.Trade;
import com.krakenarbitrage.bot.repositories.TradeRepository;
import com.krakenarbitrage.bot.services.currency.CurrencyService;
import com.krakenarbitrage.bot.services.trade.ActiveTradeManager;
import org.knowm.xchange.currency.Currency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActiveTradeManagerImpl implements ActiveTradeManager {

    private static List<Trade> ACTIVETRADES = new ArrayList<>();
    private static Date LASTTRADEDATE;
    private static Integer counter = 0;

    public ActiveTradeManagerImpl(TradeRepository tradeRepository) {
        ACTIVETRADES = tradeRepository.findAllByActiveEquals(true);
        LASTTRADEDATE = new Date();
        counter++;
    }

    @Override
    public List<Trade> getActiveTrades() {
        return ACTIVETRADES;
    }

    @Override
    public void addActiveTrade(Trade trade) {
        ACTIVETRADES.add(trade);
        LASTTRADEDATE = new Date();
    }

    @Override
    public void removeActiveTrade(Trade trade) {
        ACTIVETRADES.remove(trade);
        trade.setActive(false);
        LASTTRADEDATE = new Date();
    }

    @Override
    public List<Trade> getEntryTrades() {
        return ACTIVETRADES.stream().filter(x -> !x.getEntryComplete()).collect(Collectors.toList());
    }

    @Override
    public List<Trade> getExitTrades() {
        return ACTIVETRADES.stream().filter(x -> x.getEntryComplete() && !x.getExitComplete()).collect(Collectors.toList());
    }

    @Override
    public List<String> getActiveCryptos() {
        return ACTIVETRADES.stream().map(x -> x.getIndicator().getCrypto()).collect(Collectors.toList());
    }

    @Override
    public Map<Currency, BigDecimal> getTradeCountByCurrency() {
        Map<Currency, BigDecimal> tradeCount = new HashMap<>();
        for (Trade trade : ACTIVETRADES) {
            Currency counter = CurrencyService.getCurrencyPairFromString(trade.getIndicator().getEntryPair()).counter;
            if (!tradeCount.containsKey(counter))
                tradeCount.put(counter, BigDecimal.ONE);
            tradeCount.replace(counter, tradeCount.get(counter).add(BigDecimal.ONE));
        }
        return tradeCount;
    }

    @Override
    public Date getLastActiveTradeDate() {
        return LASTTRADEDATE;
    }
}
