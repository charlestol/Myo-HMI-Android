package example;

import java.util.*;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import static java.lang.Math.abs;

/**
 * Created by Alex on 6/19/2017.
 */

public class FeatureCalculator {
    private String TAG = "FeatureCalculator";
    int threshold = 3; //According to Ian using 3 gives better results
    static int nFeatures = 6;
    int nIMUFeatures = 0;
    static int nIMUSensors = 0;
    int nSensors = 8;
    int bufsize = 128;
    private ArrayList<DataVector> samplebuffer = new ArrayList<>(bufsize);
    private ArrayList<DataVector> imusamplebuffer = new ArrayList<>(bufsize);
    private LinkedHashMap<Integer, Integer> freq;
    int ibuf = 0;
    int imuibuf = 0;
    int nDimensions = 10;
    int lastCall, firstCall;
    private twoDimArray featureVector;
    private twoDimArray imuFeatureVector;
    public static int prediction;
    int winsize = 40;    //window size
    int winincr = 8;    //separation length between windows
    int winnext = winsize + 1;    //winsize + 2 samples until first feature
    static boolean[] featSelected = {true, true, true, true, true, true};
    static boolean[] imuSelected = {false, false, false, false, false, false, false, false, false, false};
    int numFeatSelected = 6;
    private static Classifier classifier = new Classifier();
    private int currentClass = 0;
    public static ArrayList<Integer> classes = new ArrayList<>();
    public static twoDimArray featemg;
    public static twoDimArray featimu;
    int nSamples = 100; //Kattia: Should be set by the user and have interaction in GUI
    public static boolean train = false;
    public static boolean classify = false;
    static ArrayList<DataVector> samplesClassifier = new ArrayList<DataVector>();
    static ArrayList<DataVector> featureData = new ArrayList<DataVector>();
    public static DataVector[] aux;//does it have to be public?
    public static boolean trained = false;

    static long startCalc = System.currentTimeMillis();
    static long startClass = System.currentTimeMillis();
    static long startFeature = System.currentTimeMillis();

    public ArrayList<DataVector> getSamplesClassifier() {
        return samplesClassifier;
    }

    public ArrayList<DataVector> getFeatureData(){return featureData;}

    public int getGesturesSize(){return gestures.size();}

    public void setTrain(boolean inTrain) {
        train = inTrain;
    }

    public void setClassify(boolean inClassify) {
        classify = inClassify;
    }

    public boolean getTrain() {
        return train;
    }
    
    public boolean getClassify(){
        return classify;
    }
    
    public int getPrediction(){
        return prediction;
    }

    public FeatureCalculator() {
    }

    private static List<String> gestures;

    public int getSize(){return samplesClassifier.size();}

    //Making the 100 x 40 matrix
    public void pushClassifyTrainer(DataVector[] inFeatemg) {
        featureData.add(inFeatemg[1]);
//        System.out.println(inFeatemg[0].getLength());
        samplesClassifier.add(inFeatemg[0]);
        
        System.out.println("!!! Current Class:" + currentClass + " samplesClassifier.size(): " + samplesClassifier.size());
        
        classes.add(currentClass);
//        Log.d(TAG, String.valueOf(samplesClassifier.size()));
    }

    public static void pushClassifier(DataVector inFeatemg) {
        if(!trained){
            Train();
            trained = true;
        }

        startClass = System.nanoTime();
        
        prediction = classifier.predict(inFeatemg);

        // System.out.print("," + (System.nanoTime() - startClass));
        // System.out.println("Prediction: " + prediction);
        // System.out.println(", " + (System.nanoTime() - startCalc));
    }

    public void sendClasses(List<String> classes) {
        gestures = classes;
    }

    public void pushFeatureBuffer(DataVector data) { //actively accepts single EMG data vectors and runs calculations when window is reached

        samplebuffer.add(ibuf, data);

        // System.out.println(samplebuffer.size());

        if (samplebuffer.size() > bufsize)//limit size of buffer to bufsize
            samplebuffer.remove(samplebuffer.size() - 1);

        if (train) {
            aux[0].setFlag(currentClass);
        }

        if (ibuf == winnext)//start calculating
        {
            startCalc = System.nanoTime();
            lastCall = winnext;
            firstCall = (lastCall - winsize + bufsize + 1) % bufsize;

            startFeature = System.nanoTime();

            featureVector = featCalc(samplebuffer);
            imuFeatureVector = featCalcIMU(imusamplebuffer);
            aux = buildDataVector(featureVector, imuFeatureVector);

           // System.out.print(","+(System.nanoTime() - startFeature));

            aux[0].setTimestamp(data.getTimestamp());

            if(train){
                aux[0].setFlag(currentClass);//dont need this?
                pushClassifyTrainer(aux);
                if (samplesClassifier.size() % (nSamples) == 0 && samplesClassifier.size() != 0) { //triggers
                    setTrain(false);
                    currentClass++;
                }
            }
            else if(classify){
                pushClassifier(aux[0]);
            }
            winnext = (winnext + winincr) % bufsize;
        }

        ibuf = ++ibuf & (bufsize - 1); //make buffer circular
    }

    public static void Train() {
        classifier.Train(samplesClassifier, classes);
    }

    private DataVector[] buildDataVector(twoDimArray featureVector, twoDimArray imuFeatureVector)//ignoring grid and imu for now, assuming all features are selected
    {
        // Count total EMG features to send

        int emgct = numFeatSelected * 8;
        numFeatSelected = 6; //Resets the number of features selected to 5

        ArrayList<Number> temp = new ArrayList<Number>(emgct);
        DataVector dvec1 = null;

        int n = 0;
        int k = 0;
        int tempIndex = 0;
        int temp1Index = 0;

        for (int i = 0; i < nFeatures; i++) {
        //group features per sensor
            if (featSelected[i]) {
                for (int j = 0; j < nSensors; j++) {
                    temp.add(n, featureVector.getMatrixValue(tempIndex, j));
                    n++;
                }
            }
            tempIndex++;
        }

        if(getTrain()) {//during training we wan to save all 8 sensor data
            ArrayList<Number> temp1 = new ArrayList<Number>(emgct);
            for (int i = 0; i < nFeatures; i++) {
                //group features per sensor
                for (int j = 0; j < nSensors; j++) {
                    temp1.add(k, featureVector.getMatrixValue(temp1Index, j));
                    k++;
                }
                temp1Index++;
            }
        }

        DataVector dvec = new DataVector(true, 0, emgct + nIMUSensors, temp, 0000000);//nIMU must become dynamic with UI

        DataVector dvecArr[] = {dvec, dvec1};
        return dvecArr;
    }

        private twoDimArray featCalc(ArrayList<DataVector> samplebuf) {
        ArrayList<ArrayList<Float>> AUMatrix = new ArrayList<>();
        byte signLast;
        byte slopLast;
        int j, k;
        double Delta_2;
        float[] sMAVS = new float[nSensors];//Used to store the values of the MAV from all 8 channels and used by the sMAV feature
        float MMAV = 0;

        featemg = new twoDimArray();
        featemg.createMatrix(nFeatures, nSensors);

        //for each sensor calculate features
        for (int sensor = 0; sensor < nSensors; sensor++) {//loop through each EMG pod (8)
            k = (firstCall + bufsize - 1) % bufsize;    //one before window start   // (41 - 40 + 1 = 2) - 1
            j = (k + bufsize - 1) % bufsize;    //        two before ws(firstCall)  // 0
            ArrayList<Float> tempAU = new ArrayList<>();

            signLast = 0;
            slopLast = 0;

            //Some threshold for zero crossings and slope changes
            Delta_2 = samplebuf.get(k).getVectorData().get(sensor).floatValue() - samplebuf.get(j).getVectorData().get(sensor).floatValue(); //index out of bounds exception

            if (Delta_2 > threshold) {
                slopLast += 4;
            }
            if (Delta_2 < -threshold) {
                slopLast += 8;
            }

            //Beginning of Window???
            if (samplebuf.get(j).getVectorData().get(sensor).floatValue() > threshold) {
                signLast = 4;
            } //Set to a high value?
            if (samplebuf.get(j).getVectorData().get(sensor).floatValue() < -threshold) {
                signLast = 8;
            }//set to a low value?

            for (int i = 0; i < (winsize); i++) //-2
            {
                j = k;                 //prev     //1 - 40
                k = (j + 1) % bufsize; //current  //2 - 41

                Delta_2 = samplebuf.get(k).getVectorData().get(sensor).floatValue() - samplebuf.get(j).getVectorData().get(sensor).floatValue();

                if (samplebuf.get(k).getVectorData().get(sensor).floatValue() > threshold) {
                    signLast += 1;
                }
                if (samplebuf.get(k).getVectorData().get(sensor).floatValue() < -threshold) {
                    signLast += 2;
                }
                if (Delta_2 > threshold) {
                    slopLast += 1;
                }
                if (Delta_2 < -threshold) {
                    slopLast += 2;
                }
                if ((signLast == 9 || signLast == 6)) {
                    featemg.setMatrixValue(2, sensor, featemg.getMatrixValue(2, sensor) + 1);
                }
                if ((slopLast == 9 || slopLast == 6)) {
                    featemg.setMatrixValue(3, sensor, featemg.getMatrixValue(3, sensor) + 1);
                }

                signLast = (byte) ((byte) (signLast << 2) & (byte) 15);
                slopLast = (byte) ((byte) (slopLast << 2) & (byte) 15);

                featemg.setMatrixValue(0, sensor, featemg.getMatrixValue(0, sensor) + Math.abs(samplebuf.get(k).getVectorData().get(sensor).floatValue()));
                featemg.setMatrixValue(1, sensor, featemg.getMatrixValue(1, sensor) + (float) Math.abs(Delta_2));
                tempAU.add(samplebuf.get(k).getVectorData().get(sensor).floatValue());
            }

            featemg.setMatrixValue(0, sensor, featemg.getMatrixValue(0, sensor) / winsize);
            featemg.setMatrixValue(1, sensor, featemg.getMatrixValue(1, sensor) / winsize);
            featemg.setMatrixValue(2, sensor, featemg.getMatrixValue(2, sensor) * 100 / winsize);
            featemg.setMatrixValue(3, sensor, featemg.getMatrixValue(3, sensor) * 100 / winsize);

            //Feature 4 smav
            sMAVS[sensor] = featemg.getMatrixValue(0, sensor);
            MMAV += featemg.getMatrixValue(0, sensor);

            if (sensor == (nSensors - 1)) {//don't want to use all
                for (int l = 0; l < nSensors; l++) {
                    featemg.setMatrixValue(4, l, (sMAVS[l] / (MMAV / 8)) * 25);
                }

                featemg.setMatrixValue(4, nSensors - 1, MMAV / 8);
//                plotter.pushFeaturePlotter(featemg);
            }
            AUMatrix.add(tempAU);
        }

        for(int sensorIt = 0; sensorIt < nSensors; sensorIt++){
            int sensorNext = sensorIt + 1;
            if(sensorNext == 8){ sensorNext = 0;}
            float tempValue = 0;
            for(int it = 0; it < winsize; it++){
                tempValue += Math.abs((AUMatrix.get(sensorIt).get(it).floatValue()/ featemg.getMatrixValue(0,sensorIt)) - (AUMatrix.get(sensorNext).get(it).floatValue()/ featemg.getMatrixValue(0,sensorNext)));
            }
            //Feature 5 Adjacency Uniqueness
            featemg.setMatrixValue(5, sensorIt,(tempValue/winsize) * 25); // multiply by 25 to scale the value of tempValue/winsize
        }

        return featemg;
    }

    private void setWindowSize(int newWinsize) {
        winsize = newWinsize;
        if (winsize + 10 > bufsize) {
            bufsize = winsize + 10;
            samplebuffer = null;//delete[] samplebuf;
            samplebuffer = new ArrayList<DataVector>(bufsize); //samplebuf = new DataVector[bufsize]; //arraylist holding bufsize amount of datavectors
        }
        reset();
    }

    private void setWindowIncrement(int newWinincr) {
        if (winincr + 10 > bufsize) {
            bufsize = winincr + 10;
            samplebuffer = null;//delete[] samplebuf;
            samplebuffer = new ArrayList<DataVector>(bufsize); //samplebuf = new DataVector[bufsize]; //arraylist holding bufsize amount of datavectors
        }
        winincr = newWinincr;
    }

    private void reset() {
        ibuf = 0;
        winnext = winsize + 1;
    }

    public void pushIMUFeatureBuffer(DataVector data){
        imusamplebuffer.add(imuibuf, data);
        if (imusamplebuffer.size() > bufsize)//limit size of buffer to bufsize
            imusamplebuffer.remove(samplebuffer.size() - 1);
        imuibuf = ++imuibuf % (bufsize);
    }

    public twoDimArray featCalcIMU(ArrayList<DataVector> imusamplebuf) {
        int i;
        float sum;
        featimu = new twoDimArray();
        featimu.createMatrix(nIMUFeatures, nDimensions);
        for (int ft = 0; ft < nIMUFeatures; ft++) {
            for (int d = 0; d < nDimensions; d++) {
                i = (imuibuf + bufsize - (winsize / 4)) % bufsize;
                sum = 0;
                while (i != imuibuf) {
                    sum += imusamplebuf.get(i).getValue(d).floatValue();
                    i = (i + bufsize + 1) % bufsize;
                }
                featimu.setMatrixValue(ft, d, sum/(winsize/4));
            }
        }
        return featimu;
    }

    public void setFeatSelected(boolean[] boos){
        featSelected = boos;
    }

    public void setIMUSelected(boolean[] boos){
        imuSelected = boos;
    }

    public void setNumIMUSelected(int imus){
        nIMUSensors = imus;
    }

    public void setNumFeatSelected(int feats){
        nFeatures = feats;
    }

}

//Two dimensional array class made to help in the implementation of featEMG
class twoDimArray {

    //matrix is our featEMG matrix
    ArrayList<ArrayList<Number>> matrix = new ArrayList<ArrayList<Number>>();
    int numRow;
    int numCol;

    //Init matrix to the desired dimensions all with 0
    //Note: row refers to nFeatures and columns refers to nSensors
    public void createMatrix(int numRow, int numCol) {
        this.numRow = numRow;
        this.numCol = numCol;
        for (int i = 0; i < numRow; i++) {
            ArrayList<Number> innerArray = new ArrayList<Number>();
            matrix.add(innerArray);
            for (int j = 0; j < numCol; j++) {
                innerArray.add((float) 0);
            }
        }
    }

    //Get value at specified row and column
    public float getMatrixValue(int inRow, int inCol) {
        return matrix.get(inRow).get(inCol).floatValue();
    }

    //Set value at specified row and column
    public void setMatrixValue(int numRow, int numCol, float data) {
        ArrayList<Number> temp;
        temp = matrix.get(numRow);
        temp.set(numCol, data);
        matrix.set(numRow, temp);
    }

    public ArrayList<DataVector> getDataVector(){
        ArrayList<DataVector> data = new ArrayList<>();
        for (int i=0;i<numRow;i++){
            ArrayList<Number> row = this.getInnerArray(i);
            data.add(new DataVector(0,row.size(),row));
        }
        return data;
    }

    //Return specific ROW
    public ArrayList<Number> getInnerArray(int inRow) {
        return matrix.get(inRow);
    }
    public void addRow(ArrayList inRow) {
        matrix.add(inRow);
    }
}