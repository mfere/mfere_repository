package com.analyzer.reader;

import com.analyzer.constants.GranularityType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.model.repository.RawCandlestickRepository;
import com.analyzer.client.OandaReaderClient;
import com.oanda.v20.instrument.Candlestick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.List;


@RestController
public class ReaderController {

    private static final Logger log = LoggerFactory.getLogger(ReaderController.class);

    private final RawCandlestickRepository rawCandlestickRepository;
    private final OandaReaderClient oandaClient;

    @Autowired
    ReaderController(
            RawCandlestickRepository rawCandlestickRepository,
            OandaReaderClient oandaClient) {
        this.rawCandlestickRepository = rawCandlestickRepository;
        this.oandaClient = oandaClient;
    }

    @RequestMapping(value = "/read", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> read(
            @Valid @RequestBody ReadRequestForm readRequestForm,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            InstrumentValue instrument = InstrumentValue.valueOf(
                    readRequestForm.getInstrument());

            if (readRequestForm.getFromDate() == null){
                throw new Exception("From date cannot be null");
            }

            Instant fromDate = ReaderUtil.parse(readRequestForm.getFromDate(), ReadRequestForm.DATE_TIME_PATTERN);
            Instant toDate = readRequestForm.getToDate() == null ? null :
                    ReaderUtil.parse(readRequestForm.getToDate(), ReadRequestForm.DATE_TIME_PATTERN);


            for (String granularityName : readRequestForm.getGranularity()) {
                GranularityType granularity = GranularityType.valueOf(granularityName);

                List<Candlestick> candles = oandaClient.getInstrumentCandles(
                        fromDate, toDate,
                        granularity, instrument);
                ReaderUtil.saveCandles(granularity, instrument, candles, rawCandlestickRepository);
            }
            return new ResponseEntity<>("ok", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
