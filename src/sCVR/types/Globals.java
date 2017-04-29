package sCVR.types;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by RexZhang on 4/26/17.
 */
public class Globals {

    public static int U;  // number of users
    public static int I;  // number of items
    public static int K;  // number of topics
    public static int V;  // number of viewpoints
    public static int E;  // number of concepts
    public static int N;  // number of words in review corpus
    public static int R;  // number of different ratings [0, 5]
    public static int L;  // number of sentiments
    public static int X;  // number of different transition variables
    public static int RV; // number of reviews

    public static User users[];
    public static Item items[];
    public static Review reviews[];
    public static Viewpoint viewpoints[];
    public static Concept concepts[];
    public static Topic topics[];
    public static Word words[];
    public static HashMap<Integer, HashMap<Integer, Review>> reviewMap;

    public static HashSet<String> conjWord = new HashSet<String>(Arrays.asList("and", "both"));
    public static HashSet<String> advWord = new HashSet<String>(Arrays.asList("but", "whereas"));

}
