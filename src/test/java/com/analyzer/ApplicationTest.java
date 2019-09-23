package com.analyzer;

import com.analyzer.configuration.NetworkCreator;
import com.analyzer.constants.*;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
public class ApplicationTest {

    public static final String NETWORK_NAME = "baseNetwork";

    @LocalServerPort
    protected int port;

    protected String baseUrl;

    protected URI readURI;

    protected URI simulateURI;

    protected URI enrichURI;

    protected URI learnURI;

    protected URI runDailyURI;

    @Autowired
    protected TestRestTemplate template;

    PropertiesConfiguration properties;

    @Before
    public void setUp() throws Exception {
        properties = new PropertiesConfiguration("application.properties");
        NetworkCreator networkCreator = new NetworkCreator();
        networkCreator.initProperties();
        networkCreator.createNetwork(NETWORK_NAME);
        this.baseUrl = "http://localhost:" + port;
        this.readURI = new URI(baseUrl + "/read");
        this.enrichURI = new URI(baseUrl + "/enrich");
        this.learnURI = new URI(baseUrl + "/learn");
        this.simulateURI = new URI(baseUrl + "/simulate");
        this.runDailyURI = new URI(baseUrl + "/runDaily");
    }

    @Test
    public void testAll() throws Exception {
        ReaderTest readerTest = new ReaderTest();
        readerTest.setUp();
        readerTest.testReadAll();
        EnricherTest enricherTest = new EnricherTest();
        enricherTest.setUp();
        enricherTest.testEnrichAll();
        LearnTest learnTest = new LearnTest();
        learnTest.setUp();
        learnTest.testLearnAll();
    }

    @Test
    public void testDaily() throws Exception {
        template.getForEntity(runDailyURI, String.class);
    }


    @NotNull
    List<String> getAbsoluteIndicators() {
        List<String> indicators = new ArrayList<>();
        indicators.add(IndicatorType.BULLISH_ENGULFING_CANDLE.name());
        indicators.add(IndicatorType.BEARISH_ENGULFING_CANDLE.name());
        indicators.add(IndicatorType.BULLISH_HARAM_CANDLE.name());
        indicators.add(IndicatorType.BEARISH_HARAM_CANDLE.name());
        indicators.add(IndicatorType.SMA_5_CLOSE_ABOVE_OR_BELOW.name());
        indicators.add(IndicatorType.SMA_5_UPWARD_OR_DOWNWARD.name());
        indicators.add(IndicatorType.SMA_10_CLOSE_ABOVE_OR_BELOW.name());
        indicators.add(IndicatorType.SMA_10_UPWARD_OR_DOWNWARD.name());
        indicators.add(IndicatorType.SMA_50_CLOSE_ABOVE_OR_BELOW.name());
        indicators.add(IndicatorType.SMA_50_UPWARD_OR_DOWNWARD.name());
        indicators.add(IndicatorType.SMA_200_CLOSE_ABOVE_OR_BELOW.name());
        indicators.add(IndicatorType.SMA_200_UPWARD_OR_DOWNWARD.name());
        indicators.add(IndicatorType.MACD_div_positive_or_negative.name());

        indicators.add(IndicatorType.RSI_OVER_BROUGHT_OR_SOLD.name());
        indicators.add(IndicatorType.RSI_UPWARD_OR_DOWNWARD_SLOPING.name());

        indicators.add(IndicatorType.STOCHASTIC_OSCILLATOR_K_ABOVE_OR_BELOW_D.name());

        indicators.add(IndicatorType.STOCHASTIC_OSCILLATOR_KD_OVER_BROUGHT_OR_SOLD.name());
        indicators.add(IndicatorType.STOCHASTIC_OSCILLATOR_K_UPWARD_OR_DOWNWARD_SLOPING.name());
        indicators.add(IndicatorType.STOCHASTIC_OSCILLATOR_D_UPWARD_OR_DOWNWARD_SLOPING.name());

        indicators.add(IndicatorType.BOLLINGER_BAND_UPPER_CLOSE_ABOVE_OR_BELOW.name());
        indicators.add(IndicatorType.BOLLINGER_BAND_LOWER_CLOSE_ABOVE_OR_BELOW.name());
        indicators.add(IndicatorType.BOLLINGER_BAND_EXPANDING_OR_CONTRACTING.name());

        //indicators.add(IndicatorType.IS_YESTERDAY_HOLIDAY.name());
        //indicators.add(IndicatorType.IS_TOMORROW_HOLIDAY.name());

        return indicators;
    }

    @NotNull
    public List<String> getRelativeIndicators() {
        List<String> indicators = new ArrayList<>();

        indicators.add(IndicatorType.MACD_RAW.name());
        indicators.add(IndicatorType.MACD_DIFF_WITH_PREVIOUS.name());
        indicators.add(IndicatorType.VOLUME_RAW.name());
        indicators.add(IndicatorType.SMA_5_DIFF_WITH_PREVIOUS.name());
        indicators.add(IndicatorType.SMA_10_DIFF_WITH_PREVIOUS.name());
        indicators.add(IndicatorType.SMA_50_DIFF_WITH_PREVIOUS.name());
        indicators.add(IndicatorType.SMA_100_DIFF_WITH_PREVIOUS.name());
        indicators.add(IndicatorType.SMA_200_DIFF_WITH_PREVIOUS.name());


        indicators.add(IndicatorType.RSI_RAW.name());
        indicators.add(IndicatorType.RSI_DIFF_WITH_PREVIOUS.name());
/*
        indicators.add(IndicatorType.BOLLINGER_BAND_LOWER_DIFF_WITH_PREVIOUS.name());
        indicators.add(IndicatorType.BOLLINGER_BAND_UPPER_DIFF_WITH_PREVIOUS.name());
        indicators.add(IndicatorType.BOLLINGER_BAND_MIDDLE_DIFF_WITH_PREVIOUS.name());
        indicators.add(IndicatorType.BOLLINGER_BAND_WIDTH_DIFF_WITH_PREVIOUS.name());

        indicators.add(IndicatorType.STOCHASTIC_OSCILLATOR_K_RAW.name());
        indicators.add(IndicatorType.STOCHASTIC_OSCILLATOR_D_RAW.name());

        indicators.add(IndicatorType.STOCHASTIC_OSCILLATOR_K_DIFF_WITH_PREVIOUS.name());
        indicators.add(IndicatorType.STOCHASTIC_OSCILLATOR_D_DIFF_WITH_PREVIOUS.name());

        indicators.add(IndicatorType.IS_YESTERDAY_HOLIDAY.name());
        indicators.add(IndicatorType.IS_TOMORROW_HOLIDAY.name());

        indicators.add(IndicatorType.SMA_5_CLOSE_DIFF.name());
        indicators.add(IndicatorType.SMA_10_CLOSE_DIFF.name());
        //indicators.add(IndicatorType.SMA_50_CLOSE_DIFF.name());
*/

        //indicators.add(IndicatorType.BOLLINGER_BAND_WIDTH_RAW.name());
        //indicators.add(IndicatorType.BOLLINGER_BAND_LOWER_DIFF.name());
        //indicators.add(IndicatorType.BOLLINGER_BAND_UPPER_DIFF.name());
        //indicators.add(IndicatorType.BOLLINGER_BAND_MIDDLE_DIFF.name());

        return indicators;
    }

}
