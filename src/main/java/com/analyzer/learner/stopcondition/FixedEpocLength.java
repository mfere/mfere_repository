package com.analyzer.learner.stopcondition;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

public class FixedEpocLength implements StopCondition {
    private int maxLength;
    private int iterationNumber;
    private MultiLayerNetwork model;

    FixedEpocLength(int maxLength, MultiLayerNetwork model) {
        this.maxLength = maxLength;
        this.iterationNumber = 0;
        this.model = model;
    }

    @Override
    public boolean isConditionMet() {
        iterationNumber++;
        return iterationNumber >= maxLength;
    }

    @Override
    public MultiLayerNetwork getBestConfiguration() {
        return model;
    }
}
