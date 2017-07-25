package example.ASPIRE.MyoHMI_Android;
import java.util.ArrayList;
import java.util.Arrays;

import smile.classification.*;
import smile.math.kernel.LinearKernel;
import smile.validation.CrossValidation;
import smile.math.Math;

import android.app.Activity;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Alex on 7/3/2017.
 */

public class Classifier {
    private String TAG = "Classifier";
    static int numFeatures = 5;
    double[][] trainVectorP;
    LDA lda;
    QDA qda;
    SVM svm;
    LogisticRegression logit;
    DecisionTree tree;
    NeuralNetwork net;
    double[] features;
    int[] classes;
    int[] testclasses = new int[3];
    static Activity activity;
    int [] trainClasses = new int [240];

    int samples = 100;
    double [][] trainVectorCV;
    LDA LDACV;
    QDA QDACV;
    SVM SVMCV;
    LogisticRegression LOGITCV;
    DecisionTree TREECV;
    NeuralNetwork NETCV;

    private boolean trained = false;

    static int choice=0;

    public int prediction;
    public int classSize;

    //classifier trained booleans (just 1 for now to test
    boolean trainedQDA;

    public Classifier(Activity activity){
        this.activity = activity;
    }

    public Classifier(){

    }

    public void Train(ArrayList<DataVector> trainVector, ArrayList<Integer> Classes) {
        classSize = Classes.size();
        classes = new int[classSize];
        trainVectorP = new double[trainVector.size()][numFeatures * 8];
        for (int i = 0; i < trainVector.size(); i++) {
            for (int j = 0; j < numFeatures * 8; j++) {
                trainVectorP[i][j] = trainVector.get(i).getValue(j).doubleValue();//invalid index 8 size is 8
            }
        }

        for (int j = 0; j < Classes.size(); j++) {
            classes[j] = Classes.get(j);
        }

//        if (trainVector.size() > 0) {
            trained = true;
            switch (choice) {
                case 0:
                    trainLDA();
                    break;
                case 1:
                    trainQDA();
                    break;
                case 2:
                    trainSVM();
                    break;
                case 3:
                    trainLogit();
                    break;
                case 4:
                    trainTree();
                    break;
                case 5:
                    trainNet();
                    break;
            }
//        } else{
//            Toast.makeText(activity, "No Gestures Selected", Toast.LENGTH_SHORT);
//        }
    }

    public void featVector(DataVector Features) {
        features = new double[Features.getLength()];
        for(int i=0;i<Features.getLength();i++){
            features[i]=Features.getValue(i).doubleValue();
        }
    }

    public int predict(DataVector Features) {
        featVector(Features);
        //depending on choice, predict using classifier
        switch(choice) {
            case 0:
                prediction = lda.predict(features);
                break;
            case 1:
                prediction = qda.predict(features);
                break;
            case 2:
                prediction = svm.predict(features);
                break;
            case 3:
                prediction = logit.predict(features);
                break;
            case 4:
                prediction = tree.predict(features);
                break;
            case 5:
                prediction = net.predict(features);
                break;
        }
        return prediction;
    }

    public int predictTest(DataVector Features) {
        featVector(Features);
        //depending on choice, predict using classifier
        switch(choice) {
            case 0:
                prediction = LDACV.predict(features);
                break;
            case 1:
                prediction = QDACV.predict(features);
                break;
            case 2:
                prediction = SVMCV.predict(features);
                break;
            case 3:
                prediction = LOGITCV.predict(features);
                break;
            case 4:
                prediction = TREECV.predict(features);
                break;
            case 5:
                prediction = NETCV.predict(features);
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
            lda = new LDA(trainVectorP, classes, 0);
    }

    public void trainQDA() {
        if(!trainedQDA) {
            qda = new QDA(trainVectorP, classes, 0);
            trainedQDA = true;
        }
        else { return; }
    }

    public void trainSVM() {
        svm = new SVM<>(new LinearKernel(), 10.0, classSize + 1, SVM.Multiclass.ONE_VS_ALL);
        svm.learn(trainVectorP, classes);
        svm.learn(trainVectorP, classes);
        svm.finish();
    }

    public void trainLogit() {
        logit = new LogisticRegression(trainVectorP, classes);
    }

    public void trainTree() {
        tree = new DecisionTree(trainVectorP, classes, 350);//in theory, greater the integer: more accurate but slower | lower the integer: less accurate but faster however, i didn't notice a difference
    }

    public void trainNet() {
        net = new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY, NeuralNetwork.ActivationFunction.SOFTMAX, 40, 30, classSize + 1);
        net.learn(trainVectorP, classes);
    }

    public void setChoice(int newChoice){
        choice = newChoice;

        if(trained){//must re train if the a new lda is chosen.. NEED feature that checks if one has already been trained so it doesnt train the same one twice!!!
            switch(choice) {
                case 0:
                    trainLDA();
                    break;
                case 1:
                    trainQDA();
                    break;
                case 2:
                    trainSVM();
                    break;
                case 3:
                    trainLogit();
                    break;
                case 4:
                    trainTree();
                    break;
                case 5:
                    trainNet();
                    break;
            }
        }
    }

    ArrayList<Float> crossAccuracy(ArrayList<DataVector> data, int nClass, int parts){
        //Example values seen in comments using 3 gestures for 100 samples each and dividing all(300) into 5 parts
        int correct = 0;
        int total = 0;
        ArrayList<Float> cM = new ArrayList<>();
        int [][] confMatrix = new int[nClass][nClass];

        //Separates feature vectors according to the gesture being done
        ArrayList<ArrayList<DataVector >> separateData = new ArrayList<>();

        for(int classIndex = 0; classIndex < nClass; classIndex++){
            ArrayList<DataVector> temp = new ArrayList<>();
            for(int i = 0; i < data.size(); i++){
                if(classIndex == data.get(i).getFlag()){
                    temp.add(data.get(i));
                }
            }
            separateData.add(temp);
        }

        //separateData matrix changes to sepData ARRAY
        DataVector [] sepData = new DataVector[separateData.size() * separateData.get(0).size()];
        for(int i = 0; i < separateData.size(); i++){
            int value = i * separateData.get(i).size();
            for(int j = 0; j < separateData.get(i).size(); j++){
                sepData[value] = separateData.get(i).get(j);
                value++;
            }
        }

        CrossValidation cv = new CrossValidation(data.size()/nClass, parts);

        for(int kfold = 0; kfold < parts; kfold++){

            DataVector [] train = new DataVector[((nClass * samples) / parts) * (parts - 1)]; //240
            DataVector [] train2 = new DataVector[((nClass * 100) / parts) * (parts - 1)];
            // 3 x 80
            DataVector [][] auxMatrix = new DataVector[nClass][(samples/parts) * (parts -1)];
            for(int classes = 1; classes <= nClass; classes++){
                DataVector [] aux = Arrays.copyOfRange(sepData, (classes - 1) * samples , samples * classes);
                train2 = Math.slice(aux, cv.train[kfold]);
                int it = 0;
                for(int col = 0; col < auxMatrix[classes-1].length; col++){
                    auxMatrix[classes-1][col] = train2[it];
                    it++;
                }
            }

            //Putting values from the matrix into the array
            for(int i = 0; i < auxMatrix.length; i++){
                int value = i * auxMatrix[i].length;
                for(int j = 0; j < auxMatrix[i].length; j++){
                    train[value] = auxMatrix[i][j];
                    ++value;
                }
            }

            //------------------TRAINING PHASE--------------------
            //TrainVector 240 x 40
            trainVectorCV = new double[train.length][train[0].getVectorData().size()];

            for(int x = 0; x < train.length; x++){
                for(int y = 0; y < train[x].getLength(); y++){
                    trainVectorCV[x][y] = train[x].getValue(y).doubleValue();
                }
            }

            //classes has to be 80 of 0, 80 of 1, etc...
            for(int i = 0; i < nClass; i++){
                int index = i * ((samples / parts) * (parts - 1));
                for(int j = 0; j < ((samples / parts) * (parts - 1)); j++){
                    trainClasses[index] = i;
                    index++;
                }
            }

            //SWITCH FOR ALL NEW INSTANCES OF CLASSIFIER MODELS
            //model = new LDA (trainVectorCV,trainClasses,0);

            switch(choice) {
                case 0:
                    LDACV = new LDA (trainVectorCV,trainClasses,0);
                    break;
                case 1:
                    QDACV = new QDA (trainVectorCV,trainClasses,0);
                    break;
                case 2: //NOT working
                    SVMCV = new SVM<>(new LinearKernel(), 10.0, classSize + 1, SVM.Multiclass.ONE_VS_ALL);
                    SVMCV.learn(trainVectorCV, trainClasses);
                    SVMCV.learn(trainVectorCV, trainClasses);
                    SVMCV.finish();
                    break;
                case 3:
                    LOGITCV = new LogisticRegression(trainVectorCV, trainClasses);
                    break;
                case 4:
                    TREECV = new DecisionTree(trainVectorCV, trainClasses, 350);
                    break;
                case 5: //Can't be selected in the app
                    NETCV = new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY, NeuralNetwork.ActivationFunction.SOFTMAX, 40, 30, classSize + 1);
                    NETCV.learn(trainVectorCV, trainClasses);
            }


            //------------------TESTING PHASE--------------------
            DataVector [] test = new DataVector[nClass * (samples / parts)]; // 60
            DataVector [] test2 = new DataVector[((nClass * 100) / parts) * (parts - 1)];
            DataVector [][] auxMatrixTest = new DataVector[nClass][samples / parts]; //3 x 20

            for(int classes = 1; classes <= nClass; classes++){
                DataVector [] aux = Arrays.copyOfRange(sepData, (classes - 1) * samples , samples * classes);
                test2 = Math.slice(aux, cv.test[kfold]);
                int it2 = 0;
                for(int col = 0; col < auxMatrixTest[classes-1].length; col++){
                    auxMatrixTest[classes-1][col] = test2[it2];
                    it2++;
                }
            }

            //Putting values from the matrix into the array
            for(int i = 0; i < auxMatrixTest.length; i++){
                int value = i * auxMatrixTest[i].length;
                for(int j = 0; j < auxMatrixTest[i].length; j++){
                    test[value] = auxMatrixTest[i][j];
                    ++value;
                }
            }

            //Test is a 60 x 40 an array of dataVectors
            for(int i = 0; i < test.length; i++){
                /*
                int index4 = 0;
                double [] dataVectorPredict = new double[test[0].getVectorData().size()]; //40

                for(int j = 0;j < test[0].getLength();j++){
                    dataVectorPredict[index4] = test[i].getValue(j).doubleValue();
                    index4++;
                }*/

                total++;
                int pred = predictTest(test[i]);

                confMatrix[(int)test[i].getFlag()][(int) pred]++;
                if(test[i].getFlag() == pred){
                    correct++;
                }
            }
        }

        cM.add(0, (float) correct / (float) total);
        for(int i = 0; i < nClass; i++){
            for(int j = 0; j < nClass; j++){
                cM.add(1 + j + i*nClass, samples * nClass * (float)confMatrix[i][j]/ total);
            }
        }
        //Just to display results
        for(int x = 0; x < cM.size(); x++){
            Log.d("Cross Validation: ", String.valueOf(cM.get(x)));
        }
        return cM;
    }
}