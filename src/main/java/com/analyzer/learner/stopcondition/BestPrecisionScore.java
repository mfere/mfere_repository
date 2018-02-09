package com.analyzer.learner.stopcondition;

import com.analyzer.learner.LearnerController;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BestPrecisionScore implements StopCondition {

    private static final Logger log = LoggerFactory.getLogger(BestPrecisionScore.class);

    private int maxNoChangeIteration;
    private int checkIteration;
    private int iterationNumber;
    private int noChangeIterationNumber;
    private MultiLayerNetwork model;
    private Double bestScore;
    private DataSetIterator dataIterator;
    private int labelsLength;
    private MultiLayerNetwork bestConfiguration;

    BestPrecisionScore(int checkIteration,
                       int maxNoChangeIteration,
                       MultiLayerNetwork model,
                       DataSetIterator dataIterator,
                       int numOutput) {
        this.checkIteration = checkIteration;
        this.model = model;
        this.bestScore = null;
        this.iterationNumber = 0;
        this.noChangeIterationNumber = 0;
        this.maxNoChangeIteration = maxNoChangeIteration;
        this.dataIterator = dataIterator;
        this.labelsLength = numOutput;
    }

    @Override
    public boolean isConditionMet() {
        if (iterationNumber < checkIteration) {
            iterationNumber++;
            return false;
        }

        Evaluation eval = LearnerController.evaluateModel(dataIterator, model, labelsLength);
        if (this.bestScore == null) {
            this.bestScore = eval.precision() > 0 ? eval.precision() : null;
            log.info("Found new best precision score: " + this.bestScore);
            bestConfiguration = model.clone();
        } else {
            if (eval.precision() > this.bestScore) {
                this.bestScore = eval.precision();
                log.info("Found new best precision score: " + this.bestScore);
            } else {
                noChangeIterationNumber++;
                if (noChangeIterationNumber == maxNoChangeIteration) {
                    log.info("Finishing using models with precision score: " + eval.precision());
                    return true;
                }
            }
        }
        iterationNumber = 0;
        return false;
    }

    public MultiLayerNetwork getBestConfiguration() {
        return bestConfiguration;
    }
}