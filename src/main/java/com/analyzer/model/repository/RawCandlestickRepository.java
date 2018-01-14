package com.analyzer.model.repository;

import com.analyzer.constants.GranularityValue;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.RawCandlestick.RawCandlestickKey;
import com.oanda.v20.instrument.Candlestick;
import org.springframework.data.repository.Repository;

import java.text.ParseException;
import java.time.Instant;

public interface RawCandlestickRepository extends Repository<RawCandlestick, RawCandlestickKey> {

    RawCandlestick save(RawCandlestick entity);

    RawCandlestick findOne(RawCandlestick.RawCandlestickKey id);

    void delete(RawCandlestick.RawCandlestickKey id);

    default RawCandlestick save(Candlestick candlestick,
                                GranularityValue granularity,
                                InstrumentValue instrument,
                                Candlestick previous,
                                Candlestick next) {
        RawCandlestick rawCandlestick;
        try {
            rawCandlestick = new RawCandlestick(
                    candlestick,
                    granularity,
                    instrument,
                    previous,
                    next);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return save(rawCandlestick);
    }

    default RawCandlestick findOne(
            String datetime,
            String granularity,
            String instrument) {
        GranularityValue granularityValue = GranularityValue.valueOf(
                granularity);
        InstrumentValue instrumentValue = InstrumentValue.valueOf(
                instrument);
        return findOne(
                Instant.parse(datetime),
                granularityValue,
                instrumentValue);
    }

    default RawCandlestick findOne(Instant datetime,
                                   GranularityValue granularity,
                                   InstrumentValue instrument) {
        RawCandlestickKey id = new RawCandlestickKey(datetime,
                granularity.name(), instrument.name());
        return findOne(id);
    }
}
