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
    private String trainFromDate;// = "2016-01-01 00:00:00";
    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private String trainToDate;// = "2016-02-01 00:00:00";
    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private String testFromDate;// = "2016-01-01 00:00:00";
    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private String testToDate;// = "2016-02-01 00:00:00";
    private String granularity;// = GranularityValue.D.getName();
    private List<String> indicators;
    private String rewardFunction;
    private String networkConfiguration;
    private Double learningRate;
    private Boolean testConvergance;
    private String stopCondition;

 }
