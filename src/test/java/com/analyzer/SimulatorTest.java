package com.analyzer;

import com.analyzer.constants.GranularityType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.reader.ReadRequestForm;
import com.analyzer.simulator.SimulationRequestForm;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SimulatorTest extends ApplicationTest {

    public void checkReadResponse(SimulationRequestForm form) {
        ResponseEntity<String> response;
        response = template.postForEntity(
                simulateURI, form, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ok", response.getBody());
    }

    @Test
    public void testSimulator() throws Exception {
        SimulationRequestForm form = new SimulationRequestForm();
        form.setFromDate("2017-01-04 00:00:00");
        form.setToDate("2017-06-04 00:00:00");
        checkReadResponse(form);
    }
}
