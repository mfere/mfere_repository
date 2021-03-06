package com.analyzer;

import com.analyzer.constants.GranularityType;
import com.analyzer.constants.IndicatorType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.constants.StrategyType;
import com.analyzer.enricher.EnrichRequestForm;
import com.analyzer.model.ActionStrategy;
import com.analyzer.model.FxIndicator;
import com.analyzer.model.RawCandlestick;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EnricherTest extends ApplicationTest {

    @Test
    public void testEnrichAbsolute() {
        EnrichRequestForm enrichRequestForm = createBaseEnrichForm();
        enrichRequestForm.setIndicators(getAbsoluteIndicators());
        enrichRequestForm.setInstrument(InstrumentValue.USD_CHF.name());
        checkEnrichResponse(enrichRequestForm);

        enrichRequestForm.setInstrument(InstrumentValue.EUR_USD.name());
        checkEnrichResponse(enrichRequestForm);

        enrichRequestForm.setInstrument(InstrumentValue.GBP_USD.name());
        checkEnrichResponse(enrichRequestForm);

        enrichRequestForm.setInstrument(InstrumentValue.USD_JPY.name());
        checkEnrichResponse(enrichRequestForm);
    }

    @Test
    public void testEnrichRelative() {
        EnrichRequestForm enrichRequestForm = createBaseEnrichForm();
        enrichRequestForm.setIndicators(getRelativeIndicators());
        enrichRequestForm.setInstrument(InstrumentValue.USD_CHF.name());
        checkEnrichResponse(enrichRequestForm);

        enrichRequestForm.setInstrument(InstrumentValue.EUR_USD.name());
        checkEnrichResponse(enrichRequestForm);

        enrichRequestForm.setInstrument(InstrumentValue.GBP_USD.name());
        checkEnrichResponse(enrichRequestForm);

        enrichRequestForm.setInstrument(InstrumentValue.USD_JPY.name());
        checkEnrichResponse(enrichRequestForm);
    }

    @Test
    public void testEnrichAll() {
        EnrichRequestForm enrichRequestForm = createBaseEnrichForm();
        enrichRequestForm.setGranularity(GranularityType.H1.name());
        for (InstrumentValue instrumentValue : InstrumentValue.values()) {
            enrichRequestForm.setInstrument(instrumentValue.name());
            checkEnrichResponse(enrichRequestForm);
        }
    }

    @Test
    public void testForBug() {
        EnrichRequestForm enrichRequestForm = new EnrichRequestForm();
        enrichRequestForm.setFromDate("2018-01-31 00:00:00");
        enrichRequestForm.setToDate("2018-01-31 00:00:00");
        enrichRequestForm.setGranularity(GranularityType.D.name());
        enrichRequestForm.setIndicators(new ArrayList<>());
        enrichRequestForm.setInstrument(InstrumentValue.GBP_USD.name());
        List<String> strategies = new ArrayList<>();
        strategies.add(StrategyType.BS_TAKE_PROFIT_005_24.name());
        enrichRequestForm.setStrategies(strategies);
        checkEnrichResponse(enrichRequestForm);

    }
    //O GBP 9:00 1.41990
    //L GBP 16:00  1.41590

    @Test
    public void testEnrichStrategy() {
        EnrichRequestForm enrichRequestForm = createBaseEnrichForm();
        enrichRequestForm.setIndicators(new ArrayList<>());
        for (InstrumentValue instrumentValue : InstrumentValue.values()) {
            enrichRequestForm.setInstrument(instrumentValue.name());
            checkEnrichResponse(enrichRequestForm);
        }

    }

    private EnrichRequestForm createBaseEnrichForm() {
        EnrichRequestForm enrichRequestForm = new EnrichRequestForm();
        enrichRequestForm.setFromDate("2010-01-04 06:00:00");
        enrichRequestForm.setToDate("2020-09-08 00:00:00");
        enrichRequestForm.setGranularity(GranularityType.H1.name());
        List<String> indicators = new ArrayList<>();
        List<IndicatorType> indicatorTypeList = new ArrayList<>(Arrays.asList(IndicatorType.values()));
        for (IndicatorType indicatorType : indicatorTypeList) {
            indicators.add(indicatorType.name());
        }
        List<String> strategies = new ArrayList<>();
        List<StrategyType> strategyTypeList = new ArrayList<>(Arrays.asList(StrategyType.values()));
        for (StrategyType strategyType : strategyTypeList) {
            strategies.add(strategyType.name());
        }
        enrichRequestForm.setIndicators(indicators);
        enrichRequestForm.setStrategies(strategies);

        enrichRequestForm.setInstrument(InstrumentValue.USD_CHF.name());
        return enrichRequestForm;
    }

    private void checkEnrichResponse(
            EnrichRequestForm enrichRequestForm) {
        ResponseEntity<String> response;
        List<String> indicators = enrichRequestForm.getIndicators();
        List<String> strategies = enrichRequestForm.getStrategies();
        response = template.postForEntity(
                enrichURI, enrichRequestForm, String.class);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody(), "ok");

        ResponseEntity<RawCandlestick> rawCandlestickResponse = template.getForEntity(
                baseUrl + "/rawcandlestick/"
                        + "/" + "2017-08-02T00:00:00Z"
                        + "/" + enrichRequestForm.getGranularity()
                        + "/" + enrichRequestForm.getInstrument()
                ,
                RawCandlestick.class);
        assertEquals(HttpStatus.OK, rawCandlestickResponse.getStatusCode());
        assertNotNull(rawCandlestickResponse.getBody());
        assertEquals(rawCandlestickResponse.getBody().getRawCandlestickKey().getGranularity(), enrichRequestForm.getGranularity());
        assertEquals(rawCandlestickResponse.getBody().getRawCandlestickKey().getDateTime().toString(), "2017-08-02T00:00:00Z");
        assertNotNull(rawCandlestickResponse.getBody().getFxIndicators());
        if (indicators.size() > 0) {
            FxIndicator[] fxIndicators = rawCandlestickResponse.getBody().getFxIndicators();
            assertTrue("rawCandlestickResponse should have indicators after add",
                    fxIndicators.length > 0);
            boolean hasAddedIndicator = false;
            for (FxIndicator fxIndicator : fxIndicators) {
                assertNotNull(fxIndicator.getName());
                //assertNotNull(fxIndicator.getValue());
                if (fxIndicator.getName().equals(indicators.get(indicators.size() - 1))) {
                    hasAddedIndicator = true;
                }
            }
            assertTrue("rawCandlestickResponse should have new added indicator", hasAddedIndicator);
        }
        if (strategies.size() > 0) {
            ActionStrategy[] actionStrategies = rawCandlestickResponse.getBody().getActionStrategies();
            assertTrue("rawCandlestickResponse should have actionStrategies after add",
                    actionStrategies.length > 0);
            boolean hasAddedStrategy = false;
            for (ActionStrategy actionStrategy : actionStrategies) {
                assertNotNull(actionStrategy.getActionTypeValue());
                assertNotNull(actionStrategy.getStrategyTypeValue());
                if (actionStrategy.getStrategyTypeValue().equals(strategies.get(0))) {
                    hasAddedStrategy = true;
                }
            }
            assertTrue("rawCandlestickResponse should have new added strategy", hasAddedStrategy);
        }
    }
}
