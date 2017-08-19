package example.ASPIRE.MyoHMI_Android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;

import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;
import com.echo.holographlibrary.LinePoint;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import java.util.ArrayList;
import java.lang.Math;
import java.util.Arrays;

import android.graphics.Matrix;
import android.widget.ImageView;

import static android.R.attr.pivotX;
import static android.R.attr.pivotY;

/**
 * Created by Alex on 6/30/2017.
 */

public class Plotter extends Activity {
    //boolean emg;
    private static RadarChart mChart;
    private LineGraph lineGraph;
    private static Handler mHandler;
    private static int currentTab = 0; //current tab from MainActivity
    private int lineColor = Color.rgb(64, 64, 64);

    public boolean startup = true;

    int[][] dataList1_a = new int[8][50];
    int[][] dataList1_b = new int[8][50];

    private int nowGraphIndex = 3;

    private ArrayList<Number> f0, f1, f2, f3, f4, f5;

    private static boolean[] featuresSelected = new boolean[]{true, true, true, true, true, true};

    private int w, x, y, z;
    private double pitch, roll, yaw;

    public Plotter() {
    }

    public Plotter(RadarChart chart) {

        mChart = chart;
        mHandler = new Handler();

        mChart.setNoDataText("");
        mChart.setBackgroundColor(Color.TRANSPARENT);
        mChart.getDescription().setEnabled(false);
        mChart.setWebLineWidth(1f);
        mChart.setWebColor(Color.LTGRAY);
        mChart.setWebLineWidthInner(1f);
        mChart.setWebColorInner(Color.LTGRAY);
        mChart.setWebAlpha(100);
//        mChart.getLegend().setTextSize(20f);
        mChart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);

        XAxis xAxis = mChart.getXAxis();
        //xAxis.setTypeface(mTfLight);
        xAxis.setTextSize(9f);
        xAxis.setYOffset(0f);
        xAxis.setXOffset(0f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private String[] mActivities = new String[]{"1", "2", "3", "4", "5", "6", "7", "8"};

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mActivities[(int) value % mActivities.length];
            }
        });
        YAxis yAxis = mChart.getYAxis();
        //yAxis.setTypeface(mTfLight);
        yAxis.setLabelCount(8, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(128);
        yAxis.setDrawLabels(false);

        twoDimArray featemg = new twoDimArray();
        featemg.createMatrix(6, 8);

        this.setCurrentTab(1);

        for(int i=0; i<8; i++){
            for (int j=0;j<6;j++){
                featemg.setMatrixValue(j, i, 128);
            }
        }

        this.pushFeaturePlotter(featemg);

        for(int i=0; i<8; i++){
            for (int j=0;j<6;j++){
                featemg.setMatrixValue(j, i, 0);
            }
        }

        this.pushFeaturePlotter(featemg);

//        this.setCurrentTab(0);

    }

    public Plotter(Handler handler, LineGraph line) {
        mHandler = handler;
        lineGraph = line;
    }

    public void pushPlotter(byte[] data) {
//        setData();
        if (data.length == 16 && (currentTab == 0||currentTab==1)) {
//        if ((data.length == 16 && currentTab == 0)||startup) {

//            Log.d("tag", String.valueOf(startup));

            mHandler.post(new Runnable() {
                @Override
                public void run() {
//                    dataView.setText(callback_msg);
//                    Log.d("In: ", "EMG Graph");
                    lineGraph.removeAllLines();

                    for (int inputIndex = 0; inputIndex < 8; inputIndex++) {
                        dataList1_a[inputIndex][0] = data[0 + inputIndex];
                        dataList1_b[inputIndex][0] = data[7 + inputIndex];
                    }
                    // 折れ線グラフ
                    int number = 50;
                    int addNumber = 100;
                    Line line = new Line();
                    while (0 < number) {
                        number--;
                        addNumber--;

                        //１点目add
                        if (number != 0) {
                            for (int setDatalistIndex = 0; setDatalistIndex < 8; setDatalistIndex++) {
                                dataList1_a[setDatalistIndex][number] = dataList1_a[setDatalistIndex][number - 1];
                            }
                        }
                        LinePoint linePoint = new LinePoint();
                        linePoint.setY(dataList1_a[nowGraphIndex][number]); //ランダムで生成した値をSet
                        linePoint.setX(addNumber); //x軸を１ずつずらしてSet
                        //linePoint.setColor(Color.parseColor("#9acd32")); // 丸の色をSet

                        line.addPoint(linePoint);
                        //2点目add
                        /////number--;
                        addNumber--;
                        if (number != 0) {
                            for (int setDatalistIndex = 0; setDatalistIndex < 8; setDatalistIndex++) {
                                dataList1_b[setDatalistIndex][number] = dataList1_b[setDatalistIndex][number - 1];
                            }
                        }
                        linePoint = new LinePoint();
                        linePoint.setY(dataList1_b[nowGraphIndex][number]); //ランダムで生成した値をSet
                        linePoint.setX(addNumber); //x軸を１ずつずらしてSet
                        //linePoint.setColor(Color.parseColor("#9acd32")); // 丸の色をSet

                        line.addPoint(linePoint);
                    }

                    line.setColor(lineColor); // 線の色をSet

                    line.setShowingPoints(false);
                    lineGraph.addLine(line);
                    lineGraph.setRangeY(-128, 128); // 表示するY軸の最低値・最高値 今回は0から1まで

                }
            });
        } else if (data.length == 20 && currentTab == 1) {//emg=false;
//            chartView.setAnimation(an);
//            Log.d("In: ", "IMU Graph");
            w = data[0];
            x = data[1];
            y = data[2];
            z = data[3];

            roll = Math.atan(2 * (x * w + z * y) / (2 * (x ^ 2 + y ^ 2) - 1));

//            Log.d("roll", String.valueOf(roll));

//            Log.d("IMU: ", Arrays.toString(data));

//            Log.d("roll: ", String.valueOf(roll));

//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    mChart.spin(1,0,30, Easing.EasingOption.Linear);
//                }
//            });
        }
    }

    public void pushFeaturePlotter(twoDimArray featureData) {
        if (mChart != null && currentTab == 1) {
//        if (mChart != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                    f0 = featureData.getInnerArray(0);
                    f1 = featureData.getInnerArray(1);
                    f2 = featureData.getInnerArray(2);
                    f3 = featureData.getInnerArray(3);
                    f4 = featureData.getInnerArray(4);
                    f5 = featureData.getInnerArray(5);

                    ArrayList<RadarEntry> entries0 = new ArrayList<RadarEntry>();
                    ArrayList<RadarEntry> entries1 = new ArrayList<RadarEntry>();
                    ArrayList<RadarEntry> entries2 = new ArrayList<RadarEntry>();
                    ArrayList<RadarEntry> entries3 = new ArrayList<RadarEntry>();
                    ArrayList<RadarEntry> entries4 = new ArrayList<RadarEntry>();
                    ArrayList<RadarEntry> entries5 = new ArrayList<RadarEntry>();

                    for (int i = 0; i < 8; i++) {
                        //2000 per division 14 000 in total
                        entries0.add(new RadarEntry(setMaxValue(f0.get(i).floatValue() * 200)));
                        entries1.add(new RadarEntry(setMaxValue(f1.get(i).floatValue() * 200)));
                        entries2.add(new RadarEntry(setMaxValue(f2.get(i).floatValue() * 200)));
                        entries3.add(new RadarEntry(setMaxValue(f3.get(i).floatValue() * 170)));
                        entries4.add(new RadarEntry(setMaxValue(f4.get(i).floatValue() * 200)));
                        entries5.add(new RadarEntry(setMaxValue(f5.get(i).floatValue() * 200)));

//                        Log.d("asdfadsf", String.valueOf(f3.get(i)));
                    }

                    ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();

                    RadarDataSet set0 = new RadarDataSet(entries0, "MAV");
                    set0.setColor(Color.rgb(123, 174, 157));
                    set0.setFillColor(Color.rgb(78, 118, 118));
                    set0.setDrawFilled(true);
                    set0.setFillAlpha(180);
                    set0.setLineWidth(2f);

                    RadarDataSet set1 = new RadarDataSet(entries1, "WAV");
                    set1.setColor(Color.rgb(241, 148, 138));
                    set1.setFillColor(Color.rgb(205, 97, 85));
                    set1.setDrawFilled(true);
                    set1.setFillAlpha(180);
                    set1.setLineWidth(2f);

                    RadarDataSet set2 = new RadarDataSet(entries2, "Turns");
                    set2.setColor(Color.rgb(175, 122, 197));
                    set2.setFillColor(Color.rgb(165, 105, 189));
                    set2.setDrawFilled(true);
                    set2.setFillAlpha(180);
                    set2.setLineWidth(2f);

                    RadarDataSet set3 = new RadarDataSet(entries3, "Zeros");
                    set3.setColor(Color.rgb(125, 206, 160));
                    set3.setFillColor(Color.rgb(171, 235, 198));
                    set3.setDrawFilled(true);
                    set3.setFillAlpha(180);
                    set3.setLineWidth(2f);

                    RadarDataSet set4 = new RadarDataSet(entries4, "SMAV");
                    set4.setColor(Color.rgb(39, 55, 70));
                    set4.setFillColor(Color.rgb(93, 109, 126));
                    set4.setDrawFilled(true);
                    set4.setFillAlpha(180);
                    set4.setLineWidth(2f);

                    RadarDataSet set5 = new RadarDataSet(entries5, "AdjUnique");
                    set5.setColor(Color.rgb(10, 100, 126)); // 100 50 70
                    set5.setFillColor(Color.rgb(64, 154, 180));
                    set5.setDrawFilled(true);
                    set5.setFillAlpha(180);
                    set5.setLineWidth(2f);

                    if (featuresSelected[0])
                        sets.add(set0);
                    if (featuresSelected[1])
                        sets.add(set1);
                    if (featuresSelected[2])
                        sets.add(set2);
                    if (featuresSelected[3])
                        sets.add(set3);
                    if (featuresSelected[4])
                        sets.add(set4);
                    if (featuresSelected[5])
                        sets.add(set5);

                    //                        set1.setDrawHighlightCircleEnabled(true);
                    //                        set1.setDrawHighlightIndicators(false);

                    if (!sets.isEmpty()) {
                        RadarData data = new RadarData(sets);
                        data.setValueTextSize(18f);
                        data.setDrawValues(false);
                        mChart.setData(data);
                        mChart.notifyDataSetChanged();
                        mChart.invalidate();
                    }
                }
            });
        }else if (mChart==null){
            Log.d("wassup ", "mchart might be null************************************");
        }
    }

    public void setEMG(int color, int emg) {
        lineColor = color;
        nowGraphIndex = emg;
    }

    public void setCurrentTab(int tab) {
//        if(tab==0){
//            startup=false;
//            Log.d("tag", String.valueOf(tab));
//        }

        currentTab = tab;
    }

    public void setFeatures(boolean[] features) {

        featuresSelected = features;

        //if statement for myo connection goes here

//        twoDimArray featemg = new twoDimArray();
//        featemg.createMatrix(6, 8);
//
//        this.setCurrentTab(1);
//
//        for(int i=0; i<8; i++){
//            for (int j=0;j<6;j++){
//                featemg.setMatrixValue(j, i, 128);
//            }
//        }
//
//        this.pushFeaturePlotter(featemg);
//
//        for(int i=0; i<8; i++){
//            for (int j=0;j<6;j++){
//                featemg.setMatrixValue(j, i, 0);
//            }
//        }
//
//        this.pushFeaturePlotter(featemg);
//
    }

    public float setMaxValue(float inValue){
        float value = inValue;
        if (inValue > 14000) {
            value =  14000;
        }
        return value;
    }
}
