package example.ASPIRE.MyoHMI_Android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.RadarChart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by User on 2/28/2017.
 */

public class FeatureFragment extends Fragment {
    private static final String TAG = "Tab2Fragment";
    private RadarChart mChart;
    private Plotter plotter;
    ListView listview_Classifier;
    ListView listView_Features;

    //create an ArrayList object to store selected items
    ArrayList<String> selectedItems = new ArrayList<String>();


    String[] classifier = new String[]{
            "LDA",
            "QDA",
            "SVM",
            "Logistic Regression",
            "Decision"
    };

    String[] features = new String[]{
            "MAV",
            "WAV",
            "TURNS",
            "Zeros",
            "SMAV"
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_feature, container, false);
        assert v != null;

        listView_Features = (ListView) v.findViewById(R.id.listView);
        listview_Classifier = (ListView) v.findViewById(R.id.listView1);


        listView_Features.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listview_Classifier.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        final List<String> FeaturesArrayList = new ArrayList<String>(Arrays.asList(features));
        final List<String> ClassifierArrayList = new ArrayList<String>(Arrays.asList(classifier));


        ArrayAdapter<String> adapter_features = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_list_item_multiple_choice, FeaturesArrayList);

        ArrayAdapter<String> adapter_classifier = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_list_item_single_choice, ClassifierArrayList);


        listView_Features.setAdapter(adapter_features);
        listview_Classifier.setAdapter(adapter_classifier);

        //set OnItemClickListener
        listView_Features.setOnItemClickListener((parent, view, position, id) -> {

            // selected item
            String Features_selectedItem = ((TextView) view).getText().toString();

            if (selectedItems.contains(Features_selectedItem)) {
                selectedItems.remove(Features_selectedItem); //remove deselected item from the list of selected items
            } else {
                selectedItems.add(Features_selectedItem); //add selected item to the list of selected items
            }

            Toast.makeText(getActivity(), "selected: " + selectedItems, Toast.LENGTH_SHORT).show();
        });

        //set OnItemClickListener
        listview_Classifier.setOnItemClickListener((parent, view, position, id) -> {

            // selected item
            String Classifier_selectedItem = ((TextView) view).getText().toString();


            Toast.makeText(getActivity(), "selected: " + Classifier_selectedItem, Toast.LENGTH_SHORT).show();
        });






        mChart = (RadarChart) v.findViewById(R.id.chart);
        mChart.setNoDataText("This is the chart with no data!");
        plotter = new Plotter(mChart);//must pass chart from this fragment


        return v;
    }


}
