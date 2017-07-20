package example.ASPIRE.MyoHMI_Android;
import org.apache.commons.lang3.ArrayUtils;
import java.util.ArrayList;
/*import smile.classification.LDA;
import smile.classification.QDA;
import smile.classification.SVM;
import smile.classification.LogisticRegression;*/
import smile.classification.*;
import smile.math.kernel.LinearKernel;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Alex on 7/3/2017.
 */

public class Classifier{
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
}