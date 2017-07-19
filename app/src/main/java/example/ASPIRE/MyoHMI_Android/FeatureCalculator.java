package example.ASPIRE.MyoHMI_Android;
import java.util.*;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
    int threshold = 5; //According to Ian using 3 gives better results
    int nFeatures = 5;
    int nIMUFeatures = 3;
    int nSensors = 8;
    private ArrayList<DataVector> samplebuf = new ArrayList<>(128);
    private LinkedHashMap<Integer, Integer> freq;
    int ibuf = 0;
    int bufsize = 128;
    int flag;
    int lastCall, firstCall;

//    private TextView liveView;
    public static Activity classAct;
    public static TextView liveView;
    public static int prediction;

    int winsize = 40;    //window size
    int winincr = 8;    //separation length between windows
    int winnext = winsize + 1;    //winsize + 2 samples until first feature

    private Plotter plotter;

    private static Classifier classifier = new Classifier();
    private int currentClass = 0;
    public static ArrayList<Integer> classes = new ArrayList<>();

    public static int sampleCount;

//    private FileSaver saveToFile = new FileSaver();

    twoDimArray featemg;

    int nSamples = 100; //Kattia: Should be set by the user and have interaction in GUI

    public static boolean train = false;
    public static boolean classify = false;

    static ArrayList<DataVector> samplesClassifier = new ArrayList<DataVector>();

    public static DataVector aux;//does it have to be public?

    public void setTrain(boolean inTrain){train = inTrain;}

    public void setClassify(boolean inClassify){classify = inClassify;}

    public boolean getTrain(){
        return  train;
    }

    public ArrayList<DataVector> getSamplesClassifier(){return samplesClassifier;}

//    public FeatureCalculator(){}

    public static Context context;
    private static View view;
    private static List<String> gestures;

    public FeatureCalculator(View v, Activity act){
        classAct = act;
        view = v;
        liveView = (TextView)view.findViewById(R.id.gesture_detected);
    }

    public FeatureCalculator(Plotter plot){
        plotter = plot;
    }

    private void featCalc() {
        byte signLast;
        byte slopLast;
        int j, k;
        double Delta_2;
        //ArrayList<Number> sMAV = new ArrayList<Number>(8); //Used to store the values of the MAV from all 8 channels and used by the sMAV feature
        float [] sMAVS = new float[8];
        float MMAV = 0;
        //featemg = new float[nFeatures][nSensors];//already full of zeros

       //twoDimArray featemg = new twoDimArray();
        featemg = new twoDimArray();
        featemg.createMatrix(nFeatures,nSensors);

        // lock();
        //for each sensor calculate features
        for (int sensor = 0; sensor < nSensors; sensor++) {//loop through each EMG pod (8)
            k = (firstCall + bufsize - 1) % bufsize;    //one before window start   // (41 - 40 + 1 = 2) - 1
            j = (k + bufsize - 1) % bufsize;    //        two before ws(firstCall)  // 0

            signLast = 0;
            slopLast = 0;

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
                if (signLast == 9 || signLast == 6) {
                    featemg.setMatrixValue(2, sensor, featemg.getMatrixValue(2, sensor) + 1);
                    //featemg[2][sensor]++;
                }
                if (slopLast == 9 || slopLast == 6) {
                    featemg.setMatrixValue(3, sensor, featemg.getMatrixValue(3, sensor) + 1);
                    //featemg[3][sensor]++;
                }
//                String printstring = String.valueOf(Delta_2);
//                Log.d("Delta_1: ", printstring);
                //Check finishes here

                // use <<< or >>> for UNSIGNED shifting in java Note: In C++ is << or >> for unsigned shifting
                //Changed signLast and slopLast from int to char
                signLast = (byte) ((byte)(signLast << 2) & (byte) 15);
                slopLast = (byte) ((byte)(slopLast << 2) & (byte) 15);

                /*
                featemg[0][sensor] += Math.abs(samplebuf.get(k).getVectorData().get(sensor).floatValue());
                featemg[1][sensor] += (float) Math.abs(Delta_2);
                */

                featemg.setMatrixValue(0, sensor, featemg.getMatrixValue(0, sensor) + Math.abs(samplebuf.get(k).getVectorData().get(sensor).floatValue()));
                featemg.setMatrixValue(1, sensor, featemg.getMatrixValue(1, sensor) + (float)Math.abs(Delta_2));
            }

            /*
            featemg[0][sensor] = featemg[0][sensor] / winsize;//MAV of One sensor
            featemg[1][sensor] = featemg[1][sensor] / winsize;
            featemg[2][sensor] = featemg[2][sensor] * 100 / winsize;
            featemg[3][sensor] = featemg[3][sensor] * 100 / winsize;
            */

            featemg.setMatrixValue(0, sensor, featemg.getMatrixValue(0, sensor) / winsize);
            featemg.setMatrixValue(1, sensor, featemg.getMatrixValue(1, sensor) / winsize);
            featemg.setMatrixValue(2, sensor, featemg.getMatrixValue(2, sensor) * 100 / winsize);
            featemg.setMatrixValue(3, sensor, featemg.getMatrixValue(3, sensor) * 100 / winsize);

            /*
            //if (sensor != 0){......
            sMAV.add(featemg[0][sensor]);
            MMAV += featemg[0][sensor];
            */
            //sMAV.add(featemg.getMatrixValue(0, sensor));
            sMAVS[sensor] = featemg.getMatrixValue(0, sensor);
            MMAV +=  featemg.getMatrixValue(0, sensor);

            if (sensor == (nSensors - 1)) {//don't want to use all
                for (int l = 0; l < nSensors; l++) {
                    //featemg[4][l] = (sMAV.get(l).floatValue() / (MMAV/8)) * 25;//Scaling otherwise the value is so small
                    //featemg.setMatrixValue(4, l, (sMAV.get(l).floatValue() / (MMAV/8)) * 25);
                    featemg.setMatrixValue(4, l, (sMAVS[l] /(MMAV/8)) * 25);
                    //it does not display on the EMG graph
                }

                featemg.setMatrixValue(4, nSensors-1, MMAV/8);

//                Log.d(TAG, "Feature 0 " + featemg.getInnerArray(0).toString());
//                Log.d(TAG, "Feature 1 " + featemg.getInnerArray(1).toString());
//                Log.d(TAG, "Feature 2 " + featemg.getInnerArray(2).toString());
//                Log.d(TAG, "Feature 3 " + featemg.getInnerArray(3).toString());
//                Log.d(TAG, "Feature 4 " + featemg.getInnerArray(4).toString());

                plotter.pushFeaturePlotter(featemg.getInnerArray(3));
            }
        }
        //unlock();
    }

    //Making the 100 x 40 matrix
    public void pushClassifyTrainer(DataVector inFeatemg){
//        saveToFile.save(inFeatemg);
        samplesClassifier.add(inFeatemg);
        classes.add(currentClass);
//        Log.d("classes: ", Arrays.toString());
//        inFeatemg.printDataVector("In Classify Trainer");
        Log.d(TAG, String.valueOf(samplesClassifier.size()));
    }

    public static void pushClassifier(DataVector inFeatemg){
        prediction = classifier.predict(inFeatemg, 0);
//        inFeatemg.printDataVector("Predict Vector");
        if(liveView != null){
            classAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    liveView.setText(gestures.get(prediction));
                }
            });
        }
//        Log.d("Prediction: ", String.valueOf(prediction));//prediction
    }

    public void sendClasses(List<String> classes){
        gestures = classes;
    }

    //Printing matrix for debug
    public void printClassiferTrainer(){
        for(int i = 0; i < samplesClassifier.size(); i++){
            samplesClassifier.get(i).printDataVector(TAG);
        }
    }

    public void pushFeatureBuffer(DataVector data){ //actively accepts single EMG data vectors and runs calculations when window is reached
        //push new dvec into circular buffer
        //samplebuf.add(0, data); //add new emg data to beginning of buffer
        samplebuf.add(ibuf, data);
        if(samplebuf.size() > bufsize)//limit size of buffer to bufsize
            samplebuf.remove(samplebuf.size() - 1);
        //Log.d("ibuf, winnext", Integer.toString(ibuf) + " , " + Integer.toString(winnext));
        //process window if next window is reached
        if (ibuf == winnext)//start calculating
        {
            lastCall = winnext;
            firstCall = (lastCall - winsize + bufsize + 1) % bufsize;
            featCalc();
            aux = buildDataVector();
            aux.setTimestamp(data.getTimestamp());

            if(getTrain()) {
                pushClassifyTrainer(aux);
//                Log.d("Features: ", )
//                aux.printDataVector("Features: ");
                if (samplesClassifier.size() % (nSamples) == 0 && samplesClassifier.size() != 0) { //triggers
                    setTrain(false);
                    currentClass++;
                }
            }
            else if(classify) {
                pushClassifier(aux);
            }
            winnext = (winnext + winincr) % bufsize;
        }
        ibuf = ++ibuf & (bufsize - 1);//make buffer circular
    }

    public static void Train(){
        classifier.Train(samplesClassifier, classes);
    }

    private DataVector buildDataVector()//ignoring grid and imu for now, assuming all features are selected
    {
        // Count total EMG features to send
        int emgct = 0;

//        for(int i=0;i<nFeatures;i++){
//            if (checkEMGGrid[i][j] == true) emgct+=emgct*8; //check if features are selected
//        }

        emgct = 40;//5 features, 8 sensors//delete after for loop above is working

        ArrayList<Number> temp = new ArrayList<Number>(emgct);//float* temp = new float[emgct]();
        //lock();

        int n = 0;
        for (int i = 0; i < nFeatures; i++)
        {
            //group features per sensor
            for (int j = 0; j < nSensors; j++)
            {
                //temp[n] = FeatEMG[i][j];
                //Log.d(TAG, Float.toString(FeatEMG.getValue2(i, j)));
                //temp.add(n, FeatEMG.getValue2(i, j));
                //Kattia: change
                temp.add(n, featemg.getMatrixValue(i,j));
                //temp.add(n, featemg[i][j]);
                //Log.d(TAG, Float.toString(Float.valueOf(temp.get(n)));
                //Log.d(TAG, temp.get(n).toString());
                n++;
            }
        }

        //no imu stuff for now
        //Log.d(TAG, Integer.toString(temp.size()));
        //unlock();
        //DataVector dvec = new DataVector(1, emgct, temp);
        DataVector dvec = new DataVector(true, 0, emgct, temp, 0000000);
        //temp = null;
        return dvec;
    }

    private void setWindowSize(int newWinsize){
        winsize = newWinsize;
        //if winsize is larger than buffer
        //if winsize is considerably smaller than buffer
        if (winsize + 10 > bufsize){
            bufsize = winsize + 10;
            //lock();
            samplebuf = null;//delete[] samplebuf;
            samplebuf = new ArrayList<DataVector>(bufsize); //samplebuf = new DataVector[bufsize]; //arraylist holding bufsize amount of datavectors
            //unlock();
        }
        reset();
    }

    private void setWindowIncrement(int newWinincr){
        if (winincr + 10 > bufsize)
        {
            bufsize = winincr + 10;
            //lock();
            samplebuf = null;//delete[] samplebuf;
            samplebuf = new ArrayList<DataVector>(bufsize); //samplebuf = new DataVector[bufsize]; //arraylist holding bufsize amount of datavectors
            //unlock();
        }

        winincr = newWinincr;
    }

    private void reset(){
        ibuf = 0;
        //imuibuf = 0;
        winnext = winsize + 1;
    }


    public void featCalcIMU() {
    }

    public void findMajorityFlag() {
        freq = new LinkedHashMap<Integer, Integer>();
        //lock();
        //count flag freq
        int index = firstCall;
        for (int i = 0; i < winsize; i++)
        {
            index = index % bufsize;
            freq.put(samplebuf.get(index).getFlag(), freq.get(samplebuf.get(index).getFlag())+1); //count occurrences of each flag
            index++;
        }
        //unlock();

        //find most freq
        //ties results in the lower integer flag
        Iterator it = freq.entrySet().iterator();
        int max = 0;
        while(it.hasNext()){
            Map.Entry itEntry = (Map.Entry) it.next();
            int i = (int) itEntry.getValue();
            if(max < i){
                max=i;
                //set flag
                flag = (int) itEntry.getKey();
            }
        }
    }

    //Appends 2D arrays
    public static float[][] append(float[][]a, float[][]b){
        float[][] result = new float[a.length + b.length][];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, 0, b.length);
        return  result;
    }

    public void printDataVector(double inDouble){
        double debug = inDouble;
        Log.d("TAG",Double.toString(debug));
    }
    //public DataVector buildDataVector(){};
}

//Two dimensional array class made to help in the implementation of featEMG
class twoDimArray{

    //matrix is our featEMG matrix
    ArrayList<ArrayList<Float>> matrix = new ArrayList<ArrayList<Float>>();

    //Init matrix to the desired dimensions all with 0
    //Note: row refers to nFeatures and columns refers to nSensors
    public void createMatrix(int numRow, int numCol){
        for(int i = 0; i < numRow; i++){
            ArrayList<Float> innerArray = new ArrayList<Float>();
            matrix.add(innerArray);
            for(int j = 0; j < numCol; j++) {
                innerArray.add((float) 0);
            }
        }
    }

    //Get value at specified row and column
    public float getMatrixValue(int inRow, int inCol){
        return matrix.get(inRow).get(inCol);
    }

    //Set value at specified row and column
    public void setMatrixValue(int numRow, int numCol, float data){
        ArrayList<Float> temp;
        temp = matrix.get(numRow);
        temp.set(numCol, data);
        matrix.set(numRow, temp);
    }

    //Return specific ROW
    public ArrayList<Float> getInnerArray(int inRow){
        return matrix.get(inRow);
    }

    public void addRow(ArrayList inRow){
        matrix.add(inRow);
    }
}