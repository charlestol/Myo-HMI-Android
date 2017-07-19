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

public class Plotter extends Activity{
    //boolean emg;
    private static RadarChart mChart;
    private RadarChart myChart;
    private LineGraph lineGraph;
    private static Handler mHandler;
    private static int currentTab = 0; //current tab from MainActivity
    private int lineColor = Color.rgb(64, 64, 64);

    int[][] dataList1_a = new int[8][50];
    int[][] dataList1_b = new int[8][50];

    private int nowGraphIndex = 3;

    private int w,x,y,z;
    private double pitch, roll, yaw;

    public Plotter(){}

    public Plotter(RadarChart chart){

        mChart = chart;
        mChart.setNoDataText("");
        mChart.setBackgroundColor(Color.TRANSPARENT);
        mChart.getDescription().setEnabled(false);
        mChart.setWebLineWidth(1f);
        mChart.setWebColor(Color.LTGRAY);
        mChart.setWebLineWidthInner(1f);
        mChart.setWebColorInner(Color.LTGRAY);
        mChart.setWebAlpha(100);

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
    }

    public Plotter(Handler handler, LineGraph line){
        mHandler = handler;
        lineGraph = line;
    }

    public void pushPlotter(byte[] data){
//        setData();
        if (data.length==16 && currentTab==0){//emg=true;
//            for(int i=0;i<16;i++){//can we do this all at once?
//                emgDatas[i] = data[0+i];
////                emgData2[i] = data[7+i];
//            }

            mHandler.post(new Runnable() {
                @Override
                public void run() {
//                    dataView.setText(callback_msg);
//                    Log.d("In: ", "EMG Graph");
                    lineGraph.removeAllLines();

                    for(int inputIndex = 0;inputIndex<8;inputIndex++) {
                        dataList1_a[inputIndex][0] = data[0+inputIndex];
                        dataList1_b[inputIndex][0] = data[7+inputIndex];
                    }
                    // 折れ線グラフ
                    int number = 50;
                    int addNumber = 100;
                    Line line = new Line();
                    while (0 < number) {
                        number--;
                        addNumber--;

                        //１点目add
                        if(number != 0){
                            for(int setDatalistIndex = 0;setDatalistIndex < 8;setDatalistIndex++){
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
                        if(number != 0){
                            for(int setDatalistIndex = 0;setDatalistIndex < 8;setDatalistIndex++) {
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
        }

        else if(data.length==20 && currentTab==1){//emg=false;
//            chartView.setAnimation(an);
//            Log.d("In: ", "IMU Graph");
            w = data[0]; x = data[1]; y = data[2]; z = data[3];

            roll = Math.atan(2*(x*w + z*y)/(2*(x^2+y^2)-1));

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

    public static void pushFeaturePlotter(ArrayList<Float> featData) {
        if(mChart != null && currentTab==1) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
//                    Log.d("In: ", "Radar Graph");
                    ArrayList<RadarEntry> entries1 = new ArrayList<RadarEntry>();
                    for (int i = 0; i < featData.size(); i++) {//only takes first 8 need to allow it to take second 8
                        entries1.add(new RadarEntry(featData.get(i) * 200));
                    }

                    RadarDataSet set1 = new RadarDataSet(entries1, "EMG Data");
                    ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
                    set1.setColor(Color.rgb(78, 118, 118));
                    set1.setFillColor(Color.rgb(123, 174, 157));
                    set1.setDrawFilled(true);
                    set1.setFillAlpha(180);
                    set1.setLineWidth(2f);
                    sets.add(set1);
                    //                        set1.setDrawHighlightCircleEnabled(true);
                    //                        set1.setDrawHighlightIndicators(false);
                    RadarData data = new RadarData(sets);
                    data.setValueTextSize(8f);
                    data.setDrawValues(false);
                    mChart.setData(data);
                    mChart.notifyDataSetChanged();
                    mChart.invalidate();
                }
            });
        }
    }

    public void setEMG(int color, int emg){
        lineColor = color;
        nowGraphIndex = emg;
    }

    public void setCurrentTab(int tab){
        currentTab = tab;
    }
}
