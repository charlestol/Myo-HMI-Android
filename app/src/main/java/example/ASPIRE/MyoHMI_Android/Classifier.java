package example.ASPIRE.MyoHMI_Android;
import org.apache.commons.lang3.ArrayUtils;
import java.util.ArrayList;
import smile.classification.LDA;
import smile.classification.SVM;

/**
 * Created by Alex on 7/3/2017.
 */

public class Classifier {
    int numFeatures = 5;
    double[][] trainVectorP;
    LDA lda;
    SVM svm;
    double[] features;
    int[] classes;
    int[] testclasses = new int[3];

    public Classifier(){

    }

    public void Train(ArrayList<DataVector> trainVector, ArrayList<Integer> Classes){
        classes = new int[Classes.size()];
        trainVectorP = new double[trainVector.size()][numFeatures*8];
        for(int i=0;i<trainVector.size();i++){
            for(int j=0;j<numFeatures*8;j++){
                trainVectorP[i][j] = trainVector.get(i).getValue(j).doubleValue();
            }
        }

        for(int j=0;j<Classes.size();j++){
            classes[j] = Classes.get(j);
        }
//        classes = ArrayUtils.toPrimitive((Integer[])Classes.toArray());
        lda = new LDA(trainVectorP, classes, 0);
    }

    public int predict(DataVector Features){
//        features = ArrayUtils.toPrimitive((Double[])Features.getVectorData().toArray());
        features = new double[Features.getLength()];
        for(int i=0;i<Features.getLength();i++){
            features[i]=Features.getValue(i).doubleValue();
        }
        return lda.predict(features);
    }

}