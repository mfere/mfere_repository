package com.analyzer.learner.stopcondition;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BestF1Score implements StopCondition {

    private static final Logger log = LoggerFactory.getLogger(BestF1Score.class);

    private int maxNoChangeIteration;
    private int checkIteration;
    private int iterationNumber;
    private int noChangeIterationNumber;
    private MultiLayerNetwork model;
    private Double bestScore = null;
    private DataSetIterator dataIterator;
    private int labelsLength;
    private MultiLayerNetwork bestConfiguration;

    BestF1Score(int checkIteration, int maxNoChangeIteration, MultiLayerNetwork model,
                DataSetIterator dataIterator) {
        this.checkIteration = checkIteration;
        this.model = model;
        this.bestScore = null;
        this.iterationNumber = 0;
        this.noChangeIterationNumber = 0;
        this.maxNoChangeIteration = maxNoChangeIteration;
        this.dataIterator = dataIterator;
        this.labelsLength = dataIterator.next().getLabels().length();
    }

    @Override
    public boolean isConditionMet() {
        if (iterationNumber < checkIteration) {
            iterationNumber++;
            return false;
        }

        dataIterator.reset();
        Evaluation eval = new Evaluation(labelsLength);
        while(dataIterator.hasNext()){
            DataSet t = dataIterator.next();
            INDArray features = t.getFeatureMatrix();
            INDArray labels = t.getLabels();
            INDArray predicted = model.output(features,false);
            eval.eval(labels, predicted);
        }
        if (this.bestScore == null) {
            this.bestScore = eval.f1() > 0 ? eval.f1() : null;
            log.info("Found new best f1score models: " + this.bestScore);
            bestConfiguration = model.clone();
        } else {
            if (eval.f1() > this.bestScore) {
                this.bestScore = eval.f1();
                log.info("Found new best f1score models: " + this.bestScore);
            } else {
                noChangeIterationNumber++;
                if (noChangeIterationNumber == maxNoChangeIteration) {
                    log.info("Finishing using models with f1score: " + eval.f1());
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