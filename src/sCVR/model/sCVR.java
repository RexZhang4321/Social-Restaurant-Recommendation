package sCVR.model;

import java.util.HashMap;

/**
 * Created by RexZhang on 4/10/17.
 */
public class sCVR {

    int U;  // number of users
    int I;  // number of items
    int K;  // number of topics
    int V;  // number of viewpoints
    int E;  // number of concepts
    int N;  // number of words in review corpus
    HashMap<Integer, HashMap<Integer, Integer>> Nd;   // Nd[u][i], number of words in review[u][i]

    // for Eq.3
    // R_{u}
    private int[] nUserRateItems;   // nUserRateItem[u], number of times user u rates all items
    // n_{f, -(u, i)}^{i ,y}    (??? dimension problem)
    private int[][] nVarfAssign2YInItem; // nVarfAssignYInItem[i][f], in item i, number of times variable f has been assigned to y
    // n_{f, -(u, i)}^{i}
    private int[] nVarfAssignInItemSum;  // nVarfAssignInItemSum[i], in item i, number of viewpoints v has been assigned
    // n_{u, -i}^{r_{u,i}, y}
    private int[][][] nUserItemViewpoints;  // nUserItemViewpoints[u][i][v], number of times user u rates item i with viewpoint v
    // n_{u}^{r_{u,i}, y}
    private int[][] nUserItemViewpointsSum; // nUserItemViewpointsSum[u][i], number of viewpoints for user u in item i
    // n_{v}^{i, y}
    private int[][] nViewpointInItem;   // nViewpointInItem[i][v], in item i, number of times viewpoint v has been assigned (to y)
    // n_{v}^{i}
    private int[] nViewpointInItemSum;  // nViewpointInItemSum[i], number of viewpoints v has been assigned to i (?)

    // for Eq.4
    private double[][][] thetaUserViewpointRating;  // thetaUserRatingViewpoint[u][v][r]
    private int[][] trustValueU0U1; // trust value between user u and u', 1 for having a relation, otherwise 0

    // for Eq.5
    // n_{-d}^{i, v}
    private int[] nViewpoint2Review;  // nViewpoint2Review[v], number of times viewpoint v has been assigned to user reviews, excluding d
    // n_{-d}^{i}
    private int nViewpoint2ReviewSum; // nViewpoint2ReviewSum[d], number of viewpoints(that have been assigned to user reviews) excluding d
    // n_{f}^{i, y}, declared above
    // n_{v, e}^{-d}
    private int[][] nConceptInViewpoint;    // nConceptInViewpoint[v][e], number of times concept e has been assigned to viewpoint v excluding d
    // n_{v}^{-d}
    private int[] nConceptInViewpointSum;   // nConceptInViewpointSum[v], number of concepts that have been assigned to viewpoint v excluding d
    // n_{v, z}^{-d}
    private int[][] nTopicInViewpoint;  // nTopicInViewpoint[v][z], number of times topic z has been assigned to viewpoint v excluding d
    // n_{v}^{-d}
    private int[] nTopicInViewpointSum; // nTopicInViewpointSum[v], number of topics that have been assigned to viewpoint v excluding d
    // n_{z, l, v}^{w, -d}  ??? paper may be wrong in this value
    private int[][][][] nWordInTopicViewpointSentiment;  // nWordInTopicViewpointSentiment[z][v][l][w], number of word w in certain <z, v, l>
    // n_{z, l, v}^{-d}
    private int[][][] nWordInTopicViewpointSentimentSum; // nWordInTopicViewpointSentimentSum[z][v][l], number of words have been assigned to this z, v, l

    // for Eq.6 (excluding current word -> sampling word)
    // n_{v, k}^{-j}
    private int[][] nTopicsInViewpoint;   // nTopicsInViewpoint[v][z], number of time a topic z that has been assigned to viewpoint v excluding w_j
    // n_{v}^{-j}
    private int[] nTopicsInViewpointSum;  // nTopicsInViewpointSum[v], number of topics that have been assigned to viewpoint v excluding w_j
    // n_{k, l, v}^{w_j, -j} ?? the same with **nWordInTopicViewpointSentiment** before ??
    private int[][][][] nWordJ2ZLV; // nWordJ2ZLV[z][l][v][wj], number of time that word w_j has been assigned to topic z, sentiment l
    // n_{k, l, v}^{-j}
    private int[][][] nWordJ2ZLVSum;    // nWordJ2ZLVSum[z][l][v], number of words that have been assigned to topic z and sentiment l
    // n_{-j, x}^{w_j}
    private int[][] nWordJ2X;   // nWordJ2X[wj][x], number of time word j has been assigned to x excluding current word
    // n_{-j}^{w_j}
    private int[] nWordJ2XSum;  // nWordJ2XSum[wj], number of times word j has been assigned to all x excluding current word

    // for Eq.7 (a derivation from Eq.6)
    // n_{z, l, v}^{-j} ?? the same with **nWordInTopicViewpointSentiment** before ??
    private int[][][] nWord2ZVLInWords;    // nWord2ZLVInWords[z][v][l], how many words that have been assigned to topic z, sentiment l and viewpoint v
    // n_{z, v}^{-j}
    private int[][] nWord2ZVLInWordsSum;   // nWord2ZVLInWordsSum[z][v]. how many words are assigned to viewpoint v and topic z

    public sCVR() {

    }

    public void init() {

    }

    public void inference(int nIter) {
        for (int i = 0; i < nIter; i++) {
            doEStep();
            doMStep();
        }

    }

    private void doEStep() {
        for (int u = 0; u < U; u++) {
            for (int i = 0; i < I; i++) {
                // draw f_{u, i} = y from Eq.3
                // update
                // draw v_{d} = v from Eq.5
                // update
                for (int j = 0; j < Nd.get(u).get(i); j++) {
                    // draw <z_j, l_j, x_j> from Eq.6
                    // if x_j then ... else ...
                }
            }
        }


    }

    private void doMStep() {
        // re-estimate theta, pi, fai, mew, lambda from Eq. 8
        // maximize baseTheta from Eq.9
    }

}
