package sCVR.types;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RexZhang on 4/10/17.
 */
public class Review {
    // for a user review d

    public static HashMap<Integer, HashMap<Integer, Review>> reviewDocs;

    public Viewpoint v;     // corresponding viewpoint
    public Word[] words;    // for words w in document d
    public User u;
    public Item item;
    public int id;
    public int rating;
    public Rating ratingObj;
    public Concept concept;

    public Review() {
    }

}
