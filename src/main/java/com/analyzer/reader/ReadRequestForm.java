package com.analyzer.reader;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

@Setter
@Getter
@ToString
public class ReadRequestForm {

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private String instrument;// = InstrumentValue.EUR_USD.name();
    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private String fromDate;// = "2016-01-01 00:00:00";
    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private String toDate;// = "2016-02-01 00:00:00";
    private List<String> granularity;// = GranularityValue.D.getName();

 }
