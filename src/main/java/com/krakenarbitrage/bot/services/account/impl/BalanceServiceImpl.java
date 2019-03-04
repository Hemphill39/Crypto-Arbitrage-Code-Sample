package com.krakenarbitrage.bot.services.account.impl;

import com.krakenarbitrage.bot.exchanges.KrakenSingleton;
import com.krakenarbitrage.bot.services.account.BalanceService;
import com.krakenarbitrage.bot.services.currency.CurrencyService;
import com.krakenarbitrage.bot.services.currency.impl.CurrencyServiceImpl;
import com.krakenarbitrage.bot.services.trade.ActiveTradeManager;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.kraken.service.KrakenAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class BalanceServiceImpl implements BalanceService {

    private final Logger LOGGER = LoggerFactory.getLogger(BalanceServiceImpl.class);
    private final KrakenAccountService accountService;
    private final ActiveTradeManager activeTradeManager;
    private final CurrencyServiceImpl currencyService;
    private Map<Currency, BigDecimal> balances;
    private Date lastBalances;

    public BalanceServiceImpl(
            ActiveTradeManager activeTradeManager,
            CurrencyServiceImpl currencyService,
            KrakenSingleton kraken) {
        this.activeTradeManager = activeTradeManager;
        this.currencyService = currencyService;
        this.accountService = (KrakenAccountService) kraken.getAccountService();
    }

    public Map<Currency, BigDecimal> getBalances() {
        if (balances == null || activeTradeManager.getLastActiveTradeDate().compareTo(lastBalances) > 0) {
            updateBalances();
        }
        return balances;
    }

    public void updateLastTradeTime() {
        lastBalances = new Date();
    }

    private void updateBalances() {

        try {
            Map<String, BigDecimal> strBalances = accountService.getKrakenBalance();
            this.balances = adaptBalances(strBalances);
            lastBalances = new Date();
        } catch (Exception ex) {
            LOGGER.error("could not update balances", ex);
            this.balances = null;
        }
    }

    private Map<Currency, BigDecimal> adaptBalances(Map<String, BigDecimal> strBalances) {
        Map<Currency, BigDecimal> balances = new HashMap<>();
        balances.put(TOTALUSDKEY, BigDecimal.ZERO);
        for (String key : strBalances.keySet()) {
            if (CurrencyService.STRINGCURRENCIES.containsKey(key)) {
                Currency cur = CurrencyService.getCurrencyFromString(key);
                BigDecimal bal = strBalances.get(key);
                balances.put(cur, bal);

                // update total usd balance
                BigDecimal rate = currencyService.getExchangeRate(new CurrencyPair(cur, Currency.USD));
                if (rate != null) {
                    BigDecimal usdEquiv = rate.multiply(bal);
                    balances.replace(TOTALUSDKEY, balances.get(TOTALUSDKEY).add(usdEquiv));
                }
            }
        }
        return balances;
    }
}
