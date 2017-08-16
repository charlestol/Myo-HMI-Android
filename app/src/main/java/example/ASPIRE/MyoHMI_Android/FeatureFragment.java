package example.ASPIRE.MyoHMI_Android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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

    Classifier classifier = new Classifier();

    //create an ArrayList object to store selected items
    ArrayList<String> selectedItems = new ArrayList<String>();

    String[] featureNames = new String[]{
            "MAV",
            "WAV",
            "Turns",
            "Zeros",
            "SMAV",
            "AdjUnique"
    };

    /**
     * Charles 7/18
     **/
    private static boolean[] featSelected = new boolean[]{true, true, true, true, true, true};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_feature, container, false);
        assert v != null;

        listView_Features = (ListView) v.findViewById(R.id.listView);
        //listview_Classifier = (ListView) v.findViewById(R.id.listView1);


        listView_Features.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        //listview_Classifier.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        final List<String> FeaturesArrayList = new ArrayList<String>(Arrays.asList(featureNames));
        //final List<String> ClassifierArrayList = new ArrayList<String>(Arrays.asList(classifierNames));


        ArrayAdapter<String> adapter_features = new ArrayAdapter<String>(getActivity(), R.layout.mytextview, FeaturesArrayList);

        /*ArrayAdapter<String> adapter_classifier = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_list_item_single_choice, ClassifierArrayList);

*/
        listView_Features.setAdapter(adapter_features);
        for (int i = 0; i < 6; i++) {
            listView_Features.setItemChecked(i, true);
            selectedItems.add(i, adapter_features.getItem(i));
        }

        //listview_Classifier.setAdapter(adapter_classifier);
        // listview_Classifier.setItemChecked(0,true);
        //set OnItemClickListener
        listView_Features.setOnItemClickListener((parent, view, position, id) -> {

            // selected item
            String Features_selectedItem = ((TextView) view).getText().toString();

            if (selectedItems.contains(Features_selectedItem)) {
                featureManager(Features_selectedItem, false);
                selectedItems.remove(Features_selectedItem); //remove deselected item from the list of selected items
                classifier.numFeatures--;
                Log.d("NUM FEAT: ", "" + classifier.numFeatures);
            } else {
                featureManager(Features_selectedItem, true);
                selectedItems.add(Features_selectedItem); //add selected item to the list of selected items
                classifier.numFeatures++;
                Log.d("NUM FEAT: ", "" + classifier.numFeatures);
            }

            plotter.setFeatures(featSelected);

//            Toast.makeText(getActivity(), "selected: " + selectedItems, Toast.LENGTH_SHORT).show();
        });


        mChart = (RadarChart) v.findViewById(R.id.chart);
        plotter = new Plotter(mChart);//must pass chart from this fragment


        return v;
    }

    /**
     * Charles 7/18
     **/
    private void featureManager(String inFeature, boolean selected) {
        int index = 0;
        for (int i = 0; i < 6; i++) {
            if (inFeature == featureNames[i]) {
                index = i;
            }
        }

        featSelected[index] = selected;
    }

    public String[] getFeatureNames() {
        return featureNames;
    }

    public static boolean[] getFeatSelected() {
        return featSelected;
    }
}
