package example.ASPIRE.MyoHMI_Android;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Charles on 7/12/17.
 */

public class SaveData {

    String GestureFileName;

    public void addData(String gesture, String val){
        String state;
        state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state)){
            File Root = Environment.getExternalStorageDirectory();
            File Dir = new File(Root.getAbsolutePath() + "/MyoAppFile");
            if(!Dir.exists()){
                Dir.mkdir();
            }
            GestureFileName = gesture + ".txt";
            File file = new File(Dir, GestureFileName);

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file, true);
                OutputStreamWriter osw = new OutputStreamWriter(fileOutputStream);

                osw.append(val);
                osw.append("    ");

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
