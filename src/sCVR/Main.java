package sCVR;

import sCVR.preprocess.Preprossor;
import sCVR.model.sCVR;

import java.util.Scanner;

/**
 * Created by RexZhang on 4/28/17.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Preprossor preprossor = new Preprossor();
        try {
            preprossor.preprocess("Pleasant Hills", true);
            sCVR msCVR = new sCVR();
            msCVR.inference(20);
            System.out.println("Model trained.");
            while (true) {
                int uid = scanner.nextInt();
                int iid = scanner.nextInt();
                System.out.println("Query uid: " + uid + ", iid: " + iid + ", :");
                msCVR.predict(uid, iid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
