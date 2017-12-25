package example.ASPIRE.MyoHMI_Android;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.mobileconnectors.lambdainvoker.*;

import java.util.Arrays;

/**
 * Created by Alex on 12/24/2017.
 */

public class Lambda {

    static CognitoCachingCredentialsProvider cognitoProvider;
    static LambdaInvokerFactory factory;

    final static LambdaInterface myInterface = factory.build(LambdaInterface.class);

    public Lambda(Context context){

        // Create an instance of CognitoCachingCredentialsProvider
        cognitoProvider = new CognitoCachingCredentialsProvider(context.getApplicationContext(), "us-west-2:b547c9df-87e3-4a7a-b418-c5649f10c17b", Regions.US_WEST_2);

        // Create LambdaInvokerFactory, to be used to instantiate the Lambda proxy.
        factory = new LambdaInvokerFactory(context.getApplicationContext(),Regions.US_WEST_2, cognitoProvider);

    }

    public Lambda(){}

    public static class LTask extends AsyncTask<byte[], Void, Integer>{
        @Override
        protected Integer doInBackground(byte[]... params) {
            // invoke "echo" method. In case it fails, it will throw a
            // LambdaFunctionException.
            try {
                return myInterface.giveBytes(params[0]);
            } catch (Exception lfe) {
                Log.e("Tag", "Failed to invoke echo", lfe);
                //System.out.println(lfe);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            int prediction = result.intValue();
            System.out.println("prediction");
        }
    }

}
