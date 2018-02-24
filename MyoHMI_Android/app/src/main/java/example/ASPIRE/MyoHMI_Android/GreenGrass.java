package example.ASPIRE.MyoHMI_Android;
//
//import java.security.KeyStore;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.PrivateKey;
//import java.security.SecureRandom;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateException;
//import java.security.cert.CertificateFactory;
//import com.amazonaws.auth.CognitoCachingCredentialsProvider;
//import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
//import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
//import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
//import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
//import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
//import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
//import com.amazonaws.regions.Region;
//import com.amazonaws.regions.Regions;
//import com.amazonaws.services.iot.AWSIotClient;
//import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
//import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
//import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;
//
///**
// * Created by Alex on 12/27/2017.
// */
//
//public class GreenGrass {
//    String clientEndpoint = "<prefix>.iot.<region>.amazonaws.com";       // replace <prefix> and <region> with your own
//    String clientId = "<unique client id>";                              // replace with your own client ID. Use unique client IDs for concurrent connections.
//    String certificateFile = "<certificate file>";                       // X.509 based certificate file
//    String privateKeyFile = "<private key file>";                        // PKCS#1 or PKCS#8 PEM encoded private key file
//
//    // SampleUtil.java and its dependency PrivateKeyReader.java can be copied from the sample source code.
//    // Alternatively, you could load key store directly from a file - see the example included in this README.
//    KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile);
//    AWSIotMqttClient client = new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);
//
//    // optional parameters can be set before connect()
//    client.connect();
//}
