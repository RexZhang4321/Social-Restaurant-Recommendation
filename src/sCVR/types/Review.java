package sCVR.types;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RexZhang on 4/10/17.
 */
public class Review {
    // for a user review d

    public int id;
    public String hashId;
    public User user;
    public Item item;
    public int rating;
    public Viewpoint ratingViewpoint;
    public Viewpoint reviewViewpoint;
    public Concept concept;
    public int topicCnt[];
    public int sentiment;
    public ArrayList<Word> words;    // for words w in document d

    public Review() {
        topicCnt = new int[Globals.K];
        words = new ArrayList<Word>();
    }

}
