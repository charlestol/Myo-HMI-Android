package example.ASPIRE.MyoHMI_Android;

import android.annotation.SuppressLint;
        import android.app.Activity;


        import android.app.AlertDialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.os.CountDownTimer;
        import android.os.Handler;
        import android.os.SystemClock;
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
        import android.widget.TextView;
        import android.widget.Toast;

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

    private List<String> Copy_of_selectedItemsList;

    private SaveData saver;

    private ArrayList<DataVector> trainData;

    private int count = 4;

    private Handler mHandler = new Handler();

    private int gestureCounter = 0;

    private TextView liveView;

    private TextView or_text;

    private Classifier classifier;//for making toast on this activity

    EditText GetValue;
    ImageButton addButton;
    ImageButton deleteButton;
    ImageButton clearButton;
    //Button showButton;
    Button trainButton;
    Button loadButton;

    ListView listview;

    //create an ArrayList object to store selected items
    ArrayList<String> selectedItems = new ArrayList<String>();

    String[] ListElements = new String[]{
            "Fist",
            "Point",
            "Open Hand"
    };

    String[] copy_of_selectedItem = new String[]{

    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_classification, container, false);

        assert v != null;

        final Runnable r1, r2;

        fcalc = new FeatureCalculator(v, getActivity());
        classifier = new Classifier(getActivity());
        saver  = new SaveData(this.getContext());

        or_text = (TextView) v.findViewById(R.id.or_text);
        liveView = (TextView)v.findViewById(R.id.gesture_detected);
        GetValue = (EditText) v.findViewById(R.id.add_gesture_text);
        trainButton = (Button) v.findViewById(R.id.bt_train);
        loadButton = (Button) v.findViewById(R.id.bt_load);
       // showButton = (Button) v.findViewById(R.id.bt_show);
        addButton = (ImageButton) v.findViewById(R.id.im_add);
        deleteButton = (ImageButton) v.findViewById(R.id.im_delete);
        clearButton = (ImageButton) v.findViewById(R.id.im_clear);
        listview = (ListView) v.findViewById(R.id.listView);

        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        ListElementsArrayList = new ArrayList<String>(Arrays.asList(ListElements));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_list_item_multiple_choice, ListElementsArrayList);


        listview.setAdapter(adapter);

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

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("Clear All?");
                builder.setMessage("Do you want to clear all gestures? ");

                builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int sizeOfArray = ListElementsArrayList.size();

                        for (int x = 0; x < sizeOfArray; ++x) {
                            listview.setItemChecked(x, false);
                        }

                        selectedItems.clear();
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                    }
                });
                builder.show();

            }
        });


        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selItems = "";
                while(selectedItems.size() > 0){
                    for (int i = 0; i < selectedItems.size(); ++i) {
                        String item = selectedItems.get(i);

                        for (int x = 0; x <= item.length(); ++x) {
                            selectedItems.remove(item); //remove deselected item from the list of selected items
                            listview.setItemChecked(x, false);
                            adapter.remove(item);
                        }
                        selItems += "/" + item ;
                    }

                }
                Toast.makeText(getActivity(), "Deleting: " + selItems, Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();

            }
        });

        addButton.setOnClickListener(v12 -> {

            try  {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {

            }

            ListElementsArrayList.add(GetValue.getText().toString());
            GetValue.setText("");
            adapter.notifyDataSetChanged();
        });

        /*showButton.setOnClickListener(v1 -> {

            String selItems = "";
            for (String item : selectedItems) {
                if (selItems == "")
                    selItems = item;

                else
                    selItems += "/" + item;

            }

            Toast.makeText(getActivity(), selItems, Toast.LENGTH_SHORT).show();

        });*/

        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                onClickTrain(v);
            }
        });

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Toast.makeText(getActivity(), "DOES NOTHING YET :(", Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }

    public void onClickTrain(View v) {

        or_text.setVisibility(View.GONE);
        trainButton.setVisibility(View.GONE);
        loadButton.setVisibility(View.GONE);
        fcalc.sendClasses(selectedItems);

        final Runnable r1 = new Runnable() {
            @Override
            public void run() {
                if ((--count != -1) && (gestureCounter != selectedItems.size())) {
                    mHandler.postDelayed(this, 1000);
                    liveView.setText("Do " + selectedItems.get(gestureCounter) + " in " + String.valueOf(count));
                    if(count==0){liveView.setText("Hold " + selectedItems.get(gestureCounter));}
                }
                else if(gestureCounter != selectedItems.size()){
                    count=4;//3 seconds + 1
                    mHandler.post(this);
                    fcalc.setTrain(true);
                    while(fcalc.getTrain()){
                        //wait till trainig is done
                    }
                    gestureCounter++;
                }
                else{
                    liveView.setText("");
                    fcalc.Train();

                    saver.addData(fcalc.getSamplesClassifier(), selectedItems);

                    fcalc.setClassify(true);

                }
            }
        };
        mHandler.post(r1);
    }
}

//    public void onClickTrain(View v) {
//        fcalc.sendClasses(selectedItems);
//
//        final Runnable r1 = new Runnable() {
//            @Override
//            public void run() {
//                if (selectedItems.size() > 0) {
//                    or_text.setVisibility(View.GONE);
//                    trainButton.setVisibility(View.GONE);
//                    loadButton.setVisibility(View.GONE);
//
//                    if ((--count != -1) && (gestureCounter != selectedItems.size())) {
//                        saver.addData(fcalc.getSamplesClassifier(), selectedItems);
//                    }
//                }
//                else
//                {
//                    Toast.makeText(getActivity(), "No gestures selected!", Toast.LENGTH_SHORT).show();
//
//                }
//
//            }
//        };
//        mHandler.post(r1);
//    }