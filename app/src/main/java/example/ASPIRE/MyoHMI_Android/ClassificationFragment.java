package example.ASPIRE.MyoHMI_Android;

import android.annotation.SuppressLint;
import android.app.Activity;


import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.service.notification.Condition;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.LogPrinter;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.os.CountDownTimer;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static example.ASPIRE.MyoHMI_Android.ListActivity.TAG;
import static example.ASPIRE.MyoHMI_Android.R.id.textView;
import static example.ASPIRE.MyoHMI_Android.R.layout.countdown;
import static java.lang.Character.FORMAT;

/**
 * Created by User on 2/28/2017.
 */

public class ClassificationFragment extends Fragment {

    private FeatureCalculator fcalc;
    private List<String> ListElementsArrayList;
    private List<String> ClassifierArrayList;
    private List<String> Copy_of_selectedItemsList;
    private SaveData saver;
    private ArrayList<DataVector> trainData;
    private int count = 4;
    private Handler mHandler = new Handler();
    private int gestureCounter = 0;
    private TextView liveView, status;
    private TextView or_text;
    private CloudUpload cloudUpload;
    //private Classifier classifier;//for making toast on this activity

    EditText GetValue;
    ImageButton addButton;
    ImageButton deleteButton;
    ImageButton clearButton;
    ImageButton uploadButton;
    //Button showButton;
    ImageButton trainButton;
    ImageButton loadButton;
    ImageButton resetButton;
    ListView listview_Classifier;
    ListView listview;
    ProgressBar progressBar;

    //create an ArrayList object to store selected items
    ArrayList<String> selectedItems = new ArrayList<String>();

    Classifier classifier = new Classifier();

    ServerCommunicationThread comm = new ServerCommunicationThread();

    String[] ListElements = new String[]{
            "Rest",
            "Wave In",
            "Wave Out",
            "Fist",
            "Point",
            "Open Hand",
            "Supination",
            "Pronation"
    };

    String[] classifier_options = new String[]{
            "LDA",
            "SVM",
            "Logistic Regression",
            "Decision Tree",
            "Neural Net",
            "KNN",
            "Adaboost"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_classification, container, false);

        assert v != null;

//        final Runnable r1, r2;

        fcalc = new FeatureCalculator(v, getActivity());
        classifier = new Classifier(getActivity());
        saver = new SaveData(this.getContext());

        or_text = (TextView) v.findViewById(R.id.or_text);
        liveView = (TextView) v.findViewById(R.id.gesture_detected);
        GetValue = (EditText) v.findViewById(R.id.add_gesture_text);
        trainButton = (ImageButton) v.findViewById(R.id.bt_train);
        loadButton = (ImageButton) v.findViewById(R.id.bt_load);
        addButton = (ImageButton) v.findViewById(R.id.im_add);
        deleteButton = (ImageButton) v.findViewById(R.id.im_delete);
        uploadButton = (ImageButton) v.findViewById(R.id.im_upload);
        resetButton = (ImageButton) v.findViewById(R.id.im_reset);
//        listview = (ListView) v.findViewById(R.id.listView);
        listview = (ListView) v.findViewById(R.id.listView);
        listview_Classifier = (ListView) v.findViewById(R.id.listView1);
//        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listview_Classifier.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        ListElementsArrayList = new ArrayList<String>(Arrays.asList(ListElements));
        ClassifierArrayList = new ArrayList<String>(Arrays.asList(classifier_options));

        cloudUpload = new CloudUpload(getActivity());

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_multiple_choice, ListElementsArrayList);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.mytextview, ListElementsArrayList);

//        ArrayAdapter<String> adapter_classifier = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_single_choice, ClassifierArrayList);

        ArrayAdapter<String> adapter_classifier = new ArrayAdapter<String>(getActivity(), R.layout.myradioview, ClassifierArrayList);

        listview.setAdapter(adapter);
        listview_Classifier.setAdapter(adapter_classifier);

        //selectes lda
        listview_Classifier.setItemChecked(0, true);

        //Kattia: Change 3 to 8 for experiments so that we don't need to select all gestures each time
        for (int i = 0; i < ListElements.length; i++) {
            listview.setItemChecked(i, true);
            selectedItems.add(i, adapter.getItem(i));
        }

        //set OnItemClickListener
        listview.setOnItemClickListener((parent, view, position, id) -> {

            // selected item
            String selectedItem = ((TextView) view).getText().toString();

            if (selectedItems.contains(selectedItem)) {
                selectedItems.remove(selectedItem); //remove deselected item from the list of selected items
            } else {
                selectedItems.add(selectedItem); //add selected item to the list of selected items
            }

            Copy_of_selectedItemsList = new ArrayList<String>(Arrays.asList(selectedItem));

        });


        //set OnItemClickListener
        listview_Classifier.setOnItemClickListener((parent, view, position, id) -> {

            classifier.setChoice(position);

            // selected item
            String Classifier_selectedItem = ((TextView) view).getText().toString();

            Toast.makeText(getActivity(), "selected: " + Classifier_selectedItem, Toast.LENGTH_SHORT).show();

        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selItems = "";
                while (selectedItems.size() > 0) {
                    for (int i = 0; i < selectedItems.size(); ++i) {
                        String item = selectedItems.get(i);

                        for (int x = 0; x <= item.length(); ++x) {
                            selectedItems.remove(item); //remove deselected item from the list of selected items
                            listview.setItemChecked(x, false);
                            adapter.remove(item);
                        }
                        selItems += "/" + item;
                    }

                }
                Toast.makeText(getActivity(), "Deleting: " + selItems, Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();

            }
        });

        addButton.setOnClickListener(v12 -> {

            try {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {

            }

            ListElementsArrayList.add(GetValue.getText().toString());
            GetValue.setText("");
            adapter.notifyDataSetChanged();
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Button cancel;
                Button sdCard;
                Button cloud;
                Button both;
                AlertDialog.Builder upload_pop = new AlertDialog.Builder(getActivity());

                View view = inflater.inflate(R.layout.upload_dialog, container, false);

                cancel = (Button) view.findViewById(R.id.bt_cancel);
                sdCard = (Button) view.findViewById(R.id.bt_sdcard);
                cloud = (Button) view.findViewById(R.id.bt_cloud);
                both = (Button) view.findViewById(R.id.bt_both);

                File file = saver.addData(fcalc.getFeatureData(), selectedItems);

                final AlertDialog dialog = upload_pop.create();

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        file.delete();
                        Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

                sdCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        saver.addData(fcalc.getSamplesClassifier(), selectedItems);
                        Toast.makeText(getActivity(), "Saving on SDCARD!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

                cloud.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cloudUpload.beginUpload(file);
                        cloudUpload.setDelete(true);
                        Toast.makeText(getActivity(), "Saving on Cloud!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

                both.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cloudUpload.setDelete(false);
                        cloudUpload.beginUpload(file);
                        Toast.makeText(getActivity(), "Saving on SDCARD and Cloud!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                dialog.setView(view);
                dialog.show();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gestureCounter = 0;
                liveView.setText("");
                trainButton.setVisibility(View.VISIBLE);
                fcalc.reset();
            }
        });

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickTrain(v);
            }
        });

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                openFolder();

                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = getContext().registerReceiver(null, ifilter);
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level / (float)scale;
                Log.d("Battery$$$ ", String.valueOf(batteryPct));
                Toast.makeText(getActivity(), "Battery Level "+String.valueOf(batteryPct), Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }

    public void onClickTrain(View v) {
        fcalc.sendClasses(selectedItems);

        final Runnable r1 = new Runnable() {
            @Override
            public void run() {
                if (selectedItems.size() > 1) {
                    trainButton.setVisibility(View.GONE);

                    if ((--count != -1) && (gestureCounter != selectedItems.size())) {
                        mHandler.postDelayed(this, 1000);
                        liveView.setText("Do " + selectedItems.get(gestureCounter) + " in " + String.valueOf(count));
//                        progressBar.setVisibility(View.VISIBLE);

                        if (count == 0) {
//                            progressBar.setVisibility(View.INVISIBLE);
                            liveView.setText("Hold " + selectedItems.get(gestureCounter));
                            //status.getText(featureCalculator.sampleClassifier);
                        }
                    } else if (gestureCounter != selectedItems.size()) {
                        count = 4;//3 seconds + 1
                        mHandler.post(this);
                        fcalc.setTrain(true);
                        while (fcalc.getTrain()) {//wait till trainig is done

                            /* For some reason we must print something here or else it gets stuck */
                            System.out.print("");
                        }
                        //bad ble never makes it here
                        gestureCounter++;
                    } else {
                        liveView.setText("");
                        fcalc.Train();
                        fcalc.setClassify(true);
                    }
                } else if (selectedItems.size() == 1) {
                    Toast.makeText(getActivity(), "at least 2 gestures must be selected!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getActivity(), "No gestures selected!", Toast.LENGTH_SHORT).show();

                }
            }
        };
        mHandler.post(r1);
    }

    public void openFolder(){

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        getActivity().startActivityForResult(intent, 2);

    }

    public void givePath(Uri data) {
        trainData = new ArrayList<DataVector>();
//        Log.d("Load Path: ", getPath(this.getContext(), data));
        try {
            BufferedReader reader = new BufferedReader(new FileReader(getPath(this.getContext(), data)));
            String text = null;
            String[] column;
            String[] emgData;
            double[] lineData = new double[48];

            int i = 0;
            while ((text = reader.readLine()) != null) {
                column = text.split("\t");
//                Classes.add(Integer.parseInt(column[0]));
                emgData = column[1].split(",");
                for(int j=0;j<emgData.length;j++){
                    lineData[j] = Double.parseDouble(emgData[j].replaceAll("[^\\d.]", ""));
//                       System.out.println(String.valueOf(lineData[3]));
                }
                Number[] feat_dataObj = ArrayUtils.toObject(lineData);
                ArrayList<Number> LineData = new ArrayList<Number>(Arrays.asList(feat_dataObj));
                DataVector dvec = new DataVector(Integer.parseInt(column[0]), lineData.length, LineData);
//                Log.d("Line: ", text);
                trainData.add(dvec);
//               System.out.print(String.valueOf(i) + " : ");
                dvec.printDataVector("Line: ");
                i++;
            }
//            fcalc.Train(trainData,);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}