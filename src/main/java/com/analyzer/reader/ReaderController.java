package com.analyzer.reader;

import com.analyzer.constants.GranularityValue;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import com.analyzer.client.OandaClient;
import com.oanda.v20.instrument.Candlestick;
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

    private final RawCandlestickRepository rawCandlestickRepository;
    private final OandaClient oandaClient;

    @Autowired
    ReaderController(
            RawCandlestickRepository rawCandlestickRepository,
            OandaClient oandaClient) {
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

            for (String granularityName : readRequestForm.getGranularity()) {
                List<Candlestick> candles = oandaClient.getInstrumentCandles(
                        readRequestForm, granularityName);
                Candlestick previous = null;
                Candlestick next;

                for (int i = 0; i < candles.size(); i++) {
                    if (i+1 < candles.size()) {
                        next = candles.get(i+1);
                    } else {
                        next = null;
                    }
                    GranularityValue granularity = GranularityValue.getGranularityValue(granularityName);
                    InstrumentValue instrument = InstrumentValue.getInstrumentValue(
                            readRequestForm.getInstrument());

                    Instant instant = Instant.parse(candles.get(i).getTime());

                    RawCandlestick rawCandlestick = rawCandlestickRepository.findOne(
                            instant,
                            granularity,
                            instrument);
                    if (rawCandlestick == null) {
                        rawCandlestick = rawCandlestickRepository.save(
                                candles.get(i),
                                granularity,
                                instrument,
                                previous, next);
                        System.out.println("Saved new value: "+rawCandlestick);
                    } else if ((rawCandlestick.getNextDateTime() == null && next != null)
                            || (rawCandlestick.getPrevDateTime() == null && previous != null)) {
                        if (next != null) {
                            rawCandlestick.setPrevDateTime(Instant.parse(next.getTime()));
                        }
                        if (previous != null) {
                            rawCandlestick.setPrevDateTime(Instant.parse(previous.getTime()));
                        }
                        rawCandlestick = rawCandlestickRepository.save(rawCandlestick);
                        System.out.println("Updated value: "+rawCandlestick);
                    }
                    previous = candles.get(i);
                }
            }
            return new ResponseEntity<>("ok", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
