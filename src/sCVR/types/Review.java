package sCVR.types;

import java.util.HashMap;

/**
 * Created by RexZhang on 4/10/17.
 */
public class Review {
    // for a user review d

    public static HashMap<Review, Integer> reviewDic;

    public Viewpoint v;     // corresponding viewpoint
    public Word[] words;    // for words w in document d
    public User u;

}
