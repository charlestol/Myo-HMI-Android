package example.ASPIRE.MyoHMI_Android;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Charles on 7/12/17.
 */

public class SaveData {

    String FileName;
    Calendar c = Calendar.getInstance();

    public void addData(ArrayList<DataVector> trainData, ArrayList<String> selectedItems){
        String state;
        state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state)){
            File Root = Environment.getExternalStorageDirectory();
            File Dir = new File(Root.getAbsolutePath() + "/MyoAppFile");
            if(!Dir.exists()){
                Dir.mkdir();
            }

            FileName  = "File" + ".txt";

            File file = new File(Dir, FileName);

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                OutputStreamWriter osw = new OutputStreamWriter(fileOutputStream);

                for(int i=0;i<trainData.size();i++) {
                    DataVector data = trainData.get(i);
                    double trunc = i/100;
                    //            saver.addData(selectedItems.get((int)trunc), data.getVectorData().toString() + "\t" + String.valueOf(data.getTimestamp()));
                    osw.append(selectedItems.get((int)trunc) + "\t" + data.getVectorData().toString() + "\t" + String.valueOf(data.getTimestamp()));
                    osw.append("    ");
                    Log.d("To be saved: ", selectedItems.get((int)trunc) + data.getVectorData().toString() + "\t" + String.valueOf(data.getTimestamp()));
                }
                osw.flush();
                osw.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {
            Log.d("EXTERNAL STRG","No SD card found");

        }

    }
}