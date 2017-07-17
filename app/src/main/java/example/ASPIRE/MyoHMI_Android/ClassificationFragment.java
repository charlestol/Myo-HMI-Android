//package example.ASPIRE.MyoHMI_Android;
//
//import android.app.Activity;
//
//
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.CountDownTimer;
//import android.os.Handler;
//import android.os.SystemClock;
//import android.service.notification.Condition;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.os.CountDownTimer;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.ListView;
//import android.widget.TextSwitcher;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.widget.ViewSwitcher;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.locks.ReentrantLock;
//
///**
// * Created by User on 2/28/2017.
// */
//
//public class ClassificationFragment extends Fragment {
//
//    private MyoGattCallback mMyoCallback;
//
//    private static final String TAG = "Tab2Fragment";
//
//    private Button btnTEST;
//
//    private FeatureCalculator fcalc;
//
//    private TextView countView;
//
//    private int numSamples = 100;
//
//    private int numGestures = 3;
//
//    private int gestureCounter = 0;
//
//    private int count = 4;
//
//    private Handler mHandler = new Handler();
//
//    public Classifier classifier = new Classifier();
//
//    private Map<String, Integer> gestureNames = new HashMap<String, Integer>();
//
//    private TextView liveView;
//
//    private List<String> ListElementsArrayList;
//
//    ListView listview;
//    Button Addbutton;
//    EditText GetValue;
//    Button showButton;
//
//    //create an ArrayList object to store selected items
//    ArrayList<String> selectedItems = new ArrayList<String>();
//
//    String[] ListElements = new String[] {
//            "Fist",
//            "Point",
//            "Open Hand"
//    };
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
//        final View v = inflater.inflate(R.layout.fragment_classification, container, false);
//        assert v != null;
//
//        listview = (ListView)v.findViewById(R.id.listView);
//        Addbutton = (Button)v.findViewById(R.id.button);
//        GetValue = (EditText)v.findViewById(R.id.add_gesture);
//        showButton = (Button)v.findViewById(R.id.btShow);
//
//        liveView = (TextView)v.findViewById(R.id.textView3);
//
//        fcalc = new FeatureCalculator(v, getActivity());
//
////        liveView = (TextSwitcher) v.findViewById(R.id.textSwitcher);
////        liveView.setFactory(new ViewSwitcher.ViewFactory() {
////            public View makeView() {
////                TextView t = new TextView(getActivity());
////                t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
////                t.setTextSize(36);
////                t.setText("Gesture");
////                return t;
////            }
////        });
//
//        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//        ListElementsArrayList = new ArrayList<String>(Arrays.asList(ListElements));
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>
//                (getActivity(), android.R.layout.simple_list_item_multiple_choice, ListElementsArrayList);
//
//        listview.setAdapter(adapter);
//
//        //set OnItemClickListener
//        listview.setOnItemClickListener((parent, view, position, id) -> {
//
//            // selected item
//            String selectedItem = ((TextView) view).getText().toString();
//            if(selectedItems.contains(selectedItem))
//
//                selectedItems.remove(selectedItem); //remove deselected item from the list of selected items
//            else
//                selectedItems.add(selectedItem); //add selected item to the list of selected items
//
//        });
//
//        showButton.setOnClickListener(v1 -> {
//
//            String selItems="";
//            for(String item:selectedItems){
//                if(selItems=="")
//                    selItems=item;
//                else
//                    selItems+="/"+item;
//            }
//            Toast.makeText(getActivity(), selItems, Toast.LENGTH_LONG).show();
//
//        });
//
//        Addbutton.setOnClickListener(v12 -> {
//
//            ListElementsArrayList.add(GetValue.getText().toString());
//
//            adapter.notifyDataSetChanged();
//        });
//
//        View trainButton = v.findViewById(R.id.train);
//        trainButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v){
//                onClickTrain(v);
//            }
//        });
//
//        return v;
//    }
//
//    public void onClickTrain(View v) {
//
//        fcalc.sendClasses(ListElementsArrayList);
//
//        final Runnable r1 = new Runnable() {
////        final Runnable r1 = getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if ((count != 0) && (gestureCounter != ListElementsArrayList.size())) {
//                    mHandler.postDelayed(this, 1000);
//                    count--;
//                    liveView.setText("Do " + ListElementsArrayList.get(gestureCounter) + " in " + String.valueOf(count));
//                    if(count==0){liveView.setText("Hold " + ListElementsArrayList.get(gestureCounter));}
//                }
//                else if(gestureCounter != ListElementsArrayList.size()){
//                    count=4;
//                    mHandler.postDelayed(this, 1000);
//                    fcalc.setTrain(true);
//                    while(fcalc.getTrain()){
//                        //wait till trainig is done
//                    }
//                    gestureCounter++;
//                }
//                else{
//                    liveView.setText("");
//                    fcalc.Train();
//                    fcalc.setClassify(true);
//                }
//            }
//        };
//        mHandler.postDelayed(r1, 1000);
//
//    }
//}

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


    private int count = 4;

    private Handler mHandler = new Handler();

    private int gestureCounter = 0;

    private TextView liveView;

    private TextView or_text;

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

        fcalc = new FeatureCalculator(v, getActivity());

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

        fcalc.sendClasses(ListElementsArrayList);

        final Runnable r1 = new Runnable() {
            //        final Runnable r1 = getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((count != 0) && (gestureCounter != ListElementsArrayList.size())) {
                    mHandler.postDelayed(this, 1000);
                    count--;
                    liveView.setText("Do " + ListElementsArrayList.get(gestureCounter) + " in " + String.valueOf(count));
                    if(count==0){liveView.setText("Hold " + ListElementsArrayList.get(gestureCounter));}
                }
                else if(gestureCounter != ListElementsArrayList.size()){
                    count=4;
                    mHandler.postDelayed(this, 1000);
                    fcalc.setTrain(true);
                    while(fcalc.getTrain()){
                        //wait till trainig is done
                    }
                    gestureCounter++;
                }
                else{
                    liveView.setText("");
                    fcalc.Train();
                    fcalc.setClassify(true);
                }
            }
        };
        mHandler.postDelayed(r1, 1000);

//        trainButton.setVisibility(View.VISIBLE);
    }
}
