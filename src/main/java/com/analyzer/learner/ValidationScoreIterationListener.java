package com.analyzer.learner;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.api.IterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;

public class ValidationScoreIterationListener implements IterationListener {
    private int delayInSeconds;
    private static final Logger log = LoggerFactory.getLogger(org.deeplearning4j.optimize.listeners.ScoreIterationListener.class);
    private boolean invoked = false;
    private long iterCount = 0;
    private int numOutputs;
    private DataSetIterator iterator;
    private Instant lastInstant;

    /**
     * @param delayInSeconds after how many seconds we can print
     */
    public ValidationScoreIterationListener(int delayInSeconds, int numOutputs, DataSetIterator validationScoreIterator) {
        this.delayInSeconds = delayInSeconds;
        this.numOutputs = numOutputs;
        this.iterator = validationScoreIterator;
        this.lastInstant = Instant.now();
    }

    @Override
    public boolean invoked() {
        return invoked;
    }

    @Override
    public void invoke() {
        this.invoked = true;
    }

    @Override
    public void iterationDone(Model model, int iteration) {
        if (!(model instanceof MultiLayerNetwork)) {
            throw new RuntimeException("This listener can be used only on MultiLayerNetwork");
        }

        if (Instant.now().minusSeconds(delayInSeconds).isAfter(lastInstant)) {
            invoke();
            Evaluation eval = new Evaluation(numOutputs);
            iterator.reset();
            while(iterator.hasNext()){
                DataSet t = iterator.next();
                INDArray features = t.getFeatureMatrix();
                INDArray labels = t.getLabels();
                INDArray predicted = ((MultiLayerNetwork)model).output(features,false);
                eval.eval(labels, predicted);
            }
            double result = eval.f1();
            log.info("F1 Score on validation at " + iterCount + " is " + result);
            lastInstant = Instant.now();
        }
        iterCount++;
    }
}
