package com.analyzer.learner.stopcondition;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

public interface StopCondition {
    boolean isConditionMet();

    MultiLayerNetwork getBestConfiguration();
}
