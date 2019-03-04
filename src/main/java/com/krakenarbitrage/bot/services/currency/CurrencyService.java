package com.krakenarbitrage.bot.services.currency;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrencyService {

    static Logger LOGGER = LoggerFactory.getLogger(CurrencyService.class);
    static CurrencyPair EOS_USD = new CurrencyPair(Currency.EOS, Currency.USD);
    static CurrencyPair EOS_EUR = new CurrencyPair(Currency.EOS, Currency.EUR);
    static CurrencyPair GNO_USD = new CurrencyPair(Currency.GNO, Currency.USD);
    static CurrencyPair GNO_EUR = new CurrencyPair(Currency.GNO, Currency.EUR);
    static CurrencyPair REP_USD = new CurrencyPair(Currency.REP, Currency.USD);
    static CurrencyPair REP_EUR = new CurrencyPair(Currency.REP, Currency.EUR);
    static CurrencyPair REP_BTC = new CurrencyPair(Currency.REP, Currency.BTC);
    static CurrencyPair XLM_USD = new CurrencyPair(Currency.XLM, Currency.USD);
    static CurrencyPair XLM_EUR = new CurrencyPair(Currency.XLM, Currency.EUR);
    static CurrencyPair XLM_BTC = new CurrencyPair(Currency.XLM, Currency.BTC);
    static CurrencyPair XMR_EUR = new CurrencyPair(Currency.XMR, Currency.EUR);

    static List<CurrencyPair> EIGHTTICKPAIRS = Arrays.asList(
            XLM_BTC,
            CurrencyPair.XRP_BTC
    );

    static List<CurrencyPair> SEVENTICKPAIRS = Arrays.asList(
            CurrencyPair.EOS_BTC
    );

    static List<CurrencyPair> SIXTICKPAIRS = Arrays.asList(
            CurrencyPair.ETC_BTC,
            CurrencyPair.LTC_BTC,
            REP_BTC,
            CurrencyPair.XMR_BTC
    );

    static List<CurrencyPair> FIVETICKPAIRS = Arrays.asList(
            CurrencyPair.BCH_BTC,
            CurrencyPair.DASH_BTC,
            CurrencyPair.ETH_BTC,
            CurrencyPair.GNO_BTC,
            CurrencyPair.XRP_EUR,
            CurrencyPair.XRP_USD,
            CurrencyPair.ZEC_BTC
    );

    static List<CurrencyPair> FOURTICKPAIRS = Arrays.asList(
            XLM_EUR,
            XLM_USD
    );

    static List<CurrencyPair> THREETICKPAIRS = Arrays.asList(
        CurrencyPair.ETC_EUR,
        CurrencyPair.ETC_USD,
        REP_EUR,
        REP_USD
    );

    static List<CurrencyPair> TWOTICKPAIRS = Arrays.asList(
            CurrencyPair.DASH_EUR,
            CurrencyPair.DASH_USD,
            CurrencyPair.ETH_EUR,
            CurrencyPair.ETH_USD,
            CurrencyPair.LTC_EUR,
            CurrencyPair.LTC_USD,
            XMR_EUR,
            CurrencyPair.XMR_USD,
            CurrencyPair.ZEC_EUR,
            CurrencyPair.ZEC_USD,
            GNO_EUR,
            GNO_USD,
            EOS_USD,
            EOS_EUR
    );

    static List<CurrencyPair> ONETICKPAIRS = Arrays.asList(
            CurrencyPair.BCH_EUR,
            CurrencyPair.BCH_USD,
            CurrencyPair.BTC_EUR,
            CurrencyPair.BTC_USD
    );

    public static List<CurrencyPair> PAIRS = Arrays.asList(
            CurrencyPair.BTC_USD,
            CurrencyPair.BTC_EUR,

            CurrencyPair.ETH_USD,
            CurrencyPair.ETH_EUR,
            CurrencyPair.ETH_BTC,

            CurrencyPair.BCH_USD,
            CurrencyPair.BCH_EUR,
            CurrencyPair.BCH_BTC,

            CurrencyPair.DASH_USD,
            CurrencyPair.DASH_EUR,
            CurrencyPair.DASH_BTC,

            EOS_USD,
            EOS_EUR,
            CurrencyPair.EOS_BTC,

            GNO_USD,
            GNO_EUR,
            CurrencyPair.GNO_BTC,

            CurrencyPair.ETC_USD,
            CurrencyPair.ETC_EUR,
            CurrencyPair.ETC_BTC,

            CurrencyPair.LTC_USD,
            CurrencyPair.LTC_EUR,
            CurrencyPair.LTC_BTC,

            REP_USD,
            REP_EUR,
            REP_BTC,

            XLM_USD,
            XLM_EUR,
            XLM_BTC,

            CurrencyPair.XMR_USD,
            XMR_EUR,
            CurrencyPair.XMR_BTC,

            CurrencyPair.XRP_USD,
            CurrencyPair.XRP_EUR,
            CurrencyPair.XRP_BTC,

            CurrencyPair.ZEC_USD,
            CurrencyPair.ZEC_EUR,
            CurrencyPair.ZEC_BTC
    );

    public static List<Currency> CRYPTOS = Arrays.asList(
            Currency.BTC,
            Currency.ETH,
            Currency.BCH,
            Currency.DASH,
            Currency.EOS,
            Currency.GNO,
            Currency.ETC,
            Currency.LTC,
            Currency.REP,
            Currency.XLM,
            Currency.XMR,
            Currency.XRP,
            Currency.ZEC
    );

   public static BigDecimal getTick(CurrencyPair pair) {

        if (EIGHTTICKPAIRS.contains(pair))
            return new BigDecimal(8);
        if (SEVENTICKPAIRS.contains(pair))
            return new BigDecimal(7);
        if (SIXTICKPAIRS.contains(pair))
            return new BigDecimal(6);
        if (FIVETICKPAIRS.contains(pair))
            return new BigDecimal(5);
        if (FOURTICKPAIRS.contains(pair))
            return new BigDecimal(4);
        if (THREETICKPAIRS.contains(pair))
            return new BigDecimal(3);
        if (TWOTICKPAIRS.contains(pair))
            return new BigDecimal(2);
        if (ONETICKPAIRS.contains(pair))
            return new BigDecimal(1);

        LOGGER.error("cannot get tick unknown currency pair " + pair);
        return BigDecimal.ONE;
    }

    static final Map<Currency, BigDecimal> MINLOTS = new HashMap<>();
    static {
        MINLOTS.put(Currency.BTC, new BigDecimal("0.002"));
        MINLOTS.put(Currency.REP, new BigDecimal("0.3"));
        MINLOTS.put(Currency.BCH, new BigDecimal("0.002"));
        MINLOTS.put(Currency.DASH, new BigDecimal("0.03"));
        MINLOTS.put(Currency.EOS, new BigDecimal("3"));
        MINLOTS.put(Currency.ETH, new BigDecimal("0.02"));
        MINLOTS.put(Currency.ETC, new BigDecimal("0.3"));
        MINLOTS.put(Currency.GNO, new BigDecimal("0.03"));
        MINLOTS.put(Currency.LTC, new BigDecimal("0.1"));
        MINLOTS.put(Currency.XMR, new BigDecimal("0.1"));
        MINLOTS.put(Currency.XRP, new BigDecimal("30"));
        MINLOTS.put(Currency.XLM, new BigDecimal("30"));
        MINLOTS.put(Currency.ZEC, new BigDecimal("0.03"));
    }

    public static BigDecimal getMinimumLotSize(Currency crypto) {
        return MINLOTS.get(crypto);
    }

    public static BigDecimal getMinimumLotSize(String crypto) {
        Currency currency = STRINGCURRENCIES.get(crypto);
        return getMinimumLotSize(currency);
    }

    static final Map<String, CurrencyPair> STRINGPAIRS = new HashMap<>();
    static {
        STRINGPAIRS.put("XXBTZUSD", CurrencyPair.BTC_USD);
        STRINGPAIRS.put("XXBTZEUR", CurrencyPair.BTC_EUR);

        STRINGPAIRS.put("XETHZUSD", CurrencyPair.ETH_USD);
        STRINGPAIRS.put("XETHZEUR", CurrencyPair.ETH_EUR);
        STRINGPAIRS.put("XETHXXBT", CurrencyPair.ETH_BTC);

        STRINGPAIRS.put("XETCZUSD", CurrencyPair.ETC_USD);
        STRINGPAIRS.put("XETCZEUR", CurrencyPair.ETC_EUR);
        STRINGPAIRS.put("XETCXXBT", CurrencyPair.ETC_BTC);

        STRINGPAIRS.put("XLTCZUSD", CurrencyPair.LTC_USD);
        STRINGPAIRS.put("XLTCZEUR", CurrencyPair.LTC_EUR);
        STRINGPAIRS.put("XLTCXXBT", CurrencyPair.LTC_BTC);

        STRINGPAIRS.put("XREPZUSD", REP_USD);
        STRINGPAIRS.put("XREPZEUR", REP_EUR);
        STRINGPAIRS.put("XREPXXBT", REP_BTC);

        STRINGPAIRS.put("XXLMZUSD", XLM_USD);
        STRINGPAIRS.put("XXLMZEUR", XLM_EUR);
        STRINGPAIRS.put("XXLMXXBT", XLM_BTC);

        STRINGPAIRS.put("XXMRZUSD", CurrencyPair.XMR_USD);
        STRINGPAIRS.put("XXMRZEUR", XMR_EUR);
        STRINGPAIRS.put("XXMRXXBT", CurrencyPair.XMR_BTC);

        STRINGPAIRS.put("XXRPZUSD", CurrencyPair.XRP_USD);
        STRINGPAIRS.put("XXRPZEUR", CurrencyPair.XRP_EUR);
        STRINGPAIRS.put("XXRPXXBT", CurrencyPair.XRP_BTC);

        STRINGPAIRS.put("XZECZUSD", CurrencyPair.ZEC_USD);
        STRINGPAIRS.put("XZECZEUR", CurrencyPair.ZEC_EUR);
        STRINGPAIRS.put("XZECXXBT", CurrencyPair.ZEC_BTC);

        STRINGPAIRS.put("BCHUSD", CurrencyPair.BCH_USD);
        STRINGPAIRS.put("BCHEUR", CurrencyPair.BCH_EUR);
        STRINGPAIRS.put("BCHXBT", CurrencyPair.BCH_BTC);

        STRINGPAIRS.put("DASHUSD", CurrencyPair.DASH_USD);
        STRINGPAIRS.put("DASHEUR", CurrencyPair.DASH_EUR);
        STRINGPAIRS.put("DASHXBT", CurrencyPair.DASH_BTC);

        STRINGPAIRS.put("EOSUSD", EOS_USD);
        STRINGPAIRS.put("EOSEUR", EOS_EUR);
        STRINGPAIRS.put("EOSXBT", CurrencyPair.EOS_BTC);

        STRINGPAIRS.put("GNOUSD", GNO_USD);
        STRINGPAIRS.put("GNOEUR", GNO_EUR);
        STRINGPAIRS.put("GNOXBT", CurrencyPair.GNO_BTC);

    }

    public static Map<String, Currency> STRINGCURRENCIES = new HashMap<>();
    static {
        STRINGCURRENCIES.put("ZUSD", Currency.USD);
        STRINGCURRENCIES.put("ZEUR", Currency.EUR);
        STRINGCURRENCIES.put("XXBT", Currency.BTC);
        for (Currency crypto : CRYPTOS) {
            STRINGCURRENCIES.put(crypto.toString(), crypto);
        }
    }

    public static CurrencyPair getCurrencyPairFromString(String pair) {
        return STRINGPAIRS.get(pair);
    }

    public static Currency getCurrencyFromString(String currency) {
        return STRINGCURRENCIES.get(currency);
    }

    public static String getStringFromCurrencyPair(CurrencyPair pair) {
        for (String key : STRINGPAIRS.keySet()) {
            if (STRINGPAIRS.get(key).equals(pair))
                return key;
        }
        LOGGER.error("map does not contain value " + pair.toString());
        return pair.toString();
    }

}
