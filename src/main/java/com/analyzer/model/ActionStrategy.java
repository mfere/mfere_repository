package com.analyzer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@AllArgsConstructor
public class ActionStrategy {
    private String strategyTypeValue;
    private Integer actionTypeValue;
}
