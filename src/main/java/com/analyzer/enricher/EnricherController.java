package com.analyzer.enricher;

import com.analyzer.constants.GranularityValue;
import com.analyzer.constants.IndicatorValue;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.constants.RewardFunctionValue;
import com.analyzer.model.FxIndicator;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import com.analyzer.reader.ReadRequestForm;
import com.analyzer.reader.ReaderUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class EnricherController {

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

            TimeSeries timeSeries = TimeSeriesLoader.loadTimeSeries(rawCandlestickList);

            // for now make it fix to close price
            ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(timeSeries);
                Map<String, Indicator<Decimal>> indicators = new HashMap<>();
            for (String indicatorName : enrichRequestForm.getIndicators()) {
                Indicator<Decimal> indicator = IndicatorFactory.getIndicator(
                        closePriceIndicator, IndicatorValue.getIndicatorValue(indicatorName));
                if (indicator == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                indicators.put(indicatorName,indicator);
            }

            for (int i = 0; i < timeSeries.getTickCount(); i++){
                rawCandlestick = rawCandlestickList.get(i);
                for (String indicatorName : indicators.keySet()) {
                    rawCandlestick.addIndicator(
                            new FxIndicator(indicatorName, indicators.get(indicatorName).getValue(i).toDouble())
                    );
                }
                for (String indicatorName : indicators.keySet()) {
                    rawCandlestick.addIndicator(
                            new FxIndicator(indicatorName, indicators.get(indicatorName).getValue(i).toDouble())
                    );
                }
                for (String rewardFunctionName : enrichRequestForm.getRewardFunctions()) {
                    RewardFunctionBuilder rewardFunctionBuilder = RewardFunctionFactory.getRewardFunction(
                            RewardFunctionValue.getRewardFunctionValue(rewardFunctionName),
                            rawCandlestickRepository);
                    if (rewardFunctionBuilder == null) {
                        System.out.println("No reward builder found");
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                    rawCandlestick.addRewardFunction(rewardFunctionBuilder.getRewardFunction(rawCandlestick));
                }

                rawCandlestickRepository.save(rawCandlestick);
            }


            return new ResponseEntity<>("ok", HttpStatus.OK);
        } catch (Throwable t) {
            t.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
