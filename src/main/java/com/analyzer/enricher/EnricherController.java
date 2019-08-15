package com.analyzer.enricher;

import com.analyzer.constants.GranularityType;
import com.analyzer.constants.IndicatorType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.constants.StrategyType;
import com.analyzer.enricher.strategy.Strategy;
import com.analyzer.enricher.strategy.StrategyFactory;
import com.analyzer.model.FxIndicator;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import com.analyzer.reader.ReadRequestForm;
import com.analyzer.reader.ReaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
public class EnricherController {

    private static final Logger log = LoggerFactory.getLogger(EnricherController.class);

    private final RawCandlestickRepository rawCandlestickRepository;

    EnricherController(RawCandlestickRepository rawCandlestickRepository) {
        this.rawCandlestickRepository = rawCandlestickRepository;
    }

    @RequestMapping(value = "/enrich", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> read(
            @Valid @RequestBody EnrichRequestForm enrichRequestForm,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        RawCandlestick rawCandlestick;
        try {
            GranularityType granularity = GranularityType.valueOf(
                    enrichRequestForm.getGranularity());
            InstrumentValue instrument = InstrumentValue.valueOf(
                    enrichRequestForm.getInstrument());
            Instant fromDate = ReaderUtil.parse(enrichRequestForm.getFromDate(),ReadRequestForm.DATE_TIME_PATTERN);
            Instant toDate = ReaderUtil.parse(enrichRequestForm.getToDate(),ReadRequestForm.DATE_TIME_PATTERN);

            List<RawCandlestick> rawCandlestickList = ReaderUtil.getRawCandlesticks(
                    fromDate, toDate,
                    granularity, instrument,
                    rawCandlestickRepository);

            // initial all necessary indicators in IndicatorFactory
            List<IndicatorType> indicatorList = new ArrayList<>();
            for (String indicatorName : enrichRequestForm.getIndicators()) {
                indicatorList.add(IndicatorType.valueOf(indicatorName));
            }
            IndicatorFactory indicatorFactory = new IndicatorFactory(rawCandlestickList);

            // set value for each candles indicator
            for (int i = 0; i < rawCandlestickList.size(); i++){
                rawCandlestick = rawCandlestickList.get(i);
                for (IndicatorType indicatorName : indicatorList) {
                    if (indicatorFactory.getIndicatorValue(indicatorName, i) != null) {
                        FxIndicator indicator = new FxIndicator(indicatorName.name(), indicatorFactory.getIndicatorValue(indicatorName, i));
                        if (Math.random() >= 0.99) {
                            log.info("Writing indicator "+indicator.getName()+" : "+indicator.getValue());
                        }
                        rawCandlestick.addIndicator(indicator);
                    }
                }
            }

            for (String strategyType : enrichRequestForm.getStrategies()) {
                Strategy strategy = StrategyFactory.getStrategy(
                        StrategyType.valueOf(strategyType));
                for (RawCandlestick candlestick : rawCandlestickList) {
                    candlestick.addActionStrategy(strategy.getCorrectActionStrategy(
                            rawCandlestickRepository, candlestick));
                }
            }

            for (RawCandlestick candlestick : rawCandlestickList) {
                rawCandlestickRepository.save(candlestick);
            }

            return new ResponseEntity<>("ok", HttpStatus.OK);
        } catch (Throwable t) {
            t.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
