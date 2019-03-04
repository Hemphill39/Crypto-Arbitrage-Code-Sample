package com.krakenarbitrage.bot.services.indicator.impl;

import com.krakenarbitrage.bot.domains.indicator.Indicator;
import com.krakenarbitrage.bot.repositories.IndicatorRepository;
import com.krakenarbitrage.bot.services.currency.CurrencyService;
import com.krakenarbitrage.bot.services.currency.impl.CurrencyServiceImpl;
import com.krakenarbitrage.bot.services.indicator.DltService;
import com.krakenarbitrage.bot.services.indicator.IndicatorListener;
import com.krakenarbitrage.bot.services.indicator.IndicatorService;
import com.krakenarbitrage.bot.services.ticker.TickerService;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.kraken.dto.marketdata.KrakenTicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class IndicatorServiceImpl implements IndicatorService {

    private final Logger LOGGER = LoggerFactory.getLogger(IndicatorServiceImpl.class);
    private final List<IndicatorListener> listeners;
    private final CurrencyServiceImpl currencyService;
    private final IndicatorRepository indicatorRepository;
    private final DltService dltService;
    private final BigDecimal floatingSpread;
    private final BigDecimal spread;
    private List<Indicator> indicators;

    public IndicatorServiceImpl(
            CurrencyServiceImpl currencyService,
            TickerService tickerService,
            DltService dltService,
            IndicatorRepository indicatorRepository,
            @Value("${floating-spread}")BigDecimal floatingSpread,
            @Value("${spread}") BigDecimal spread) {
        this.dltService = dltService;
        this.currencyService = currencyService;
        this.floatingSpread = floatingSpread;
        this.indicatorRepository = indicatorRepository;
        this.spread = spread;
        this.listeners = new ArrayList<>();
        tickerService.addListener(this);
        addListener((IndicatorListener) dltService);
    }

    @Override
    public List<Indicator> getIndicators() {
        return indicators;
    }

    @Override
    public void updateIndicators(Map<CurrencyPair, KrakenTicker> tickers) {
        BigDecimal btcusdRate = currencyService.getExchangeRate(CurrencyPair.BTC_USD);
        BigDecimal btceurRate = currencyService.getExchangeRate(CurrencyPair.BTC_EUR);
        BigDecimal eurusdRate = currencyService.getExchangeRate(CurrencyPair.EUR_USD);

        if (btcusdRate == null || btceurRate == null || eurusdRate == null) {
            LOGGER.error("could not update indicators. exchange rate null");
            return;
        }

        BigDecimal center = dltService.isInitialized() ? dltService.getCenter() : BigDecimal.ZERO;
        BigDecimal forwardFloatingSpread = center.add(this.floatingSpread);
        BigDecimal backwardFloatingSpread = this.floatingSpread.subtract(center);

        List<Indicator> newIndicators = new ArrayList<>();

        for (Currency crypto : CurrencyService.CRYPTOS) {
            Indicator eurUsdIndicator = new Indicator(), usdEurIndicator = new Indicator();

            CurrencyPair cryptoEurPair = new CurrencyPair(crypto, Currency.EUR);
            CurrencyPair cryptoUsdPair = new CurrencyPair(crypto, Currency.USD);

            KrakenTicker cryptoEurTicker = tickers.get(cryptoEurPair);
            KrakenTicker cryptoUsdTicker = tickers.get(cryptoUsdPair);

            eurUsdIndicator.setCrypto(crypto.toString());
            eurUsdIndicator.setForward(false);
            eurUsdIndicator.setEntryPair(CurrencyService.getStringFromCurrencyPair(cryptoEurPair));
            eurUsdIndicator.setExitPair(CurrencyService.getStringFromCurrencyPair(cryptoUsdPair));
            eurUsdIndicator.setEntryPrice(cryptoEurTicker.getAsk().getPrice());
            eurUsdIndicator.setEntryBestBid(cryptoEurTicker.getBid().getPrice());
            eurUsdIndicator.setExitPrice(cryptoUsdTicker.getBid().getPrice());
            eurUsdIndicator.setExchangeRate(eurusdRate);
            eurUsdIndicator.setValues(backwardFloatingSpread);
            eurUsdIndicator.setSpread(spread);


            usdEurIndicator.setCrypto(crypto.toString());
            usdEurIndicator.setForward(true);
            usdEurIndicator.setEntryPair(CurrencyService.getStringFromCurrencyPair(cryptoUsdPair));
            usdEurIndicator.setExitPair(CurrencyService.getStringFromCurrencyPair(cryptoEurPair));
            usdEurIndicator.setEntryPrice(cryptoUsdTicker.getAsk().getPrice());
            usdEurIndicator.setEntryBestBid(cryptoUsdTicker.getBid().getPrice());
            usdEurIndicator.setExitPrice(cryptoEurTicker.getBid().getPrice());
            usdEurIndicator.setExchangeRate(BigDecimal.ONE.divide(eurusdRate, 8, BigDecimal.ROUND_HALF_UP));
            usdEurIndicator.setValues(forwardFloatingSpread);
            usdEurIndicator.setSpread(spread);

            newIndicators.add(eurUsdIndicator);
            newIndicators.add(usdEurIndicator);

            if (!crypto.equals(Currency.BTC)) {
                Indicator btcusdIndicator = new Indicator(), usdbtcIndicator = new Indicator();

                CurrencyPair cryptoBtcPair = new CurrencyPair(crypto, Currency.BTC);
                KrakenTicker cryptoBtcTicker = tickers.get(cryptoBtcPair);

                btcusdIndicator.setCrypto(crypto.toString());
                btcusdIndicator.setForward(false);
                btcusdIndicator.setEntryPair(CurrencyService.getStringFromCurrencyPair(cryptoBtcPair));
                btcusdIndicator.setExitPair(CurrencyService.getStringFromCurrencyPair(cryptoUsdPair));
                btcusdIndicator.setEntryPrice(cryptoBtcTicker.getAsk().getPrice());
                btcusdIndicator.setEntryBestBid(cryptoBtcTicker.getBid().getPrice());
                btcusdIndicator.setExitPrice(cryptoUsdTicker.getBid().getPrice());
                btcusdIndicator.setExchangeRate(btcusdRate);
                btcusdIndicator.setValues(backwardFloatingSpread);
                btcusdIndicator.setSpread(spread);

                usdbtcIndicator.setCrypto(crypto.toString());
                usdbtcIndicator.setForward(true);
                usdbtcIndicator.setEntryPair(CurrencyService.getStringFromCurrencyPair(cryptoUsdPair));
                usdbtcIndicator.setExitPair(CurrencyService.getStringFromCurrencyPair(cryptoBtcPair));
                usdbtcIndicator.setEntryPrice(cryptoUsdTicker.getAsk().getPrice());
                usdbtcIndicator.setEntryBestBid(cryptoUsdTicker.getBid().getPrice());
                usdbtcIndicator.setExitPrice(cryptoBtcTicker.getBid().getPrice());
                usdbtcIndicator.setExchangeRate(BigDecimal.ONE.divide(btcusdRate, 8, BigDecimal.ROUND_HALF_UP));
                usdbtcIndicator.setValues(forwardFloatingSpread);
                usdbtcIndicator.setSpread(spread);

                newIndicators.add(btcusdIndicator);
                newIndicators.add(usdbtcIndicator);

                Indicator btceurIndicator = new Indicator(), eurbtcIndicator = new Indicator();

                btceurIndicator.setCrypto(crypto.toString());
                btceurIndicator.setForward(false);
                btceurIndicator.setEntryPair(CurrencyService.getStringFromCurrencyPair(cryptoBtcPair));
                btceurIndicator.setExitPair(CurrencyService.getStringFromCurrencyPair(cryptoEurPair));
                btceurIndicator.setEntryPrice(cryptoBtcTicker.getAsk().getPrice());
                btceurIndicator.setEntryBestBid(cryptoBtcTicker.getBid().getPrice());
                btceurIndicator.setExitPrice(cryptoEurTicker.getBid().getPrice());
                btceurIndicator.setExchangeRate(btceurRate);
                btceurIndicator.setValues(backwardFloatingSpread);
                btceurIndicator.setSpread(spread);

                eurbtcIndicator.setCrypto(crypto.toString());
                eurbtcIndicator.setForward(true);
                eurbtcIndicator.setEntryPair(CurrencyService.getStringFromCurrencyPair(cryptoEurPair));
                eurbtcIndicator.setExitPair(CurrencyService.getStringFromCurrencyPair(cryptoBtcPair));
                eurbtcIndicator.setEntryPrice(cryptoEurTicker.getAsk().getPrice());
                eurbtcIndicator.setEntryBestBid(cryptoEurTicker.getBid().getPrice());
                eurbtcIndicator.setExitPrice(cryptoBtcTicker.getBid().getPrice());
                eurbtcIndicator.setExchangeRate(BigDecimal.ONE.divide(btceurRate, 8, BigDecimal.ROUND_HALF_UP));
                eurbtcIndicator.setValues(forwardFloatingSpread);
                eurbtcIndicator.setSpread(spread);

                newIndicators.add(btceurIndicator);
                newIndicators.add(eurbtcIndicator);
            }
        }
        this.indicators = newIndicators;
        indicatorRepository.saveAll(this.indicators);
        notifyListeners();
    }

    @Override
    public Indicator getIndicator(CurrencyPair entry, CurrencyPair exit) {
        String entryPair = CurrencyService.getStringFromCurrencyPair(entry);
        String exitPair = CurrencyService.getStringFromCurrencyPair(exit);
        for (Indicator indicator : indicators) {
            if (indicator.getEntryPair().equals(entryPair) && indicator.getExitPair().equals(exitPair))
                return indicator;
        }
        LOGGER.error("indicator with entry and exit pairs " + entry.toString() + " -> " + exit.toString() + " not found");
        return null;
    }

    @Override
    public void addListener(IndicatorListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void notifyListeners() {
        for (IndicatorListener listener : listeners) {
            listener.indicatorsUpdated(indicators);
        }
    }

    @Override
    public void tickersUpdated(Map<CurrencyPair, KrakenTicker> tickers) {
        updateIndicators(tickers);
    }
}
