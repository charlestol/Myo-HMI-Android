package example.ASPIRE.MyoHMI_Android;

/**
 * Created by admin on 7/18/2017.
 */


public class Credentials {

    /*
     *  Replace with the Identity pool found on Amazon Cognito. Under "Manage Federated Identities -- PoolName -- Sample Code"
     */
    public static final String COGNITO_POOL_ID = "us-west-2:a8a80cf1-37bb-4fa5-83e3-81575bd77c02";

    /*
     * Region of your Cognito identity pool ID.
     */
    public static final String COGNITO_POOL_REGION = "us-west-2";

    /*
     * Note, you must first create a bucket using the S3 console before running
     * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
     * put it's name in the field below.
     */
    public static final String BUCKET_NAME = "icelab";

    /*
     * Region of your bucket.
     */
    public static final String BUCKET_REGION = "us-west-2";
}
