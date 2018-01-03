package com.analyzer.learner.stopcondition;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeastError implements StopCondition {

    private static final Logger log = LoggerFactory.getLogger(BestF1Score.class);

    private int maxNoChangeIteration;
    private int iterationNumber;
    private MultiLayerNetwork model;
    private MultiLayerNetwork bestConfiguration;
    private Double leastError = null;

    LeastError(int maxNoChangeIteration, MultiLayerNetwork model) {
        this.maxNoChangeIteration = maxNoChangeIteration;
        this.model = model;
        this.leastError = null;
        this.iterationNumber = 0;
    }

    @Override
    public boolean isConditionMet() {
        if (leastError == null) {
            leastError = model.score();
            log.info("Found new least error model: " + leastError);
        } else {
            if (model.score() < leastError) {
                leastError = model.score();
                iterationNumber = 0;
                bestConfiguration = model;
                log.info("Found new least error model: " + leastError);
            } else {
                iterationNumber ++;
            }
        }
        if (iterationNumber >= maxNoChangeIteration) {
            log.info("Finishing using model with score: "+model.score());
            return true;
        }
        return false;
    }

    public MultiLayerNetwork getBestConfiguration() {
        return bestConfiguration;
    }
}
