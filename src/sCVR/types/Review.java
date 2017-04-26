package sCVR.types;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RexZhang on 4/10/17.
 */
public class Review {
    // for a user review d

    public static HashMap<Integer, HashMap<Integer, Review>> reviewDocs;

    public int id;
    public User user;
    public Item item;
    public Rating rating;
    public Viewpoint ratingViewpoint;
    public Viewpoint reviewViewpoint;
    public Concept concept;
    public ArrayList<Word> words;    // for words w in document d

    public Review() {
    }

}
