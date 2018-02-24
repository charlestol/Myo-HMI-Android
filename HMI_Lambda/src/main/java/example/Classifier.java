package example;

import java.util.ArrayList;
import java.util.Arrays;

import smile.classification.*;
import smile.math.kernel.LinearKernel;
import smile.validation.CrossValidation;
import smile.math.Math;

/**
 * Created by Alex on 7/3/2017.
 */

public class Classifier {
    private String TAG = "Classifier";

    static int numFeatures = 6;
    static double[][] trainVectorP;
    static LDA lda;
    static SVM svm;
    static LogisticRegression logit; 
    static DecisionTree tree;
    static NeuralNetwork net;
    static KNN knn;
    static AdaBoost forest;

    int previousPrediction = 3;
    double[] features;
    static int[] classes;
    int[] testclasses = new int[3];
//    static Activity activity;
    int[] trainClasses;

    int samples = 100;

    double[][] trainVectorCV;
    LDA LDACV;
    SVM SVMCV;
    LogisticRegression LOGITCV;
    DecisionTree TREECV;
    static NeuralNetwork NETCV;
    static KNN KNNCV;
    static AdaBoost forestCV;

    private static boolean trained = false;
    static int choice2;

    private int prediction;
    static private int classSize;

    //classifier trained booleans (just 1 for now to test)
    boolean trainedLDA;
    boolean trainedSVM;
    boolean trainedLOGIT;
    boolean trainedTREE;
    boolean trainedNET;
    boolean trainedKNN;
    boolean trainedFOREST;

    static boolean trained2 = false;

    static int nIMUSensors = 0;

    static FeatureCalculator fcalc2 = new FeatureCalculator();

    static int choice = 3;

    public Classifier() {

    }

    public void setnIMUSensors(int imus){
        nIMUSensors = imus;
    }

    public void Train(ArrayList<DataVector> trainVector, ArrayList<Integer> Classes) {

        classSize = Classes.size();
        classes = new int[classSize];
        trainVectorP = new double[trainVector.size()][numFeatures * 8 + nIMUSensors];//make this dynamic yo
        for (int i = 0; i < trainVector.size(); i++) {
            for (int j = 0; j < numFeatures * 8 + nIMUSensors; j++) {
                trainVectorP[i][j] = trainVector.get(i).getValue(j).doubleValue();//invalid index 8 size is 8
            }
        }

        for (int j = 0; j < Classes.size(); j++) {
            classes[j] = Classes.get(j);
        }

        trained = true;
        trained2 = true;
        switch (choice) {
            case 0:
                trainLDA();
                break;
            case 1:
                trainSVM();
                break;
            case 2:
                trainLogit();
                break;
            case 3:
                trainTree();
                break;
            case 4:
                trainNet();
                break;
            case 5:
                trainKNN();
                break;
            case 6:
                trainAdaBoost();
                break;
        }
    }

    public void setChoice(int newChoice) {
        trained2 = false;

        if (trained) {//must re train if the a new lda is chosen.. NEED feature that checks if one has already been trained so it doesnt train the same one twice!!!

            switch (newChoice) {
                case 0:
                    trainLDA();
//                    Log.d(TAG, "Cross Validation LDA choice: ");
                    ArrayList<Float> tempLDA = crossAccuracy(fcalc2.getSamplesClassifier(), fcalc2.getGesturesSize(), 5);
                    break;
                case 1:
                    trainSVM();
//                    Log.d(TAG, "Cross Validation SVM choice: ");
                    ArrayList<Float> tempSVM = crossAccuracy(fcalc2.getSamplesClassifier(), fcalc2.getGesturesSize(), 5);
                    break;
                case 2:
                    trainLogit();
//                    Log.d(TAG, "Cross Validation Logit choice: ");
                    ArrayList<Float> tempLogit = crossAccuracy(fcalc2.getSamplesClassifier(), fcalc2.getGesturesSize(), 5);
                    break;
                case 3:
                    trainTree();
//                    Log.d(TAG, "Cross Validation Tree choice: ");
                    ArrayList<Float> tempTree = crossAccuracy(fcalc2.getSamplesClassifier(), fcalc2.getGesturesSize(), 5);
                    break;
                case 4:
                    trainNet();
//                    Log.d(TAG, "Cross Validation Net choice: ");
                    ArrayList<Float> tempNet = crossAccuracy(fcalc2.getSamplesClassifier(), fcalc2.getGesturesSize(), 5);
                    break;
                case 5:
                    trainKNN();
//                    Log.d(TAG, "Cross Validation KNN choice: ");
                    ArrayList<Float> tempKNN = crossAccuracy(fcalc2.getSamplesClassifier(), fcalc2.getGesturesSize(), 5);
                    break;
                case 6:
                    trainAdaBoost();
//                    Log.d(TAG, "Cross Validation Forest choice: ");
                    ArrayList<Float> tempForrest = crossAccuracy(fcalc2.getSamplesClassifier(), fcalc2.getGesturesSize(), 5);
                    break;
            }
        }
        choice = newChoice;
        trained2 = true;
    }

    public void featVector(DataVector Features) {
        features = new double[Features.getLength()];
        for (int i = 0; i < Features.getLength(); i++) {
            features[i] = Features.getValue(i).doubleValue();
        }
    }

    //if flag is turned on (found in newChoice), predict or else return 1000
    public int predict(DataVector Features) {
        featVector(Features);
        if (trained2) {
            switch (choice) {
                case 0:
//                    Log.d(TAG, "LDA");
                    prediction = lda.predict(features);
                    break;
                case 1:
                    prediction = svm.predict(features);
//                    Log.d(TAG, "SVM");
                    if (prediction >= 3) {
                        prediction = previousPrediction;
                    }
                    break;
                case 2:
//                    Log.d(TAG, "LOGIT");
                    prediction = logit.predict(features);
                    //Log.d(TAG, "Logistic Regression");
                    break;
                case 3:
//                    Log.d(TAG, "TREE");
                    prediction = tree.predict(features);
                    //Log.d(TAG, "Tree");
                    break;
                case 4:
//                    Log.d(TAG, "NET");
                    prediction = net.predict(features);
                    //Log.d(TAG, "Net");
                    break;
                case 5:
//                    Log.d(TAG, "KNN");
                    prediction = knn.predict(features);
                    //Log.d(TAG, "KNN");
                    break;
                case 6:
//                    Log.d(TAG, "FOREST");
                    prediction = forest.predict(features);
                    //Log.d(TAG, "AdaBoost");
                    break;
            }
//            Log.d("TIME", String.valueOf(System.currentTimeMillis() - MyoGattCallback.superTimeInitial));
            return prediction;
        }
        return 1000;
    }

    public int predictTest(DataVector Features) {
        featVector(Features);
        //depending on choice, predict using classifier
        switch (choice) {
            case 0:
                prediction = LDACV.predict(features);
                break;
            case 1:
                prediction = SVMCV.predict(features);
                if (prediction > 3) {
                    prediction = previousPrediction;
                }
                break;
            case 2:
                prediction = LOGITCV.predict(features);
                break;
            case 3:
                prediction = TREECV.predict(features);
                break;
            case 4:
                prediction = NETCV.predict(features);
                break;
            case 5:
                prediction = KNNCV.predict(features);
                //KNN
                break;
            case 6:
                prediction = forestCV.predict(features);
                //Adaboost
                break;
        }
        return prediction;
    }

    /*
    -training/setting up all the different classifiers
    -probably still have to add a boolean, *see QDA (not tested yet)
    */
    public void trainLDA() {
        //if selected gestures is not zero
//        Toast.makeText(activity, "Training LDA", Toast.LENGTH_SHORT).show();
        if (!trainedLDA) {
            lda = new LDA(trainVectorP, classes, 0);
            trainedLDA = true;
        }
        choice = 0;
    }

    public void trainSVM() {
        if (!trainedSVM) {
//            Toast.makeText(activity, "Training SVM", Toast.LENGTH_SHORT).show();

            svm = new SVM<>(new LinearKernel(), 10.0, classSize + 1, SVM.Multiclass.ONE_VS_ALL);//classSize + 1
            svm.learn(trainVectorP, classes);
            //svm.learn(trainVectorP, classes);
            svm.finish();
            trainedSVM = true;
        }
        choice = 1;
    }

    public void trainLogit() {
        if (!trainedLOGIT) {
//            Toast.makeText(activity, "Training Logit", Toast.LENGTH_SHORT).show();
//            Log.d("2", "222");
            logit = new LogisticRegression(trainVectorP, classes);
            trainedLOGIT = true;
        }
//        Log.d("3", "333");
        choice = 2;
    }

    public void trainTree() {
        if (!trainedTREE) {
            tree = new DecisionTree(trainVectorP, classes, 350);//in theory, greater the integer: more accurate but slower | lower the integer: less accurate but faster however, i didn't notice a difference
            trainedTREE = true;
        }
    }

    public void trainNet() {
        if (!trainedNET) {
            net = new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY, NeuralNetwork.ActivationFunction.SOFTMAX, numFeatures * 8, 30, classSize + 1); //100
            net.learn(trainVectorP, classes);
            trainedNET = true;
        }
    }

    public void trainKNN() {
//        Log.d(TAG, "Made it to KNN");
        if (!trainedKNN) {
            knn = KNN.learn(trainVectorP, classes, (int) Math.sqrt((double) classSize));
            trainedKNN = true;
        }
    }

    public void trainAdaBoost() {
        if (!trainedFOREST) {
            forest = new AdaBoost(trainVectorP, classes, 100, 64);
            trainedFOREST = true;
        }
    }


    ArrayList<Float> crossAccuracy(ArrayList<DataVector> data, int nClass, int parts) {
        //Example values seen in comments using 3 gestures for 100 samples each and dividing all(300) into 5 parts
        int correct = 0;
        int total = 0;
        ArrayList<Float> cM = new ArrayList<>();
        int[][] confMatrix = new int[nClass][nClass];
        trainClasses = new int[nClass * 80];

        //Separates feature vectors according to the gesture being done
        ArrayList<ArrayList<DataVector>> separateData = new ArrayList<>();

        for (int classIndex = 0; classIndex < nClass; classIndex++) {
            ArrayList<DataVector> temp = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                if (classIndex == data.get(i).getFlag()) {
                    temp.add(data.get(i));
                }
            }
            separateData.add(temp);
        }

        //separateData matrix changes to sepData ARRAY
        DataVector[] sepData = new DataVector[separateData.size() * separateData.get(0).size()];
        for (int i = 0; i < separateData.size(); i++) {
            int value = i * separateData.get(i).size();
            for (int j = 0; j < separateData.get(i).size(); j++) {
                sepData[value] = separateData.get(i).get(j);
                value++;
            }
        }

        CrossValidation cv = new CrossValidation(data.size() / nClass, parts);

        for (int kfold = 0; kfold < parts; kfold++) {

            DataVector[] train = new DataVector[((nClass * samples) / parts) * (parts - 1)]; //240
            DataVector[] train2 = new DataVector[((nClass * 100) / parts) * (parts - 1)];
            // 3 x 80
            DataVector[][] auxMatrix = new DataVector[nClass][(samples / parts) * (parts - 1)];
            for (int classes = 1; classes <= nClass; classes++) {
                DataVector[] aux = Arrays.copyOfRange(sepData, (classes - 1) * samples, samples * classes);
                train2 = Math.slice(aux, cv.train[kfold]);
                int it = 0;
                for (int col = 0; col < auxMatrix[classes - 1].length; col++) {
                    auxMatrix[classes - 1][col] = train2[it];
                    it++;
                }
            }

            //Putting values from the matrix into the array
            for (int i = 0; i < auxMatrix.length; i++) {
                int value = i * auxMatrix[i].length;
                for (int j = 0; j < auxMatrix[i].length; j++) {
                    train[value] = auxMatrix[i][j];
                    ++value;
                }
            }

            //------------------TRAINING PHASE--------------------
            //TrainVector 240 x 40
            trainVectorCV = new double[train.length][train[0].getVectorData().size()];

            for (int x = 0; x < train.length; x++) {
                for (int y = 0; y < train[x].getLength(); y++) {
                    trainVectorCV[x][y] = train[x].getValue(y).doubleValue();
                }
            }

            //classes has to be 80 of 0, 80 of 1, etc...
            for (int i = 0; i < nClass; i++) {
                int index = i * ((samples / parts) * (parts - 1));
                for (int j = 0; j < ((samples / parts) * (parts - 1)); j++) {
                    trainClasses[index] = i;
                    index++;
                }
            }

            //SWITCH FOR ALL NEW INSTANCES OF CLASSIFIER MODELS
            //model = new LDA (trainVectorCV,trainClasses,0);

            switch (choice) {
                case 0: //Works on the app
                    LDACV = new LDA(trainVectorCV, trainClasses, 0);
                    break;
                case 1:
                    //TOO SLOW to be used in app
                    //Making it learn once at least makes it somewhat usable??
                    SVMCV = new SVM<>(new LinearKernel(), 10.0, nClass + 1, SVM.Multiclass.ONE_VS_ALL); //classSize + 1
                    SVMCV.learn(trainVectorCV, trainClasses);
                    //SVMCV.learn(trainVectorCV, trainClasses);
                    SVMCV.finish();
                    break;
                case 2: //Works on the app
                    LOGITCV = new LogisticRegression(trainVectorCV, trainClasses);
                    break;
                case 3: //Works but it's kind of slow when outputing gesture decision
                    TREECV = new DecisionTree(trainVectorCV, trainClasses, 350);
                    break;
                case 4: //Giving low percentages in confusion matrix
                    NETCV = new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY, NeuralNetwork.ActivationFunction.SOFTMAX, numFeatures * 8, 30, nClass + 1);//trainClasses.length); //80 classSize +1
                    NETCV.learn(trainVectorCV, trainClasses);
                    break;
                case 5: //NOT working
                    KNNCV = KNN.learn(trainVectorCV, trainClasses, (int) Math.sqrt((double) classSize));
                    //KNN
                    break;
                case 6: //Works on the app
                    forestCV = new AdaBoost(trainVectorCV, trainClasses, 100, 64);
                    //Adaboost
                    break;
            }


            //------------------TESTING PHASE--------------------
            DataVector[] test = new DataVector[nClass * (samples / parts)]; // 60
            DataVector[] test2 = new DataVector[((nClass * 100) / parts) * (parts - 1)];
            DataVector[][] auxMatrixTest = new DataVector[nClass][samples / parts]; //3 x 20

            for (int classes = 1; classes <= nClass; classes++) {
                DataVector[] aux = Arrays.copyOfRange(sepData, (classes - 1) * samples, samples * classes);
                test2 = Math.slice(aux, cv.test[kfold]);
                int it2 = 0;
                for (int col = 0; col < auxMatrixTest[classes - 1].length; col++) {
                    auxMatrixTest[classes - 1][col] = test2[it2];
                    it2++;
                }
            }

            //Putting values from the matrix into the array
            for (int i = 0; i < auxMatrixTest.length; i++) {
                int value = i * auxMatrixTest[i].length;
                for (int j = 0; j < auxMatrixTest[i].length; j++) {
                    test[value] = auxMatrixTest[i][j];
                    ++value;
                }
            }

            //Test is an 60 x 40 array of dataVectors
            for (int i = 0; i < test.length; i++) {
                /*
                int index4 = 0;
                double [] dataVectorPredict = new double[test[0].getVectorData().size()]; //40

                for(int j = 0;j < test[0].getLength();j++){
                    dataVectorPredict[index4] = test[i].getValue(j).doubleValue();
                    index4++;
                }*/

                total++;
                int pred = predictTest(test[i]);

                confMatrix[(int) test[i].getFlag()][(int) pred]++;
                if (test[i].getFlag() == pred) {
                    correct++;
                }
            }
        }

        cM.add(0, (float) correct / (float) total);
        for (int i = 0; i < nClass; i++) {
            for (int j = 0; j < nClass; j++) {
                cM.add(1 + j + i * nClass, samples * nClass * (float) confMatrix[i][j] / total);
            }
        }
        //Just to display results
        for (int x = 0; x < cM.size(); x++) {
//            Log.d("Cross Validation: ", String.valueOf(cM.get(x)));
        }
        return cM;
    }
}