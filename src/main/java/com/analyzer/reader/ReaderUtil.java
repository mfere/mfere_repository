package com.analyzer.reader;

import com.analyzer.constants.GranularityType;
import com.analyzer.constants.InstrumentValue;
import com.analyzer.model.RawCandlestick;
import com.analyzer.model.repository.RawCandlestickRepository;
import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.primitives.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ReaderUtil {

    private static final Logger log = LoggerFactory.getLogger(ReaderUtil.class);

    public static DateTime getDateTime(String value, String pattern) throws ParseException {
        return new DateTime(parse(value, pattern).toString());
    }

    public static Instant parse(String value, String pattern) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.parse(value).toInstant();
    }

    public static String format(Instant instant, String pattern) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern( pattern )
                        .withLocale( Locale.UK )
                        .withZone( ZoneId.of("UTC") );
        return formatter.format(instant);
    }

    public static void saveCandles(
            GranularityType granularity,
            InstrumentValue instrument,
            List<Candlestick> candles,
            RawCandlestickRepository rawCandlestickRepository) {
        Candlestick previous = null;
        Candlestick next;
        for (int i = 0; i < candles.size(); i++) {
            if (i+1 < candles.size()) {
                next = candles.get(i+1);
            } else {
                next = null;
            }

            Instant instant = Instant.parse(candles.get(i).getTime());

            RawCandlestick rawCandlestick = rawCandlestickRepository.findOne(
                    instant,
                    granularity,
                    instrument);
            if (candles.get(i).getComplete()) {
                if (rawCandlestick == null) {
                    rawCandlestick = rawCandlestickRepository.save(
                            candles.get(i),
                            granularity,
                            instrument,
                            previous, next);
                    log.info("Saved new value: "+rawCandlestick);
                } else if ((rawCandlestick.getNextDateTime() == null && next != null)
                        || (rawCandlestick.getPrevDateTime() == null && previous != null)) {
                    if (next != null) {
                        rawCandlestick.setNextDateTime(Instant.parse(next.getTime()));
                    }
                    if (previous != null) {
                        rawCandlestick.setPrevDateTime(Instant.parse(previous.getTime()));
                    }
                    rawCandlestick = rawCandlestickRepository.save(rawCandlestick);
                    log.info("Updated value: "+rawCandlestick);
                }
            }
            previous = candles.get(i);
        }
    }

    public static List<RawCandlestick> getRawCandlesticks(
            Instant fromDate,
            Instant toDate,
            GranularityType granularity,
            InstrumentValue instrument,
            RawCandlestickRepository rawCandlestickRepository) throws Exception {
        RawCandlestick rawCandlestick;
        int i = 0;
        do {
            rawCandlestick = rawCandlestickRepository.findOne(
                    fromDate,
                    granularity,
                    instrument);
            if (rawCandlestick == null) {
                log.info("Candlestick not found on date: "+ fromDate);
                LocalDateTime nextDate;
                if (granularity == GranularityType.D) {
                    nextDate = LocalDateTime.ofInstant(fromDate, ZoneId.of("UTC")).plusDays(1);
                } else if (granularity == GranularityType.H4) {
                    nextDate = LocalDateTime.ofInstant(fromDate, ZoneId.of("UTC")).plusHours(4);
                } else if (granularity == GranularityType.H2) {
                    nextDate = LocalDateTime.ofInstant(fromDate, ZoneId.of("UTC")).plusHours(2);
                } else if (granularity == GranularityType.H1) {
                    nextDate = LocalDateTime.ofInstant(fromDate, ZoneId.of("UTC")).plusHours(1);
                } else {
                    throw new Exception("Could not found any candlestick up to "+ fromDate);
                }
                fromDate = nextDate.toInstant(ZoneOffset.UTC);
                i++;
            }
        } while (rawCandlestick == null && i < 10);

        if (rawCandlestick == null) {
            throw new Exception("Could not found any candlestick up to "+ fromDate);
        }
        log.info("Found candlestick on date: "+ fromDate);
        List<RawCandlestick> rawCandlestickList = new ArrayList<>();
        while (rawCandlestick != null &&
                !rawCandlestick.getRawCandlestickKey().getDateTime().isAfter(
                        toDate
                )){
            rawCandlestickList.add(rawCandlestick);
            if (rawCandlestick.getNextDateTime() != null) {
                rawCandlestick = rawCandlestickRepository.findOne(
                        rawCandlestick.getNextDateTime(),
                        granularity,
                        instrument);
            } else {
                rawCandlestick = null;
            }

        }
        return rawCandlestickList;
    }

}
