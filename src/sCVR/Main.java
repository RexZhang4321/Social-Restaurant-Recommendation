package sCVR;

import sCVR.preprocess.Preprossor;
import sCVR.model.sCVR;

/**
 * Created by RexZhang on 4/28/17.
 */
public class Main {
    public static void main(String[] args) {
        Preprossor preprossor = new Preprossor();
        sCVR msCVR = new sCVR();
        try {
            preprossor.preprocess("");
            msCVR.inference(10);
            msCVR.predict(0, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
