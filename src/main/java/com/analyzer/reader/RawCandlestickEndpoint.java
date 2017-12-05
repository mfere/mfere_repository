package com.analyzer.reader;

import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/rawcandlestick")
public class RawCandlestickEndpoint {

    private final RawCandlestickRepository rawCandlestickRepository;

    RawCandlestickEndpoint(RawCandlestickRepository rawCandlestickRepository) {
        this.rawCandlestickRepository = rawCandlestickRepository;
    }

    @GetMapping(value = "/{datetime}/{granularity}/{instrument}", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<RawCandlestick> rawCandlestick(
            @PathVariable String datetime,
            @PathVariable String granularity,
            @PathVariable String instrument) {
        RawCandlestick rawCandlestick = rawCandlestickRepository.findOne(datetime, granularity, instrument);
            if (rawCandlestick == null) {
            return new ResponseEntity<>(NOT_FOUND);
        }
        return new ResponseEntity<>(rawCandlestick, OK);
    }

}
