package sCVR.types;

import java.util.HashMap;

/**
 * Created by RexZhang on 4/10/17.
 */
public class Word {

    public int id;
    public String word;
    public Sentiment sentiment;
    public Topic topic;
    public int x;       // transition variable

    public Word() {
    }
}
