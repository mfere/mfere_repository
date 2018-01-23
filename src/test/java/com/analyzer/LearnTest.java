package com.analyzer;

import com.analyzer.constants.*;
import com.analyzer.learner.LearnerRequestForm;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.analyzer.TestConstants.NETWORK_PATH;
import static org.junit.Assert.assertEquals;

public class LearnTest extends ApplicationTest {

    @Test
    public void testLearnDaily() throws Exception {
        LearnerRequestForm form = createBaseLearnerRequestForm();
        form.setBatchNumber(50);
        form.setLearningRate(0.001);
        form.setStopCondition(StopConditionType.LEAST_ERROR_LAST_100.name());
        form.setName("D_USD_JPY");
        form.setInstrument(InstrumentValue.USD_JPY.name());
        checkLearn(form);
    }

    @Test
    public void testLearnHourly() throws Exception {
        LearnerRequestForm form = createBaseLearnerRequestForm();
        form.setName("H_EUR_USD");
        form.setGranularity(GranularityType.H1.name());
        form.setStrategy(StrategyType.BS_TAKE_PROFIT_001_24.name());
        checkLearn(form);
    }

    @Test
    public void testLearnAll() throws Exception {
        LearnerRequestForm form = createBaseLearnerRequestForm();

        form.setName("D_USD_JPY");
        form.setInstrument(InstrumentValue.USD_JPY.name());
        checkLearn(form);

        form.setName("D_USD_CHF");
        form.setInstrument(InstrumentValue.USD_CHF.name());
        checkLearn(form);

        form.setName("D_EUR_USD");
        form.setInstrument(InstrumentValue.EUR_USD.name());
        checkLearn(form);

        form.setName("D_GBP_USD");
        form.setInstrument(InstrumentValue.GBP_USD.name());
        checkLearn(form);
    }

    @Test
    public void testLearnRelative() throws Exception {
        LearnerRequestForm form = createBaseLearnerRequestForm();
        form.setNormalizer(NormalizerType.STANDARD.name());
        form.setIndicators(getRelativeIndicators());
        form.setStrategy(StrategyType.BS_TAKE_PROFIT_005_24.name());

        ResponseEntity<String> response = template.postForEntity(
                learnURI, form, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ok", response.getBody());
    }

    @Test
    public void testConvergance() throws Exception {
        LearnerRequestForm form = createBaseLearnerRequestForm();
        form.setIndicators(getRelativeIndicators());
        form.setTrainFromDate("2010-01-04 00:00:00");
        form.setTrainToDate("2010-01-31 00:00:00");
        form.setName("D_EUR_USD");
        form.setNetworkConfiguration(readNetworkConfiguration("5layerNetwork"));
        form.setBatchNumber(1);
        form.setLearningRate(0.00001);
        form.setNormalizer(NormalizerType.STANDARD.name());
        form.setStopCondition(StopConditionType.FIXED_EPOC_LENGTH_10000.name());
        checkLearn(form);
    }

    private String readNetworkConfiguration(String networkName)
            throws IOException {
        byte[] encoded = Files.readAllBytes(
                Paths.get(properties.getProperty(NETWORK_PATH) + networkName + ".json"));
        return new String(encoded, Charset.forName("UTF-8"));
    }

    private void checkLearn(LearnerRequestForm form) throws IOException {
        ResponseEntity<String> response = template.postForEntity(
                learnURI, form, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ok", response.getBody());
    }

    public LearnerRequestForm createBaseLearnerRequestForm() throws Exception{
        LearnerRequestForm form = new LearnerRequestForm();
        form.setName("D_EUR_USD");
        form.setTrainFromDate("2010-01-04 00:00:00");
        form.setTrainToDate("2015-12-31 00:00:00");
        form.setTestFromDate("2016-01-04 00:00:00");
        form.setTestToDate("2017-11-01 00:00:00");
        form.setGranularity(GranularityType.D.name());
        form.setInstrument(InstrumentValue.EUR_USD.name());
        form.setStrategy(StrategyType.BS_TAKE_PROFIT_005_24.name());
        form.setStopCondition(StopConditionType.LEAST_ERROR_LAST_100.name());
        form.setIndicators(getAbsoluteIndicators());
        form.setBatchNumber(50);
        form.setLearningRate(0.001);
        form.setNormalizer(NormalizerType.MIN_MAX.name());
        form.setTestConvergance(false);
        form.setNetworkConfiguration(readNetworkConfiguration("3layerNetwork"));
        return form;
    }


}
