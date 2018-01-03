package com.analyzer.configuration;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.PrintWriter;

import static com.analyzer.TestConstants.NETWORK_PATH;

public class NetworkCreator {

    private PropertiesConfiguration properties;

    @Before
    public void initProperties() throws Exception {
        properties = new PropertiesConfiguration("application.properties");
    }

    @Test
    public void createBaseNeuralNetwork() {
        String networkName = "baseNetwork";
        int seed = 123;
        double learningRate = 0.1;
        int numInputs = 2; // This will be overwritten
        int numOutputs = 2; // This will be overwritten
        int numHiddenNodes = 20;


        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(1)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(learningRate)
                .updater(new Nesterovs(0.9))
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(numInputs)
                        .nOut(numHiddenNodes)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new OutputLayer.Builder(
                        LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(numHiddenNodes)
                        .nOut(numOutputs)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.SOFTMAX)
                        .build())
                .pretrain(false)
                .backprop(true)
                .build();

        System.out.println(conf.toJson());
        writeToFile(networkName, conf);
    }

    @Test
    public void create3LayerNetwork() {
        String networkName = "3layerNetwork";
        int seed = 123;
        double learningRate = 0.1;
        int numInputs = 2; // This will be overwritten
        int numOutputs = 2; // This will be overwritten
        int numHiddenNodes = 20;


        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .biasInit(1)
                .regularization(true).l2(1e-4)
                .iterations(1)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(learningRate)
                .updater(Updater.NESTEROVS)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.SOFTMAX)
                        .nIn(numHiddenNodes).nOut(numOutputs).build())
                .pretrain(false).backprop(true).build();

        System.out.println(conf.toJson());
        writeToFile(networkName, conf);
    }

    @Test
    public void create3Layer50HiddenNetwork() {
        String networkName = "3layer50HiddenNetwork";
        int seed = 123;
        double learningRate = 0.001;
        int numInputs = 2; // This will be overwritten
        int numOutputs = 2; // This will be overwritten
        int numHiddenNodes = 50;


        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .biasInit(1)
                .regularization(true).l2(1e-4)
                .iterations(1)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(learningRate)
                .updater(Updater.NESTEROVS)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.SOFTMAX)
                        .nIn(numHiddenNodes).nOut(numOutputs).build())
                .pretrain(false).backprop(true).build();

        System.out.println(conf.toJson());
        writeToFile(networkName, conf);
    }



    private void writeToFile(String networkName, MultiLayerConfiguration conf) {

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(
                    properties.getProperty(NETWORK_PATH) + networkName + ".json", "UTF-8");
            writer.write(conf.toJson());
            writer.flush();
        }catch (Exception e) {
            e.printStackTrace();
            if (writer != null) {
                writer.close();
            }
        }
    }
}
