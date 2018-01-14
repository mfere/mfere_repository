package com.analyzer.client;

import com.analyzer.constants.GranularityValue;
import com.analyzer.constants.InstrumentValue;
import com.oanda.v20.Context;
import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.primitives.DateTime;
import com.oanda.v20.primitives.InstrumentName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

@Service
public class OandaReaderClient {

    private static final Logger log = LoggerFactory.getLogger(OandaReaderClient.class);

    private static final int MAX_RECORD_COUNT = 4000;

    private final Context ctx;

    @Autowired
    public OandaReaderClient(
            @Value("${oanda.url}") String url,
            @Value("${oanda.token}") String token
    ) {
        // See http://developer.oanda.com/rest-live-v20/instrument-ep/
        ctx = new Context(url, token);
    }


    public List<Candlestick> getInstrumentCandles(
            Instant fromDate,
            Instant toDate,
            GranularityValue granularity,
            InstrumentValue instrument) throws Exception {

        List<Candlestick> candlesticks = new ArrayList<>();
        boolean keepRead = true;
        Instant lastDate = null;


                InstrumentCandlesRequest request = new InstrumentCandlesRequest(
                new InstrumentName(instrument.name()));

        while (keepRead) {
            if (fromDate != null) {
                request.setFrom(new DateTime(fromDate.toString()));
            }
            if (toDate != null) {
                request.setTo(new DateTime(toDate.toString()));
            }

            request.setGranularity(granularity.getOandaGranularity());
            request.setIncludeFirst(true);
            request.setSmooth(false);
            request.setDailyAlignment(0);
            request.setAlignmentTimezone("UTC");
            request.setPrice("ABM");
            request.setCount(MAX_RECORD_COUNT);
            log.info("Starting read using parameters: "
                    + ", fromDate: " + fromDate
                    + ", toDate: " + toDate
                    + ", price: " + "ABM"
                    + ", granularity: " + granularity.name()
                    + ", instrument: " + instrument.name());
            InstrumentCandlesResponse response = ctx.instrument.candles(request);
            if (response.getCandles() == null) {
                throw new Exception("No candles found in selected period");
            }

            if (lastDate != null && lastDate.equals(Instant.parse(response.getCandles().get(0).getTime()))) {
                // remove last element to prevent that it gets added twice
                candlesticks.remove(candlesticks.size() - 1);
            }
            candlesticks.addAll(response.getCandles());

            if (response.getCandles().size() < MAX_RECORD_COUNT) {
                keepRead = false;
            } else {
                Candlestick lastCandlestick = response.getCandles().get(response.getCandles().size() - 1);
                lastDate = Instant.parse(lastCandlestick.getTime());

                if (toDate == null || (lastDate.isBefore(toDate)) && !lastCandlestick.getComplete()) {
                    fromDate = lastDate;
                    sleep(1300);
                    keepRead = true;
                } else {
                    keepRead = false;
                }
            }
        }

        return candlesticks;
    }

}
