package com.analyzer;

import com.analyzer.constants.*;
import com.analyzer.enricher.EnrichRequestForm;
import com.analyzer.learner.LearnerRequestForm;
import com.analyzer.model.FxIndicator;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.RewardFunction;
import com.analyzer.reader.ReadRequestForm;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.analyzer.TestConstants.NETWORK_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
public class ApplicationTest {

		@LocalServerPort
		private int port;

		private String baseUrl;

		private URI readURI;

		private URI enrichURI;

		private URI learnURI;

		@Autowired
		private TestRestTemplate template;

    	private PropertiesConfiguration properties;

		@Before
		public void setUp() throws Exception {
			properties = new PropertiesConfiguration("application.properties");
			this.baseUrl = "http://localhost:" + port;
			this.readURI = new URI(baseUrl + "/read");
			this.enrichURI = new URI(baseUrl + "/enrich");
			this.learnURI = new URI(baseUrl + "/learn");
		}

	@Test
	public void testLearn() throws Exception {
		LearnerRequestForm form = new LearnerRequestForm();
		form.setName("D_EUR_USD");
		form.setGranularity(GranularityValue.H1.name());
		form.setInstrument(InstrumentValue.EUR_USD.name());
		form.setRewardFunction(RewardFunctionValue.BS_TAKE_PROFIT_005_24.name());
		test3LayerLearn(form);
	}

	@Test
	public void testAll() throws Exception{
			testReadAll();
			testEnrichAll();
			testLearnAll();
	}

	@Test
	public void testReadAll() throws Exception {
		ReadRequestForm form = new ReadRequestForm();
		form.setFromDate("2010-01-01 00:00:00");
		List<String> granularity = new ArrayList<>();
		granularity.add(GranularityValue.D.name());
		granularity.add(GranularityValue.H4.name());
		granularity.add(GranularityValue.H1.name());
		form.setGranularity(granularity);
		form.setInstrument(InstrumentValue.USD_JPY.name());

		checkReadResponse(form);

		form.setInstrument(InstrumentValue.USD_CHF.name());

		checkReadResponse(form);

		form.setInstrument(InstrumentValue.EUR_USD.name());

		checkReadResponse(form);

		form.setInstrument(InstrumentValue.GBP_USD.name());

		checkReadResponse(form);

	}
		@Test
		public void testRawCandlestick() {
			String datetime = "2016-01-04T00:00:00Z";
			String granularity = GranularityValue.D.name();
			String instrument = InstrumentValue.USD_CHF.name();

			ResponseEntity<RawCandlestick> response = template.getForEntity(
					baseUrl + "/rawcandlestick/"
							+ "/"+ datetime
							+ "/"+ granularity
							+ "/"+ instrument
					,
					RawCandlestick.class);
			assertEquals(response.getStatusCode(), HttpStatus.OK);
			assertNotNull(response.getBody());
			assertEquals(response.getBody().getRawCandlestickKey().getGranularity(), granularity);
			assertEquals(response.getBody().getRawCandlestickKey().getDateTime().toString(), datetime);
			assertNotNull(response.getBody().getMidRawCandlestickData().getOpen());
		}

	@Test
	public void testEnrichAll() throws Exception {
		EnrichRequestForm enrichRequestForm = new EnrichRequestForm();
		enrichRequestForm.setFromDate("2010-01-04 00:00:00");
		enrichRequestForm.setToDate("2018-01-04 00:00:00");
		enrichRequestForm.setGranularity(GranularityValue.D.name());
		List<String> indicators = new ArrayList<>();
		List<IndicatorValue> indicatorValueList = new ArrayList<>(Arrays.asList(IndicatorValue.values()));
		for (IndicatorValue indicatorValue : indicatorValueList) {
			indicators.add(indicatorValue.name());
		}
		List<String> rewardFunctions = new ArrayList<>();
		List<RewardFunctionValue> rewardFunctionValueList= new ArrayList<>(Arrays.asList(RewardFunctionValue.values()));
		for (RewardFunctionValue rewardFunctionValue : rewardFunctionValueList) {
			rewardFunctions.add(rewardFunctionValue.name());
		}
		enrichRequestForm.setIndicators(indicators);
		enrichRequestForm.setRewardFunctions(rewardFunctions);

		enrichRequestForm.setInstrument(InstrumentValue.USD_CHF.name());
		checkEnrichResponse(enrichRequestForm, indicators, rewardFunctions);

		enrichRequestForm.setInstrument(InstrumentValue.EUR_USD.name());
		checkEnrichResponse(enrichRequestForm, indicators, rewardFunctions);

		enrichRequestForm.setInstrument(InstrumentValue.GBP_USD.name());
		checkEnrichResponse(enrichRequestForm, indicators, rewardFunctions);

		enrichRequestForm.setInstrument(InstrumentValue.USD_JPY.name());
		checkEnrichResponse(enrichRequestForm, indicators, rewardFunctions);

	}

	@Test
	public void testLearnAll() throws Exception {
		LearnerRequestForm form = new LearnerRequestForm();
		form.setGranularity(GranularityValue.D.name());
		form.setRewardFunction(RewardFunctionValue.BS_TAKE_PROFIT_005_24.name());
		form.setName("D_USD_CHF");
		form.setInstrument(InstrumentValue.USD_CHF.name());
		test3LayerLearn(form);

		form.setName("D_EUR_USD");
		form.setInstrument(InstrumentValue.EUR_USD.name());
		test3LayerLearn(form);

		form.setName("D_GBP_USD");
		form.setInstrument(InstrumentValue.GBP_USD.name());
		test3LayerLearn(form);

		form.setName("D_USD_JPY");
		form.setInstrument(InstrumentValue.USD_JPY.name());
		test3LayerLearn(form);
	}



	@Test
	public void testLearnRelative() throws Exception {
		LearnerRequestForm form = new LearnerRequestForm();
		form.setTrainFromDate("2010-01-04 00:00:00");
		form.setTrainToDate("2015-12-31 00:00:00");
		form.setTestFromDate("2016-01-04 00:00:00");
		form.setTestToDate("2017-11-01 00:00:00");
		form.setGranularity(GranularityValue.D.name());
		form.setInstrument(InstrumentValue.EUR_USD.name());
		form.setNetworkConfiguration(readNetworkConfiguration("baseNetwork"));
		form.setLearningRate(0.01);
		form.setStopCondition(StopConditionValue.FIXED_EPOC_LENGTH_2000.name());
		form.setNormalizer(NormalizerValue.STANDARD.name());
		List<String> indicators = new ArrayList<>();

		indicators.add(IndicatorValue.MACD_RAW.name());
		indicators.add(IndicatorValue.VOLUME_RAW.name());
		indicators.add(IndicatorValue.SMA_5_CLOSE_DIFF.name());
		indicators.add(IndicatorValue.SMA_10_CLOSE_DIFF.name());
		indicators.add(IndicatorValue.SMA_50_CLOSE_DIFF.name());
		indicators.add(IndicatorValue.SMA_100_CLOSE_DIFF.name());
		indicators.add(IndicatorValue.SMA_200_CLOSE_DIFF.name());
		indicators.add(IndicatorValue.RSI_RAW.name());
		indicators.add(IndicatorValue.BOLLINGER_BAND_WIDTH_RAW.name());
		indicators.add(IndicatorValue.BOLLINGER_BAND_LOWER_DIFF.name());
		indicators.add(IndicatorValue.BOLLINGER_BAND_UPPER_DIFF.name());
		indicators.add(IndicatorValue.BOLLINGER_BAND_MIDDLE_DIFF.name());
		form.setIndicators(indicators);
		form.setTestConvergance(false);
		form.setRewardFunction(RewardFunctionValue.BS_TAKE_PROFIT_005_24.name());

		ResponseEntity<String> response = template.postForEntity(
				learnURI, form, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("ok",response.getBody());
	}

	private String readNetworkConfiguration(String networkName)
			throws IOException
	{
		byte[] encoded = Files.readAllBytes(
		        Paths.get(properties.getProperty(NETWORK_PATH)+networkName+".json"));
		return new String(encoded, Charset.forName("UTF-8"));
	}

	public void checkReadResponse(ReadRequestForm form) {
		ResponseEntity<String> response;
		response = template.postForEntity(
				readURI, form, String.class);
		assertEquals( HttpStatus.OK, response.getStatusCode());
		assertEquals("ok",response.getBody());
	}

	public void checkEnrichResponse(
			EnrichRequestForm enrichRequestForm,
			List<String> indicators,
			List<String> rewardFunctions) {
		ResponseEntity<String> response;
		response = template.postForEntity(
				enrichURI, enrichRequestForm, String.class);
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(response.getBody(),"ok");

		ResponseEntity<RawCandlestick> rawCandlestickResponse = template.getForEntity(
				baseUrl + "/rawcandlestick/"
						+ "/"+ "2017-08-02T00:00:00Z"
						+ "/"+ enrichRequestForm.getGranularity()
						+ "/"+ enrichRequestForm.getInstrument()
				,
				RawCandlestick.class);
		assertEquals(HttpStatus.OK, rawCandlestickResponse.getStatusCode());
		assertNotNull(rawCandlestickResponse.getBody());
		assertEquals(rawCandlestickResponse.getBody().getRawCandlestickKey().getGranularity(), enrichRequestForm.getGranularity());
		assertEquals(rawCandlestickResponse.getBody().getRawCandlestickKey().getDateTime().toString(), "2017-08-02T00:00:00Z");
		assertNotNull(rawCandlestickResponse.getBody().getFxIndicators());
		FxIndicator[] fxIndicators = rawCandlestickResponse.getBody().getFxIndicators();
		assertTrue("rawCandlestickResponse should have indicators after add",
				fxIndicators.length > 0);
		boolean hasAddedIndicator= false;
		/*for (FxIndicator fxIndicator: fxIndicators) {
			assertNotNull(fxIndicator.getName());
			//assertNotNull(fxIndicator.getValue());
			if (fxIndicator.getName().equals(indicators.get(0))) {
				hasAddedIndicator = true;
			}
		}
		assertTrue("rawCandlestickResponse should have new added indicator", hasAddedIndicator);
*/
		RewardFunction[] strategies = rawCandlestickResponse.getBody().getStrategies();
		assertTrue("rawCandlestickResponse should have strategies after add",
				strategies.length > 0);
		boolean hasAddedRewardFunction= false;
		for (RewardFunction rewardFunction: strategies) {
			assertNotNull(rewardFunction.getName());
			assertNotNull(rewardFunction.getValue());
			if (rewardFunction.getName().equals(rewardFunctions.get(0))) {
				hasAddedRewardFunction = true;
			}
		}
		assertTrue("rawCandlestickResponse should have new added rewardFunction", hasAddedRewardFunction);
	}

	public void test3LayerLearn(LearnerRequestForm form) throws IOException {
		form.setTrainFromDate("2010-01-04 00:00:00");
		form.setTrainToDate("2015-12-31 00:00:00");
		form.setTestFromDate("2016-01-04 00:00:00");
		form.setTestToDate("2017-11-01 00:00:00");
		form.setNetworkConfiguration(readNetworkConfiguration("3layerNetwork"));
		form.setLearningRate(0.001);
		form.setNormalizer(NormalizerValue.MIN_MAX.name());
		form.setStopCondition(StopConditionValue.LEAST_ERROR_LAST_100.name());
		List<String> indicators = new ArrayList<>();
		indicators.add(IndicatorValue.BULLISH_ENGULFING_CANDLE.name());
		indicators.add(IndicatorValue.BEARISH_ENGULFING_CANDLE.name());
		indicators.add(IndicatorValue.BULLISH_HARAM_CANDLE.name());
		indicators.add(IndicatorValue.BEARISH_HARAM_CANDLE.name());
		indicators.add(IndicatorValue.SMA_5_CLOSE_ABOVE_OR_BELOW.name());
		indicators.add(IndicatorValue.SMA_5_UPWARD_OR_DOWNWARD.name());
		indicators.add(IndicatorValue.SMA_10_CLOSE_ABOVE_OR_BELOW.name());
		indicators.add(IndicatorValue.SMA_10_UPWARD_OR_DOWNWARD.name());
		indicators.add(IndicatorValue.SMA_50_CLOSE_ABOVE_OR_BELOW.name());
		indicators.add(IndicatorValue.SMA_50_UPWARD_OR_DOWNWARD.name());
		indicators.add(IndicatorValue.MACD_div_positive_or_negative.name());
		//indicators.add(IndicatorValue.MACD_RAW.name());
		//indicators.add(IndicatorValue.VOLUME_RAW.name());

		indicators.add(IndicatorValue.RSI_OVER_BROUGHT_OR_SOLD.name());
		indicators.add(IndicatorValue.RSI_UPWARD_OR_DOWNWARD_SLOPING.name());

		indicators.add(IndicatorValue.STOCHASTIC_OSCILLATOR_K_ABOVE_OR_BELOW_D.name());

		indicators.add(IndicatorValue.STOCHASTIC_OSCILLATOR_KD_OVER_BROUGHT_OR_SOLD.name());
		indicators.add(IndicatorValue.STOCHASTIC_OSCILLATOR_K_UPWARD_OR_DOWNWARD_SLOPING.name());
		indicators.add(IndicatorValue.STOCHASTIC_OSCILLATOR_D_UPWARD_OR_DOWNWARD_SLOPING.name());

		indicators.add(IndicatorValue.BOLLINGER_BAND_UPPER_CLOSE_ABOVE_OR_BELOW.name());
		indicators.add(IndicatorValue.BOLLINGER_BAND_LOWER_CLOSE_ABOVE_OR_BELOW.name());
		indicators.add(IndicatorValue.BOLLINGER_BAND_EXPANDING_OR_CONTRACTING.name());

		//indicators.add(IndicatorValue.IS_YESTERDAY_HOLIDAY.name());
		//indicators.add(IndicatorValue.IS_TOMORROW_HOLIDAY.name());

		form.setIndicators(indicators);
		form.setTestConvergance(false);

		ResponseEntity<String> response = template.postForEntity(
				learnURI, form, String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("ok",response.getBody());
	}

}
