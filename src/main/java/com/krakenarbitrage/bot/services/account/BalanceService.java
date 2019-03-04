package com.krakenarbitrage.bot.services.account;

import org.knowm.xchange.currency.Currency;

import java.math.BigDecimal;
import java.util.Map;

public interface BalanceService {
    Currency TOTALUSDKEY = Currency.TOP;

    Map<Currency, BigDecimal> getBalances();
    void updateLastTradeTime();
}
