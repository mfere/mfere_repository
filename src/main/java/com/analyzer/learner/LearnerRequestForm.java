package com.analyzer.learner;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

@Setter
@Getter
@ToString
public class LearnerRequestForm {

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private String name;
    private String instrument;// = InstrumentValue.EUR_USD.name();
    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private String trainFromDate;// = "2016-06-01 00:00:00";
    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private String trainToDate;// = "2016-02-01 00:00:00";
    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private String validateFromDate;// = "2016-01-01 00:00:00";
    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private String validateToDate;// = "2016-02-01 00:00:00";
    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private String testFromDate;// = "2016-01-01 00:00:00";
    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private String testToDate;// = "2016-02-01 00:00:00";
    private String granularity;// = GranularityType.D.getName();
    private List<String> indicators;
    private List<String> watchInstruments;
    private Integer pastValuesNumber = 0;
    private String strategy;
    private String networkConfiguration;
    private Double learningRate;
    private String stopCondition;
    private String normalizer;

    private Integer batchNumber = 50;


 }
