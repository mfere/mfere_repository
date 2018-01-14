package com.analyzer.trader;

import com.analyzer.client.OandaReaderClient;
import com.analyzer.constants.GranularityValue;
import com.analyzer.constants.IndicatorValue;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.enricher.IndicatorFactory;
import com.analyzer.learner.LearnerController;
import com.analyzer.model.FxIndicator;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import com.analyzer.reader.ReaderUtil;
import com.oanda.v20.instrument.Candlestick;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.dataset.api.preprocessor.Normalizer;
import org.nd4j.linalg.dataset.api.preprocessor.serializer.NormalizerSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import static org.junit.Assert.assertEquals;

@Component
public class ScheduledTrader {
    private static final Logger log = LoggerFactory.getLogger(LearnerController.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private String dailyTradingPropertiesPath;

    private final RawCandlestickRepository rawCandlestickRepository;
    private final OandaReaderClient oandaClient;

    public ScheduledTrader(
            @Value("${trader.configuration.daily}")String dailyTradingPropertiesPath,
            RawCandlestickRepository rawCandlestickRepository,
            OandaReaderClient oandaClient) {
        this.dailyTradingPropertiesPath = dailyTradingPropertiesPath;
        this.rawCandlestickRepository = rawCandlestickRepository;
        this.oandaClient = oandaClient;
    }

    //@Scheduled(cron = "5 0 * * * MON-FRI", zone = "UTC")
    @Scheduled(cron = "* * * * * *")
    public void reportCurrentTime() throws Exception {
        if (dailyTradingPropertiesPath != null && !"".equals(dailyTradingPropertiesPath)) {
            log.info("The time is now {}", dateFormat.format(new Date()));

            // Read the files in daily directory
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(dailyTradingPropertiesPath);

            // For every file
            for (Resource resource : resources) {
                log.info("Reading trading settings file: " + resource.getFilename());
                PropertiesConfiguration config = new PropertiesConfiguration(resource.getFile());
                System.out.println("Working Directory = " +
                        System.getProperty("user.dir"));

                // Create model from provided file
                MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(config.getString("model.path"));

                // Create normalizer if needed
                Normalizer normalizer = null;
                String normalizerPath = config.getString("model.normalizer.path",null);
                if (normalizerPath != null) {
                    NormalizerSerializer serializer = NormalizerSerializer.getDefault();
                    normalizer = serializer.restore(normalizerPath);
                }

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
                GranularityValue granularity = GranularityValue.D;
                InstrumentValue instrument = InstrumentValue.valueOf(config.getString("trade.instrument"));

                List<Candlestick> instrumentCandles = oandaClient.getInstrumentCandles(
                        lastCandleInstant, null,
                        granularity, instrument);
                Candlestick lastCandle = instrumentCandles.get(instrumentCandles.size()-1);
                Instant lastInstant = Instant.parse(lastCandle.getTime());

                ReaderUtil.saveCandles(granularity, instrument, instrumentCandles, rawCandlestickRepository);

                // Calculate indicator starting from older date
                List<IndicatorValue> indicatorList = new ArrayList<>(Arrays.asList(IndicatorValue.values()));
                List<RawCandlestick> rawCandlestickList = ReaderUtil.getRawCandlesticks(
                        prevInstant, lastInstant,
                        granularity, instrument,
                        rawCandlestickRepository);

                IndicatorFactory indicatorFactory = new IndicatorFactory(rawCandlestickList);
                RawCandlestick rawCandlestick;
                for (int i = 0; i < rawCandlestickList.size(); i++){
                    rawCandlestick = rawCandlestickList.get(i);
                    for (IndicatorValue indicatorName : indicatorList) {
                        rawCandlestick.addIndicator(
                                new FxIndicator(indicatorName.name(), indicatorFactory.getIndicatorValue(indicatorName, i))
                        );
                    }
                }

                // Read file and create csv
                File tmpTestFile = File.createTempFile("test_"+new Date().getTime(), ".csv");
                FileWriter writer = new FileWriter(tmpTestFile);
                PrintWriter printWriter = new PrintWriter(writer);

                rawCandlestick = rawCandlestickRepository.findOne(
                        lastInstant,
                        granularity,
                        instrument);

                log.info("saved test temporary file: "+tmpTestFile.getAbsolutePath());


                // Create iterator from csv

                // Normalize iterator if needed

                // Use model to predict last data

                // Retrieve oanda account data

                // Place order based on result and reward function


            }
        }

    }
}
