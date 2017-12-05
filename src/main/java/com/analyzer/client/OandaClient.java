package com.analyzer.client;

import com.analyzer.constants.GranularityValue;
import com.analyzer.reader.ReadRequestForm;
import com.analyzer.reader.ReaderUtil;
import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.primitives.DateTime;
import com.oanda.v20.primitives.InstrumentName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

@Service
public class OandaClient {

    private static final int MAX_RECORD_COUNT = 4000;

    private final Context ctx;
    private final AccountID accountID;

    @Autowired
    public OandaClient(
            @Value("${oanda.url}") String url,
            @Value("${oanda.token}") String token,
            @Value("${oanda.account_id}") String accountId
    ) {
        // See http://developer.oanda.com/rest-live-v20/instrument-ep/
        ctx = new Context(url, token);
        accountID = new AccountID(accountId);
    }


    public List<Candlestick> getInstrumentCandles(
            ReadRequestForm readRequestForm, String granularity) throws Exception {

        List<Candlestick> candlesticks = new ArrayList<>();
        boolean keepRead = true;
        Instant lastDate = null;

        if (readRequestForm.getFromDate() == null){
            throw new Exception("From date cannot be null");
        }
        Instant fromDate = ReaderUtil.parse(readRequestForm.getFromDate(), ReadRequestForm.DATE_TIME_PATTERN);
        Instant toDate = readRequestForm.getToDate() == null ? null :
                ReaderUtil.parse(readRequestForm.getToDate(), ReadRequestForm.DATE_TIME_PATTERN);
        InstrumentCandlesRequest request = new InstrumentCandlesRequest(
                new InstrumentName(readRequestForm.getInstrument()));

        while (keepRead) {
            if (fromDate != null) {
                request.setFrom(new DateTime(fromDate.toString()));
            }
            if (toDate != null) {
                request.setFrom(new DateTime(toDate.toString()));
            }

            GranularityValue granularityValue = GranularityValue.getGranularityValue(granularity);
            if (granularityValue == null){
                throw new Exception("Invalid granularity: "+ granularity);
            }
            request.setGranularity(granularityValue.getGranularity());
            request.setIncludeFirst(true);
            request.setSmooth(false);
            request.setDailyAlignment(0);
            request.setAlignmentTimezone("UTC");
            request.setPrice(readRequestForm.getPrice());
            request.setCount(MAX_RECORD_COUNT);
            System.out.println("Starting read using parameters: "
                    + ", fromDate: " + fromDate
                    + ", toDate: " + toDate
                    + ", granularity: " + granularityValue.getGranularity()
                    + ", instrument: " + readRequestForm.getInstrument());
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

                if ((toDate == null || lastDate.isBefore(toDate)) && !lastCandlestick.getComplete()) {
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
