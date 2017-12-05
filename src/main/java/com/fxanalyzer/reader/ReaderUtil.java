package com.fxanalyzer.reader;

import com.oanda.v20.primitives.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.TimeZone;

public class ReaderUtil {

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

}
