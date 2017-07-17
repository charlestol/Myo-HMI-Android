package example.ASPIRE.MyoHMI_Android;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.echo.holographlibrary.LineGraph;
import com.github.mikephil.charting.charts.RadarChart;

import static android.R.attr.bitmap;
import static android.content.Context.BLUETOOTH_SERVICE;
import static example.ASPIRE.MyoHMI_Android.R.id.imageView;

/**
 * Created by User on 2/28/2017.
 */

public class EmgFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "Tab2Fragment";

    private static final int REQUEST_ENABLE_BT = 1;

    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
//    private TextView emgDataText;
    private TextView gestureText;
    private BluetoothLeScanner mLEScanner;

    private MyoGattCallback mMyoCallback;
    private MyoCommandList commandList = new MyoCommandList();

    private String deviceName;

    private LineGraph graph;
    private RadarChart mChart;
    private Plotter plotter;
    Activity activity;

    private ScanCallback scanCallback = new ScanCallback() {};

    private boolean click = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_emg, container, false);
        assert v != null;

//        emgDataText = (TextView)v.findViewById(R.id.emgDataTextView);
        gestureText = (TextView)v.findViewById(R.id.gestureTextView);
        gestureText.setTextColor(Color.rgb(38,38,38));
        graph = (LineGraph) v.findViewById(R.id.holo_graph_view);
//        mChart = (RadarChart) v.findViewById(R.id.chart1);
//        mChart.setNoDataText("");
        mHandler = new Handler();
        activity = this.getActivity();

        ImageView imgView = (ImageView) v.findViewById(R.id.imageView);
        imgView.setDrawingCacheEnabled(true);
        imgView.setOnTouchListener(changeColorListener);

        BluetoothManager mBluetoothManager = (BluetoothManager) getActivity().getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

        Intent intent = getActivity().getIntent();
        deviceName = intent.getStringExtra(ListActivity.TAG);

        if (deviceName != null) {
            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                // Scanning Time out by Handler.
                // The device scanning needs high energy.

                /***********CO BY CHARLES***********/
                /*
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLEScanner.stopScan(mScanCallback);
                    }
                }, SCAN_PERIOD);
                */
                mLEScanner.startScan(mScanCallback);
            }
        }

        View emgbutton = v.findViewById(R.id.iEMG);
        emgbutton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v){
            clickedemg(v);
        }
        });

        View vibbutton = v.findViewById(R.id.iVibrate);
        vibbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                clickedvib(v);
            }
        });

        return v;
    }

    @Override
    public void onClick(View v){
        clickedemg(v);
    }

    public void clickedemg(View v) {
        click=!click;
        Log.d("Tag", String.valueOf(click));
        if (click) {
            if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendImuAndEmg())) {
                Log.d(TAG, "False EMG");
            }
        } else{
            if (mBluetoothGatt == null
                    || !mMyoCallback.setMyoControlCommand(commandList.sendUnsetData())
                    /*|| !mMyoCallback.setMyoControlCommand(commandList.sendNormalSleep())*/) {
                Log.d(TAG, "False Data Stop");
            }
        }
    }

    public void clickedvib(View v) {
        if (mBluetoothGatt == null || !mMyoCallback.setMyoControlCommand(commandList.sendVibration3())) {
            Log.d(TAG, "False Vibrate");
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
        BluetoothDevice device = result.getDevice();
        if (deviceName.equals(device.getName())) {
            mLEScanner.stopScan(scanCallback);
            // Trying to connect GATT

            plotter = new Plotter(mHandler, graph);
            mMyoCallback = new MyoGattCallback(mHandler, gestureText, plotter);
            mBluetoothGatt = device.connectGatt(getActivity(), false, mMyoCallback);
            mMyoCallback.setBluetoothGatt(mBluetoothGatt);
        }
        }
    };

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

    private final View.OnTouchListener changeColorListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Bitmap source = v.getDrawingCache();
            Bitmap bmp = Bitmap.createBitmap(source, 0, 0, v.getWidth(), v.getHeight());
            int color = bmp.getPixel((int) event.getX(), (int) event.getY());
            if(plotter!=null) {
                if (color == Color.rgb(89, 140, 175)) {
                    Log.d("Clicked on ", "blue");
                    plotter.setEMG(color, 2);
                } else if (color == Color.rgb(100, 169, 95)) {
                    Log.d("Clicked on ", "green");
                    plotter.setEMG(color, 1);
                } else if (color == Color.rgb(169, 95, 95)) {
                    Log.d("Clicked on ", "clay");
                    plotter.setEMG(color, 0);
                } else if (color == Color.rgb(189, 75, 167)) {
                    Log.d("Clicked on ", "magenta");
                    plotter.setEMG(color, 7);
                } else if (color == Color.rgb(171, 89, 43)) {
                    Log.d("Clicked on ", "brown");
                    plotter.setEMG(color, 6);
                } else if (color == Color.rgb(94, 62, 130)) {
                    Log.d("Clicked on ", "purple");
                    plotter.setEMG(color, 5);
                } else if (color == Color.rgb(171, 21, 21)) {
                    Log.d("Clicked on ", "red");
                    plotter.setEMG(color, 4);
                } else if (color == Color.rgb(64, 64, 64) || (color < -12500000 && color > -15800000)) {//gray or the logo color
                    Log.d("Clicked on ", "gray");
                    plotter.setEMG(Color.rgb(64, 64, 64), 3);
                }
                return true;
            }
            else{
                return false;
            }

        }
    };

}
