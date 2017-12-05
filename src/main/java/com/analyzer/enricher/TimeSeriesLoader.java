package com.analyzer.enricher;


import com.analyzer.model.RawCandlestick;
import org.ta4j.core.BaseTick;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class build a Ta4j time series from different sources
 */
public class TimeSeriesLoader {

        /**
         * @return a time series from RawCandlestick series
         */
        public static TimeSeries loadTimeSeries(List<RawCandlestick> rawCandlesticks) {

            List<Tick> ticks = new ArrayList<>();

            for (RawCandlestick rawCandlestick : rawCandlesticks) {

                ZonedDateTime dateTime = rawCandlestick.getRawCandlestickKey().getDateTime().atZone(ZoneId.of("UTC"));

                ticks.add(new BaseTick(
                        dateTime,
                        rawCandlestick.getMidRawCandlestickData().getOpen(),
                        rawCandlestick.getMidRawCandlestickData().getHigh(),
                        rawCandlestick.getMidRawCandlestickData().getLow(),
                        rawCandlestick.getMidRawCandlestickData().getClose(),
                        rawCandlestick.getVolume()));
            }

            return new BaseTimeSeries(rawCandlesticks.get(0).getRawCandlestickKey().getInstrument(), ticks);
        }
}
