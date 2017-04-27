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

    /* -------------- Gibbs Sampling Variables Start ------------ */

    // n_{z, l, v}^{w, -d}
    // number of word w in certain <z, v, l>
    // nWordInTopicViewpointSentiment[z][v][l]
    public int[][][] nWordInTopicViewpointSentiment;    // done

    // n_{z, l, v}^{-d}
    // number of words have been assigned to this z, v, l
    // nWordInTopicViewpointSentimentSum[z][v][l]
    public static int[][][] nWordInTopicViewpointSentimentSum;  // done

    // n_{z, v}^{-j}
    // how many words are assigned to viewpoint v and topic z excluding w_j
    // nWordInTopicViewpoint[z][v]
    public static int[][] nWordInTopicViewpoint;    // done

    // n_{-j, x}^{w_j}
    // number of time word j has been assigned to x excluding current word
    // nWordJInX[x]
    public int[] nWordJInX; // done

    // n_{-j}^{w_j}
    // number of times word j has been assigned to all x excluding current word
    public int nWordJInXSum;    // done

    /* -------------- Gibbs Sampling Variables End------------ */

    public Word() {
        nWordInTopicViewpointSentiment = new int[Globals.K][Globals.V][Globals.L];
        nWordJInX = new int[Globals.X];
        nWordJInXSum = 0;
    }
}
