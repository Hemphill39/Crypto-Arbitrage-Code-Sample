package com.krakenarbitrage.bot.exchanges;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KrakenSingleton {

    private final MarketDataService marketDataService;
    private final AccountService accountService;
    private final TradeService tradeService;

    public KrakenSingleton(
            @Value("${kraken-api-key}") String apiKey,
            @Value("${kraken-api-secret}") String apiSecret) {
        Exchange exchange = getExchange(apiKey, apiSecret);
        this.marketDataService = exchange.getMarketDataService();
        this.accountService = exchange.getAccountService();
        this.tradeService = exchange.getTradeService();
    }

    public MarketDataService getMarketDataService() {
        return marketDataService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public TradeService getTradeService() {
        return tradeService;
    }

    private void setExchangeSpecs(ExchangeSpecification exchangeSpecification, String apiKey, String apiSecret) {
        exchangeSpecification.setApiKey(apiKey);
        exchangeSpecification.setSecretKey(apiSecret);
    }

    private Exchange getExchange(String apiKey, String apiSecret) {
        ExchangeSpecification exchangeSpecification = new KrakenExchange().getDefaultExchangeSpecification();
        setExchangeSpecs(exchangeSpecification, apiKey, apiSecret);
        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
        return exchange;
    }
}
