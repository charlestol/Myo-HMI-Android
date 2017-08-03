package example.ASPIRE.MyoHMI_Android;

import java.util.*;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    int nFeatures = 6;
    int nIMUFeatures = 3;
    int nSensors = 8;
    private ArrayList<DataVector> samplebuf = new ArrayList<>(128);
    private LinkedHashMap<Integer, Integer> freq;
    int ibuf = 0;
    int bufsize = 128;
    int flag;
    int lastCall, firstCall;
    int pushCount=0;

    //    private TextView liveView;
    public static Activity classAct;
    public static TextView liveView, status;
    public static ProgressBar progressBar;
    public static ImageButton uploadButton;
    public static int prediction;

    int winsize = 40;    //window size
    int winincr = 8;    //separation length between windows
    int winnext = winsize + 1;    //winsize + 2 samples until first feature

    private Plotter plotter;

    /**
     * Charles 7/18
     **/
    FeatureFragment featureFragment = new FeatureFragment();
    boolean[] featSelected = new boolean[nFeatures];
    zprogress zprogress = new zprogress();
    int numFeatSelected = 6;

    private static Classifier classifier = new Classifier();
    private int currentClass = 0;
    public static ArrayList<Integer> classes = new ArrayList<>();

    public static int sampleCount;

//    private FileSaver saveToFile = new FileSaver();

    public static twoDimArray featemg;

    int nSamples = 100; //Kattia: Should be set by the user and have interaction in GUI

    public static boolean train = false;

    public static boolean classify = false;

    static ArrayList<DataVector> samplesClassifier = new ArrayList<DataVector>();
    static ArrayList<DataVector> featureData = new ArrayList<DataVector>();

    public ArrayList<DataVector> getSamplesClassifier() {
        return samplesClassifier;
    }

    public ArrayList<DataVector> getFeatureData(){return featureData;}

    public int getGesturesSize(){return gestures.size();}

    public static DataVector[] aux;//does it have to be public?

    public void setTrain(boolean inTrain) {
        train = inTrain;
    }

    public void setClassify(boolean inClassify) {
        classify = inClassify;
    }

    public boolean getTrain() {
        return train;
    }

    public FeatureCalculator() {
    }

    public static Context context;
    private static View view;
    private static List<String> gestures;

    public FeatureCalculator(View v, Activity act) {
        classAct = act;
        view = v;
        liveView = (TextView) view.findViewById(R.id.gesture_detected);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        uploadButton = (ImageButton) view.findViewById(R.id.im_upload);
        //status = (TextView) view.findViewById(R.id.txt_status);
    }

    public FeatureCalculator(Plotter plot) {
        plotter = plot;
    }

    private void featCalc() {
        ArrayList<ArrayList<Float>> AUMatrix = new ArrayList<>();
        byte signLast;
        byte slopLast;
        int j, k;
        double Delta_2;
        float[] sMAVS = new float[8];//Used to store the values of the MAV from all 8 channels and used by the sMAV feature
        float MMAV = 0;

        featemg = new twoDimArray();
        featemg.createMatrix(nFeatures, nSensors);

        setSelectedFeatures();

        // lock();
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

                // use <<< or >>> for UNSIGNED shifting in java Note: In C++ is << or >> for unsigned shifting
                //Changed signLast and slopLast from int to char
                signLast = (byte) ((byte) (signLast << 2) & (byte) 15);
                slopLast = (byte) ((byte) (slopLast << 2) & (byte) 15);


                //featemg[0][] -> MAV
                //featemg[1][] -> wav
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
                /*
                Log.d(TAG, "Feature 0 " + featemg.getInnerArray(0).toString());
                Log.d(TAG, "Feature 1 " + featemg.getInnerArray(1).toString());
                Log.d(TAG, "Feature 2 " + featemg.getInnerArray(2).toString());
                Log.d(TAG, "Feature 3 " + featemg.getInnerArray(3).toString());
                Log.d(TAG, "Feature 4 " + featemg.getInnerArray(4).toString());
                */
                plotter.pushFeaturePlotter(featemg);
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

/*
        for(int x = 0; x < featemg.getInnerArray(5).size(); x++){
            Log.d(TAG, String.valueOf(featemg.getMatrixValue(5,x)));
        }
        Log.d(TAG, "-----------------------------------------------------------");
        //unlock();
*/
    }

    //Making the 100 x 40 matrix
    public void pushClassifyTrainer(DataVector[] inFeatemg) {
//        ArrayList<Number> line = new ArrayList<>();
//        for(int i=0;i<6;i++){//for saving ALL feature data not just ones selected
//            featureData.add(new DataVector(0,8,featemg.getInnerArray(i)));
//            DataVector dvec2 = new DataVector(0,8,featemg.getInnerArray(i));
//            dvec2.printDataVector("hey there: " + String.valueOf(i));
//        }
        featureData.add(inFeatemg[1]);
        samplesClassifier.add(inFeatemg[0]);
        classes.add(currentClass);
        Log.d(TAG, String.valueOf(samplesClassifier.size()));
    }

    public static void pushClassifier(DataVector inFeatemg) {
        prediction = classifier.predict(inFeatemg);
        //inFeatemg.printDataVector("Predict Vector");
        if (liveView != null) {
            classAct.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (prediction != 1000) {
                        liveView.setText(gestures.get(prediction));
                        progressBar.setVisibility(View.INVISIBLE);
                        uploadButton.setVisibility(View.VISIBLE);
                    } else {
                        liveView.setText("Training Classifier");
                    }
                }
            });
        }
        //Log.d("Prediction: ", String.valueOf(prediction));//prediction
    }

    public static void setTextNull() {
        liveView.setText("Training Classifier");
    }

    public void sendClasses(List<String> classes) {
        gestures = classes;
    }

    //Printing matrix for debug
    public void printClassiferTrainer() {
        for (int i = 0; i < samplesClassifier.size(); i++) {
            samplesClassifier.get(i).printDataVector(TAG);

        }
    }

    public void pushFeatureBuffer(DataVector data) { //actively accepts single EMG data vectors and runs calculations when window is reached
        //push new dvec into circular buffer
        //samplebuf.add(0, data); //add new emg data to beginning of buffer
        samplebuf.add(ibuf, data);
        if (samplebuf.size() > bufsize)//limit size of buffer to bufsize
            samplebuf.remove(samplebuf.size() - 1);
        //Log.d("ibuf, winnext", Integer.toString(ibuf) + " , " + Integer.toString(winnext));
        //process window if next window is reached
        if (getTrain()) {
            aux[0].setFlag(currentClass);
        }

        if (ibuf == winnext)//start calculating
        {
            lastCall = winnext;
            firstCall = (lastCall - winsize + bufsize + 1) % bufsize;
            featCalc();
            aux = buildDataVector();
            aux[0].setTimestamp(data.getTimestamp());

            if (getTrain()) {
                aux[0].setFlag(currentClass);
                pushClassifyTrainer(aux);
//                Log.d("Features: ", )
//                aux.printDataVector("Features: ");
                if (samplesClassifier.size() % (nSamples) == 0 && samplesClassifier.size() != 0) { //triggers
                    setTrain(false);
                    currentClass++;
                }
            } else if (classify) {
                pushClassifier(aux[0]);
            }
            winnext = (winnext + winincr) % bufsize;
        }
        ibuf = ++ibuf & (bufsize - 1); //make buffer circular
    }

    public static void Train() {
        classifier.Train(samplesClassifier, classes);
        //Kattia: Testing CrossValidation
//        ArrayList<Float> temp = classifier.crossAccuracy(samplesClassifier, gestures.size(),5);
    }

    private DataVector[] buildDataVector()//ignoring grid and imu for now, assuming all features are selected
    {
        // Count total EMG features to send

        /**
         * Charles 7/18
         **/
        int emgct = numFeatSelected * 8;
        numFeatSelected = 6; //Resets the number of features selected to 5

        ArrayList<Number> temp = new ArrayList<Number>(emgct);
        ArrayList<Number> temp1 = new ArrayList<Number>(emgct);
        //lock();

        int n = 0;
        int k = 0;
        int tempIndex = 0;
        int temp1Index = 0;

        for (int i = 0; i < nFeatures; i++) {
        //group features per sensor
            for (int j = 0; j < nSensors; j++) {

                if (featSelected[i] == true) {
                temp.add(n, featemg.getMatrixValue(tempIndex, j));
                n++;
                }
            }
            tempIndex++;
        }

        for (int i = 0; i < 6; i++) {
            //group features per sensor
            for (int j = 0; j < nSensors; j++) {
                temp1.add(k, featemg.getMatrixValue(temp1Index, j));
                k++;
            }
            temp1Index++;
        }

        DataVector dvec = new DataVector(true, 0, emgct, temp, 0000000);
        DataVector dvec1 = new DataVector(true, 0, 48, temp1, 0000000);
        dvec1.printDataVector("Hey there: ");
        DataVector dvecArr[] = {dvec, dvec1};
        return dvecArr;
    }

    private void setWindowSize(int newWinsize) {
        winsize = newWinsize;
        //if winsize is larger than buffer
        //if winsize is considerably smaller than buffer
        if (winsize + 10 > bufsize) {
            bufsize = winsize + 10;
            //lock();
            samplebuf = null;//delete[] samplebuf;
            samplebuf = new ArrayList<DataVector>(bufsize); //samplebuf = new DataVector[bufsize]; //arraylist holding bufsize amount of datavectors
            //unlock();
        }
        reset();
    }

    private void setWindowIncrement(int newWinincr) {
        if (winincr + 10 > bufsize) {
            bufsize = winincr + 10;
            //lock();
            samplebuf = null;//delete[] samplebuf;
            samplebuf = new ArrayList<DataVector>(bufsize); //samplebuf = new DataVector[bufsize]; //arraylist holding bufsize amount of datavectors
            //unlock();
        }

        winincr = newWinincr;
    }

    private void reset() {
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
        for (int i = 0; i < winsize; i++) {
            index = index % bufsize;
            freq.put(samplebuf.get(index).getFlag(), freq.get(samplebuf.get(index).getFlag()) + 1); //count occurrences of each flag
            index++;
        }
        //unlock();

        //find most freq
        //ties results in the lower integer flag
        Iterator it = freq.entrySet().iterator();
        int max = 0;
        while (it.hasNext()) {
            Map.Entry itEntry = (Map.Entry) it.next();
            int i = (int) itEntry.getValue();
            if (max < i) {
                max = i;
                //set flag
                flag = (int) itEntry.getKey();
            }
        }
    }

    public void printDataVector(double inDouble) {
        double debug = inDouble;
        Log.d("TAG", Double.toString(debug));
    }

    public void setSelectedFeatures() {
        for (int i = 0; i < 6; i++) {
            featSelected[i] = featureFragment.getFeatSelected()[i];
            //Log.d(TAG, "Feature bool: " + featSelected[i]);

            if (featSelected[i] == false) {
                numFeatSelected--;
            }
        }
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