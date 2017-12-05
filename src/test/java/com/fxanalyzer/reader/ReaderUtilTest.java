package com.fxanalyzer.reader;

import com.fxanalyzer.enricher.EnrichRequestForm;
import com.oanda.v20.primitives.DateTime;
import org.junit.Test;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.time.Instant;

public class ReaderUtilTest {

    @Test
    public void testGetDateTime() {
        try {
            DateTime dateTime = ReaderUtil.getDateTime("2017-01-01 00:00:00", ReadRequestForm.DATE_TIME_PATTERN);
            assertEquals("2017-01-01T00:00:00Z", dateTime.toString());
        } catch (ParseException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetInstant() {
        try {
            Instant instant = ReaderUtil.parse("2017-01-01 00:00:00", EnrichRequestForm.DATE_TIME_PATTERN);
            assertEquals("2017-01-01T00:00:00Z", instant.toString());
        } catch (ParseException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testFormat() {
        try {
            String date = "2017-01-01 00:00:00";
            Instant instant = ReaderUtil.parse(date, EnrichRequestForm.DATE_TIME_PATTERN);
            String formattedDate = ReaderUtil.format(instant, EnrichRequestForm.DATE_TIME_PATTERN);
            assertEquals(date, formattedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            fail();
        }
    }
}
