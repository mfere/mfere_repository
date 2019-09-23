package com.analyzer.configuration;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.junit.Before;
import org.junit.Test;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.PrintWriter;

import static com.analyzer.TestConstants.NETWORK_PATH;

public class NetworkCreator {

    private PropertiesConfiguration properties;

    public void createNetwork(String networkName) {
        int seed = 123;
        double learningRate = 0.01;
        int numInputs = 2; // This will be overwritten
        int numOutputs = 2; // This will be overwritten
        int numHiddenNodes = 500;
        int layerCounter = 0;


        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .biasInit(1)
                .regularization(true)
                .l2(1e-4) // FASTER LEARNING
                .l2Bias(1e-4)
                .learningRateScoreBasedDecayRate(0.9) // FASTER LEARNING
                .iterations(1)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(learningRate)
                .updater(Updater.NADAM)  // AVOID OVERFITTING AND PREVENTS LOCAL MINIMUM WITH MORE THAN 1 BATCH
                .list()
                .layer(layerCounter++, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        //.dropOut(0.8)                       // Avoid overfitting by randomly remove some nodes
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)  // PREVENTS LOCAL MINIMUM WITH MORE THAN 1 BATCH
                        .build())
                .layer(layerCounter++, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes/2)
                        //.dropOut(0.5)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)  // PREVENTS LOCAL MINIMUM WITH MORE THAN 1 BATCH
                        .build())
                .layer(layerCounter++, new DenseLayer.Builder().nIn(numHiddenNodes/2).nOut(numHiddenNodes/5)
                        //.dropOut(0.5)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)  // PREVENTS LOCAL MINIMUM WITH MORE THAN 1 BATCH
                        .build())
                .layer(layerCounter++, new DenseLayer.Builder().nIn(numHiddenNodes/5).nOut(numHiddenNodes/10)
                        //.dropOut(0.5)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)  // PREVENTS LOCAL MINIMUM WITH MORE THAN 1 BATCH
                        .build())
                .layer(layerCounter, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        //OutputLayer.Builder(LossFunctions.LossFunction.RECONSTRUCTION_CROSSENTROPY) // NO CONVERGENCE
                        //OutputLayer.Builder(LossFunctions.LossFunction.MCXENT // For multi class classification
                        //OutputLayer.Builder(LossFunctions.LossFunction.MSE // For regression
                        //.dropOut(1)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.SOFTMAX)
                        .nIn(numHiddenNodes/10).nOut(numOutputs).build())
                .pretrain(false).backprop(true).build();

        System.out.println(conf.toJson());
        writeToFile(networkName, conf);
    }

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
        int numHiddenNodes = 100;


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
        double learningRate = 0.1; // This will be overwritten
        int numInputs = 2; // This will be overwritten
        int numOutputs = 2; // This will be overwritten
        int numHiddenNodes = 20;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .biasInit(1)
                .regularization(true).l2(1e-4)
                .learningRateScoreBasedDecayRate(0.5)
                .iterations(1)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(learningRate)
                //.updater(Updater.NESTEROVS)
                .updater(Updater.ADAM)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(2, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
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
        createNetwork(networkName);
    }

    /*
    public void createLSTMNetwork(String networkName) {
        int seed = 123;
        double learningRate = 0.01;
        int numInputs = 2; // This will be overwritten
        int numOutputs = 2; // This will be overwritten
        int numHiddenNodes = 500;

        int lstmLayerSize = 200;					//Number of units in each LSTM layer
        int miniBatchSize = 32;						//Size of mini batch to use when  training
        int exampleLength = 1000;					//Length of each training example sequence to use. This could certainly be increased
        int tbpttLength = 50;                       //Length for truncated backpropagation through time. i.e., do parameter updates ever 50 characters
        int numEpochs = 1;							//Total number of training epochs
        int generateSamplesEveryNMinibatches = 10;  //How frequently to generate samples from the network? 1000 characters / 50 tbptt length: 20 parameter updates per minibatch
        int nSamplesToGenerate = 4;					//Number of samples to generate after each training epoch
        int nCharactersToSample = 300;				//Length of each sample to generate



        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .l2(0.0001)
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.ADAM)
                .list()
                .layer(new LSTM.Builder().nIn(3).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build())
                .layer(new LSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build())
                .layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                        .nIn(lstmLayerSize).nOut(2).build())
                .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength).tBPTTBackwardLength(tbpttLength)
                .build();

        System.out.println(conf.toJson());
        writeToFile(networkName, conf);
    }
*/

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
