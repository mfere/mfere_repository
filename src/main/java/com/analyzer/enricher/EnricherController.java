package com.analyzer.enricher;

import com.analyzer.constants.GranularityValue;
import com.analyzer.constants.IndicatorValue;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.constants.RewardFunctionValue;
import com.analyzer.enricher.rewardfunction.RewardFunctionBuilder;
import com.analyzer.enricher.rewardfunction.RewardFunctionFactory;
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
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import javax.validation.Valid;
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
            GranularityValue granularity = GranularityValue.getGranularityValue(
                    enrichRequestForm.getGranularity());
            InstrumentValue instrument = InstrumentValue.getInstrumentValue(
                    enrichRequestForm.getInstrument());
            rawCandlestick = rawCandlestickRepository.findOne(
                    ReaderUtil.parse(enrichRequestForm.getFromDate(),ReadRequestForm.DATE_TIME_PATTERN),
                    granularity,
                    instrument);

            List<RawCandlestick> rawCandlestickList = new ArrayList<>();
            while (rawCandlestick.getNextDateTime() != null &&
                    !rawCandlestick.getNextDateTime().isAfter(
                            ReaderUtil.parse(enrichRequestForm.getToDate(),ReadRequestForm.DATE_TIME_PATTERN)
                    )){
                rawCandlestickList.add(rawCandlestick);
                rawCandlestick = rawCandlestickRepository.findOne(
                        rawCandlestick.getNextDateTime(),
                        granularity,
                        instrument);
            }

            // initial all necessary indicators in IndicatorFactory
            List<IndicatorValue> indicatorList = new ArrayList<>();
            for (String indicatorName : enrichRequestForm.getIndicators()) {
                indicatorList.add(IndicatorValue.getIndicatorValue(indicatorName));
            }
            IndicatorFactory indicatorFactory = new IndicatorFactory(rawCandlestickList);

            // set value for each candles' indicator
            for (int i = 0; i < rawCandlestickList.size(); i++){
                rawCandlestick = rawCandlestickList.get(i);
                for (IndicatorValue indicatorName : indicatorList) {
                    rawCandlestick.addIndicator(
                            new FxIndicator(indicatorName.getName(), indicatorFactory.getIndicatorValue(indicatorName, i))
                    );
                }
            }

            for (String rewardFunctionName : enrichRequestForm.getRewardFunctions()) {
                RewardFunctionBuilder rewardFunctionBuilder = RewardFunctionFactory.getRewardFunction(
                        RewardFunctionValue.getRewardFunctionValue(rewardFunctionName),
                        rawCandlestickRepository);
                if (rewardFunctionBuilder == null) {
                    log.info("No reward builder found");
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                for (RawCandlestick candlestick : rawCandlestickList) {
                    candlestick.addRewardFunction(rewardFunctionBuilder.getRewardFunction(candlestick));
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
