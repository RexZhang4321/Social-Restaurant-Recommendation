package sCVR;

import sCVR.preprocess.Preprossor;
import sCVR.model.sCVR;
import sCVR.types.Concept;
import sCVR.types.Topic;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by RexZhang on 4/28/17.
 */
public class Main {
    public static void main(String[] args) {
        int niters = 20;
        try {
            Properties properties = new Properties();
            FileInputStream in = new FileInputStream("config.txt");
            properties.load(in);
            in.close();
            niters = Integer.parseInt(properties.getProperty("TRAIN_ITERS"));
            System.out.println("Train for " + niters + " iterations.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);
        Preprossor preprossor = new Preprossor();
        try {
            preprossor.preprocess("Pleasant Hills", true);
            sCVR msCVR = new sCVR();
            HashMap<Concept, Topic> conceptTopicHashMap = new HashMap<>();
            HashMap<Topic, String[]> topicWordsHashMap = new HashMap<>();
            if (args[0].equals("read")) {
                msCVR.readModel();
                msCVR.collectStats(conceptTopicHashMap, topicWordsHashMap);
            } else {
                msCVR.inference(niters);
                System.out.println("Model trained.");
                System.out.println("Saving model...");
                msCVR.saveModel();
                System.out.println("Model saved.");
            }
            msCVR.evaluate();
            while (true) {
                int uid = scanner.nextInt();
                int iid = scanner.nextInt();
                System.out.println("Query uid: " + uid + ", iid: " + iid + ":");
                msCVR.predict(uid, iid, true);
                if (args[0].equals("read")) {
                    msCVR.recommendForUser(uid, conceptTopicHashMap, topicWordsHashMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
