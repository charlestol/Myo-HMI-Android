package example;

import com.amazonaws.services.lambda.runtime.Context;
import org.apache.commons.lang3.ArrayUtils;
import java.util.Properties;
import java.util.Arrays;
import java.util.ArrayList;

public class Pojo {

    public static FeatureCalculator fcalc = new FeatureCalculator();
    public static Classifier classifier = new Classifier();

    static int samples = 100;

    public static Integer giveIncrement(byte[] emg_data){ //this is my lambda handler!!!

        // byte[] time = Arrays.copyOfRange(emg_data, 65, 73);
        // long clienttime = bytesToLong(time);
                            
        switch(emg_data[0]){
            case 0:
                //route data nowhere
                break;
            case 1:
                //classify
                if((fcalc.getSize()%samples)==0){ //Baaaaaad solution just for testing, need to remove dependency on local operation
                    fcalc.setTrain(false);
                    fcalc.setClassify(true);
                    //trained = true;
                }
                break;
            case 2:
                //train
                fcalc.setClassify(false);
                fcalc.setTrain(true);
                break;
            case 3:
                //System.out.println(buffer);
            default:
                //broken packet
                break;
        }

        for(int i = 0; i < 64/8; i++){

            byte[] emg_data1 = Arrays.copyOfRange(emg_data,i*8+1,i*8+1+8);
            Number[] emg_dataObj = ArrayUtils.toObject(emg_data1);
            ArrayList<Number> emg_data_list = new ArrayList<>(Arrays.asList(emg_dataObj));
            DataVector dvec = new DataVector(true, 1, 8, emg_data_list, System.currentTimeMillis());

            fcalc.pushFeatureBuffer(dvec);
        }

        Integer prediction = -1; //-1 for no prediction

        if(fcalc.getClassify())
            prediction = new Integer(fcalc.getPrediction());
        	System.out.println("!!! Prediction: " + String.valueOf(prediction));

        return prediction;
    }
}