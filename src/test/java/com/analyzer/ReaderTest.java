package com.analyzer;

import com.analyzer.constants.GranularityType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.reader.ReadRequestForm;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ReaderTest extends ApplicationTest {

    public void checkReadResponse(ReadRequestForm form) {
        ResponseEntity<String> response;
        response = template.postForEntity(
                readURI, form, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ok", response.getBody());
    }

    @Test
    public void testReadAll() {
        ReadRequestForm form = new ReadRequestForm();
        form.setFromDate("2019-08-01 00:00:00");
        List<String> granularity = new ArrayList<>();
        //granularity.add(GranularityType.D.name());
        //granularity.add(GranularityType.H4.name());
        granularity.add(GranularityType.H1.name());
        form.setGranularity(granularity);

        for (InstrumentValue instrumentValue : InstrumentValue.values()) {
            form.setInstrument(instrumentValue.name());
            checkReadResponse(form);
        }
    }
}
