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

public class ValidationScoreIterationListener implements IterationListener {
    private int printIterations;
    private static final Logger log = LoggerFactory.getLogger(org.deeplearning4j.optimize.listeners.ScoreIterationListener.class);
    private boolean invoked = false;
    private long iterCount = 0;
    private int numOutputs;
    private DataSetIterator iterator;

    /**
     * @param printIterations frequency with which to print scores (i.e., every printIterations parameter updates)
     */
    public ValidationScoreIterationListener(int printIterations, int numOutputs, DataSetIterator validationScoreIterator) {
        this.printIterations = printIterations;
        this.numOutputs = numOutputs;
        this.iterator = validationScoreIterator;
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
        if (printIterations <= 0)
            printIterations = 1;
        if (iterCount % printIterations == 0) {
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
        }
        iterCount++;
    }
}
