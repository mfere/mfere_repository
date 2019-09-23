package com.analyzer.learner.stopcondition;

import com.analyzer.learner.LearnerController;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.evaluation.EvaluationTools;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BestScore implements StopCondition {

    private static final Logger log = LoggerFactory.getLogger(BestScore.class);

    private int maxNoChangeIteration;
    private int checkIteration;
    private int iterationNumber;
    private int noChangeIterationNumber;
    private MultiLayerNetwork model;
    private Double bestScore;
    private DataSetIterator dataIterator;
    private int labelsLength;
    private MultiLayerNetwork bestConfiguration;

    BestScore(int checkIteration,
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
            this.bestScore = eval.f1() > 0 ? eval.f1() : null;
            log.info("Found new best f1 score: " + this.bestScore);
            bestConfiguration = model.clone();
        } else {
            if (eval.f1() > this.bestScore) {
                this.bestScore = eval.f1();
                bestConfiguration = model.clone();
                log.info("Found new best f1 score: " + this.bestScore);
            } else {
                noChangeIterationNumber++;
                if (noChangeIterationNumber == maxNoChangeIteration) {
                    log.info("Finishing using models with f1 score: " + eval.f1());
                    return true;
                }
                log.info("Worse f1 score: " + eval.f1());
            }
        }
        iterationNumber = 0;
        return false;
    }

    public MultiLayerNetwork getBestConfiguration() {
        return bestConfiguration;
    }
}