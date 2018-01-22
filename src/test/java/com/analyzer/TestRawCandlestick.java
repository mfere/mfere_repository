package com.analyzer;

import com.analyzer.constants.GranularityType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.model.RawCandlestick;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestRawCandlestick extends ApplicationTest {

    @Test
    public void testRawCandlestick() {
        String datetime = "2016-01-04T00:00:00Z";
        String granularity = GranularityType.D.name();
        String instrument = InstrumentValue.USD_CHF.name();

        ResponseEntity<RawCandlestick> response = template.getForEntity(
                baseUrl + "/rawcandlestick/"
                        + "/" + datetime
                        + "/" + granularity
                        + "/" + instrument
                ,
                RawCandlestick.class);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        assertEquals(response.getBody().getRawCandlestickKey().getGranularity(), granularity);
        assertEquals(response.getBody().getRawCandlestickKey().getDateTime().toString(), datetime);
        assertNotNull(response.getBody().getMidRawCandlestickData().getOpen());
    }
}
