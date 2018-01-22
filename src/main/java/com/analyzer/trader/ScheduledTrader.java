package com.analyzer.trader;

import com.analyzer.client.OandaReaderClient;
import com.analyzer.client.OandaTradingClient;
import com.analyzer.constants.*;
import com.analyzer.enricher.IndicatorFactory;
import com.analyzer.constants.ActionType;
import com.analyzer.enricher.action.Action;
import com.analyzer.enricher.strategy.Strategy;
import com.analyzer.enricher.strategy.StrategyFactory;
import com.analyzer.learner.LearnerController;
import com.analyzer.model.FxIndicator;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import com.analyzer.reader.ReaderUtil;
import com.oanda.v20.instrument.Candlestick;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.AbstractDataSetNormalizer;
import org.nd4j.linalg.dataset.api.preprocessor.serializer.NormalizerSerializer;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sound.midi.Instrument;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import static org.junit.Assert.assertEquals;

@Component
public class ScheduledTrader {
    private static final Logger log = LoggerFactory.getLogger(LearnerController.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private String dailyTradingPropertiesPath;
    private String testTradingPropertiesPath;

    private final RawCandlestickRepository rawCandlestickRepository;
    private final OandaReaderClient oandaClient;

    private String url;
    private String token;

    public ScheduledTrader(
            @Value("${trader.configuration.daily}")String dailyTradingPropertiesPath,
            @Value("${trader.configuration.test}")String testTradingPropertiesPath,
            @Value("${oanda.url}") String url,
            @Value("${oanda.token}") String token,
            RawCandlestickRepository rawCandlestickRepository,
            OandaReaderClient oandaClient) {
        this.dailyTradingPropertiesPath = dailyTradingPropertiesPath;
        this.testTradingPropertiesPath = testTradingPropertiesPath;
        this.rawCandlestickRepository = rawCandlestickRepository;
        this.oandaClient = oandaClient;
        this.url = url;
        this.token = token;
    }

    //@Scheduled(cron = "0 * * * * *", zone = "UTC")
    public void testTransaction() throws Exception {
        if (testTradingPropertiesPath != null && !"".equals(testTradingPropertiesPath)) {
            elaborateOperations(testTradingPropertiesPath);
        }
    }

    @Scheduled(cron = "0 2 0 * * MON-FRI", zone = "UTC")
    public void dailyTransaction() throws Exception {
        if (dailyTradingPropertiesPath != null && !"".equals(dailyTradingPropertiesPath)) {
            elaborateOperations(dailyTradingPropertiesPath);
        }
    }

    public void elaborateOperations(String path) throws Exception {
        log.info("The time is now {}", dateFormat.format(new Date()));

        // Read the files in daily directory
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(path);

        // For each instrument retriew candlestick to elaborate for prediction
        Map<InstrumentValue, List<RawCandlestick>> instrumentMap = new HashMap<>();
        for (Resource resource : resources) {
            log.info("Reading trading settings file: " + resource.getFilename());
            PropertiesConfiguration config = new PropertiesConfiguration(resource.getFile());
            System.out.println("Working Directory = " +
                    System.getProperty("user.dir"));
            GranularityType granularity = GranularityType.D;
            InstrumentValue instrument = InstrumentValue.valueOf(config.getString("trade.instrument"));
            List<RawCandlestick> rawCandlestickList = getLatestRawCandlesticks(granularity, instrument);
            instrumentMap.put(instrument, rawCandlestickList);
        }

        // Try to do trade
        for (Resource resource : resources) {
            PropertiesConfiguration config = new PropertiesConfiguration(resource.getFile());
            InstrumentValue instrument = InstrumentValue.valueOf(config.getString("trade.instrument"));
            StrategyType strategy = StrategyType.valueOf(config.getString("trade.strategy"));
            checkForTrade(instrument, strategy, config, instrumentMap.get(instrument));
        }
    }

    public List<RawCandlestick> getLatestRawCandlesticks(GranularityType granularity, InstrumentValue instrument) throws Exception {
        // Read and save the last candlesticks
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        Instant lastCandleInstant = calendar.toInstant();

        calendar.add(Calendar.DAY_OF_MONTH, -200);
        Instant prevInstant = calendar.toInstant();

        // Get latest candlestick and add to db


        List<Candlestick> instrumentCandles = oandaClient.getInstrumentCandles(
                lastCandleInstant, null,
                granularity, instrument);
        Candlestick lastCandle = instrumentCandles.get(instrumentCandles.size()-1);
        if (!lastCandle.getComplete()) {
            lastCandle = instrumentCandles.get(instrumentCandles.size()-2);
        }
        log.info("Predicting candle: "+ lastCandle);

        Instant lastInstant = Instant.parse(lastCandle.getTime());

        ReaderUtil.saveCandles(granularity, instrument, instrumentCandles, rawCandlestickRepository);

        // Calculate indicator starting from older date
        return ReaderUtil.getRawCandlesticks(
                prevInstant, lastInstant,
                granularity, instrument,
                rawCandlestickRepository);
    }

    public void checkForTrade(InstrumentValue instrument,
                              StrategyType strategyType,
                              PropertiesConfiguration config,
                              List<RawCandlestick> rawCandlestickList) throws Exception {


        // Create model from provided file
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(config.getString("model.path"));

        // Create normalizer if needed
        AbstractDataSetNormalizer normalizer = null;
        String normalizerPath = config.getString("model.normalizer.path",null);
        if (normalizerPath != null) {
            NormalizerSerializer serializer = NormalizerSerializer.getDefault();
            normalizer = serializer.restore(normalizerPath);
        }

        // Read indicators to use
        String indicatorPath = config.getString("model.indicators.path");
        FileInputStream fis = new FileInputStream(indicatorPath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        List<String> indicators = (List<String>) ois.readObject();
        ois.close();

        IndicatorFactory indicatorFactory = new IndicatorFactory(rawCandlestickList);
        RawCandlestick rawCandlestick;
        INDArray input = Nd4j.zeros(indicators.size());
        int column = 0;
        for (int i = 0; i < rawCandlestickList.size(); i++){
            rawCandlestick = rawCandlestickList.get(i);

            for (String indicatorName : indicators) {
                FxIndicator indicator = new FxIndicator(indicatorName,
                        indicatorFactory.getIndicatorValue(IndicatorType.valueOf(indicatorName), i));
                rawCandlestick.addIndicator(indicator);
                // If last candlestick, create input array
                if (i == rawCandlestickList.size() -1) {
                    input.putScalar(0,column, indicator.getValue());
                    column++;
                }
            }
        }

        // Normalize iterator if needed
        if (normalizer != null) {
            normalizer.transform(input);
        }

        // Use model to predict last data
        INDArray prediction = model.output(input,false);
        log.info("predicted probabilities per label s: " + prediction);

        Strategy actionStrategy = StrategyFactory.getStrategy(strategyType);
        Action action = actionStrategy.getPredictedAction(
                instrument,
                prediction,
                config.getInt("trade.amount"),
                config.getDouble("trade.probability.treshold")
        );

        // Place order based on result and reward function
        if (ActionType.SELL == action.getType() || ActionType.BUY == action.getType()) {
            // Retrieve oanda account data
            OandaTradingClient client = new OandaTradingClient(url, token, config.getString("trade.oanda.account_id"));
            client.doAction(action);
        } else {
            log.info("Nothing to do");
        }
    }

}
