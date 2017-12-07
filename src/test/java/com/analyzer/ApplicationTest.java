package com.analyzer;

import com.analyzer.constants.GranularityValue;
import com.analyzer.constants.IndicatorValue;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.constants.RewardFunctionValue;
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
	public void testReadAllOanda() throws Exception {
		ReadRequestForm form = new ReadRequestForm();
		form.setFromDate("2010-01-01 00:00:00");
		List<String> granularity = new ArrayList<>();
		granularity.add(GranularityValue.D.getName());
		granularity.add(GranularityValue.H4.getName());
		granularity.add(GranularityValue.H1.getName());
		form.setGranularity(granularity);
		form.setInstrument(InstrumentValue.EUR_USD.name());

		ResponseEntity<String> response = template.postForEntity(
				readURI, form, String.class);
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(response.getBody(),"ok");
	}

		@Test
		public void testReadFromOanda() throws Exception {
			ReadRequestForm form = new ReadRequestForm();
			form.setFromDate("2013-01-01 00:00:00");
			form.setToDate("2014-01-01 00:00:00");
			List<String> granularity = new ArrayList<>();
			granularity.add(GranularityValue.D.getName());
			granularity.add(GranularityValue.H12.getName());
			form.setGranularity(granularity);
			form.setInstrument(InstrumentValue.USD_CHF.name());

			ResponseEntity<String> response = template.postForEntity(
					readURI, form, String.class);
			assertEquals(response.getStatusCode(), HttpStatus.OK);
			assertEquals(response.getBody(),"ok");

		}

		@Test
		public void testRawCandlestick() {
			String datetime = "2016-01-04T00:00:00Z";
			String granularity = GranularityValue.D.getName();
			String instrument = InstrumentValue.EUR_USD.getName();

			ResponseEntity<RawCandlestick> response = template.getForEntity(
					baseUrl + "/rawcandlestick/"
							+ "/"+ datetime
							+ "/"+ granularity
							+ "/"+ instrument
					,
					RawCandlestick.class);
			assertEquals(response.getStatusCode(), HttpStatus.OK);;
			assertNotNull(response.getBody());
			assertEquals(response.getBody().getRawCandlestickKey().getGranularity(), granularity);
			assertEquals(response.getBody().getRawCandlestickKey().getDateTime().toString(), datetime);
			assertNotNull(response.getBody().getMidRawCandlestickData().getOpen());
		}

	@Test
	public void testEnrich() throws Exception {
		EnrichRequestForm form = new EnrichRequestForm();
		form.setFromDate("2010-01-04 00:00:00");
		form.setToDate("2017-11-015 00:00:00");
		form.setGranularity(GranularityValue.D.getName());
		form.setInstrument(InstrumentValue.EUR_USD.name());
		List<String> indicators = new ArrayList<>();
		indicators.add(IndicatorValue.STANDARD_MACD.getName());
		List<String> rewardFunctions = new ArrayList<>();
		rewardFunctions.add(RewardFunctionValue.BS_TAKE_PROFIT_005_24.getName());
        rewardFunctions.add(RewardFunctionValue.B_TAKE_PROFIT_001_24.getName());
		rewardFunctions.add(RewardFunctionValue.S_TAKE_PROFIT_001_24.getName());
		form.setIndicators(indicators);
		form.setRewardFunctions(rewardFunctions);

		ResponseEntity<String> response = template.postForEntity(
				enrichURI, form, String.class);
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(response.getBody(),"ok");

		ResponseEntity<RawCandlestick> rawCandlestickResponse = template.getForEntity(
				baseUrl + "/rawcandlestick/"
						+ "/"+ "2017-08-01T00:00:00Z"
						+ "/"+ form.getGranularity()
						+ "/"+ form.getInstrument()
				,
				RawCandlestick.class);
		assertEquals(rawCandlestickResponse.getStatusCode(), HttpStatus.OK);;
		assertNotNull(rawCandlestickResponse.getBody());
		assertEquals(rawCandlestickResponse.getBody().getRawCandlestickKey().getGranularity(), form.getGranularity());
		assertEquals(rawCandlestickResponse.getBody().getRawCandlestickKey().getDateTime().toString(), "2017-08-01T00:00:00Z");
		assertNotNull(rawCandlestickResponse.getBody().getFxIndicators());
		FxIndicator[] fxIndicators = rawCandlestickResponse.getBody().getFxIndicators();
		assertTrue("rawCandlestickResponse should have indicators after add",
				fxIndicators.length > 0);
		boolean hasAddedIndicator= false;
		for (FxIndicator fxIndicator: fxIndicators) {
			assertNotNull(fxIndicator.getName());
			assertNotNull(fxIndicator.getValue());
			if (fxIndicator.getName().equals(indicators.get(0))) {
				hasAddedIndicator = true;
			}
		}
		assertTrue("rawCandlestickResponse should have new added indicator", hasAddedIndicator);

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

	@Test
	public void testLearn() throws Exception {
		LearnerRequestForm form = new LearnerRequestForm();
		form.setTrainFromDate("2010-01-04 00:00:00");
		form.setTrainToDate("2015-12-31 00:00:00");
		form.setTestFromDate("2016-01-04 00:00:00");
		form.setTestToDate("2017-11-01 00:00:00");
		form.setGranularity(GranularityValue.D.getName());
		form.setInstrument(InstrumentValue.EUR_USD.name());
		form.setNetworkConfiguration(readNetworkConfiguration("baseNetwork"));
		form.setLearningRate(0.1);
		List<String> indicators = new ArrayList<>();
		indicators.add(IndicatorValue.STANDARD_MACD.getName());
		form.setIndicators(indicators);
		form.setRewardFunction(RewardFunctionValue.BS_TAKE_PROFIT_005_24.getName());

		ResponseEntity<String> response = template.postForEntity(
				learnURI, form, String.class);
		assertEquals(response.getStatusCode(), HttpStatus.OK);
		assertEquals(response.getBody(),"ok");
	}

	private String readNetworkConfiguration(String networkName)
			throws IOException
	{
		byte[] encoded = Files.readAllBytes(
		        Paths.get(properties.getProperty(NETWORK_PATH)+networkName+".json"));
		return new String(encoded, Charset.forName("UTF-8"));
	}

}
