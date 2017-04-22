package sCVR.model;

import sCVR.types.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by RexZhang on 4/10/17.
 */
public class sCVR {

    private Random rnd;
    private final static long seed = 0;

    int U;  // number of users
    int I;  // number of items
    int K;  // number of topics
    int V;  // number of viewpoints
    int E;  // number of concepts
    int N;  // number of words in review corpus
    int R;  // number of different ratings [0, 5]
    int L;  // number of sentiments
    int X;  // number of different transition variables
    int RV; // number of reviews

    public double alpha = 0.01;
    public double sigma = 0.01;
    public double chi = 0.01;
    public double beta = 0.01;
    public double tau[] = {0.3, 0.4, 0.3};
    public double eta[] = {0.3, 0.4, 0.3};

    public User users[];
    public Item items[];
    public Viewpoint viewpoints[];
    public Concept concepts[];
    public Topic topics[];
    public Word words[];
    public Sentiment sentiments[];
    public Rating ratings[];

    public double pi[][];

    // for Eq.3
    // R_{u}
    private int[] nUserRateItems;   // nUserRateItem[u], number of times user u rates all items
    // n_{f, -(u, i)}^{i ,y}
    private int[][] nRatingViewpointInItem; // nVarfAssignYInItem[i][f], in item i, number of times variable f has been assigned to y
    // n_{f, -(u, i)}^{i}
    private int[] nRatingViewpointInItemSum;  // nVarfAssignInItemSum[i], in item i, number of viewpoints v has been assigned
    // n_{u, -i}^{r_{u,i}, y}
    // private int[][][] nUserItemViewpoints;  // nUserItemViewpoints[u][i][v], number of times user u rates item i with viewpoint v
    private int[][][] nUserViewpointsRating;    // nUserViewpointsRating[u][v][r], number of times user u gives rating r for certain viewpoint v, excluding item i
    // n_{u}^{r_{u,i}, y}
    //private int[][] nUserItemViewpointsSum; // nUserItemViewpointsSum[u][i], number of viewpoints for user u in item i
    private int[][] nUserViewpointsRatingSum;   // nUserViewpointsRatingSum[u][v], number of times user u gives rating using viewpoint v
    // n_{v}^{i, y}
    private int[][] nViewpointInItem;   // nViewpointInItem[i][v], in item i, number of times viewpoint v has been assigned (to y)
    // n_{v}^{i}
    private int[] nViewpointInItemSum;  // nViewpointInItemSum[i], number of viewpoints v has been assigned to i (?)

    // for Eq.4
    private double[][][] thetaUserRatingViewpoint;  // thetaUserRatingViewpoint[u][r][v]
    private double[][][] thetaBaseUserRatingViewpoint;  // thetaBaseUserRatingViewpoint[u][r][v], base distribution
    private HashMap<Integer, HashMap<Integer, Integer>> trustValueU0U1; // trust value between user u and u', 1 for having a relation, otherwise 0

    // for Eq.5
    // n_{-d}^{i, v}
    private int[] nViewpointInReview;  // nViewpointInReview[v], number of times viewpoint v has been assigned to user reviews, excluding d
    // n_{-d}^{i}
    private int nViewpointInReviewSum; // nViewpointInReviewSum, number of viewpoints(that have been assigned to user reviews) excluding d
    // n_{f}^{i, y}, declared above
    // n_{v, e}^{-d}
    private int[][] nConceptInViewpoint;    // nConceptInViewpoint[v][e], number of times concept e has been assigned to viewpoint v excluding d
    // n_{v}^{-d}
    private int[] nConceptInViewpointSum;   // nConceptInViewpointSum[v], number of concepts that have been assigned to viewpoint v excluding d
    // n_{v, z}^{-d}
    private int[][] nTopicInViewpoint;  // nTopicInViewpoint[v][z], number of times topic z has been assigned to viewpoint v excluding d
    // n_{v}^{-d}
    private int[] nTopicInViewpointSum; // nTopicInViewpointSum[v], number of topics that have been assigned to viewpoint v excluding d
    // n_{z, l, v}^{w, -d}
    private int[][][][] nWordInTopicViewpointSentiment;  // nWordInTopicViewpointSentiment[z][v][l][w], number of word w in certain <z, v, l>
    // n_{z, l, v}^{-d}
    private int[][][] nWordInTopicViewpointSentimentSum; // nWordInTopicViewpointSentimentSum[z][v][l], number of words have been assigned to this z, v, l

    // for Eq.6 (excluding current word -> sampling word)
    // n_{v, k}^{-j}
    //private int[][] nTopicsInViewpoint;   // nTopicsInViewpoint[v][z], number of time a topic z that has been assigned to viewpoint v excluding w_j
    // n_{v}^{-j}
    //private int[] nTopicsInViewpointSum;  // nTopicsInViewpointSum[v], number of topics that have been assigned to viewpoint v excluding w_j
    // n_{k, l, v}^{w_j, -j} ?? the same with **nWordInTopicViewpointSentiment** before ??
    // TODO: problem
    //private int[][][][] nWordJ2ZLV; // nWordJ2ZLV[z][l][v][wj], number of time that word w_j has been assigned to topic z, sentiment l
    // n_{k, l, v}^{-j}
    // TODO: problem
    //private int[][][] nWordJ2ZLVSum;    // nWordJ2ZLVSum[z][l][v], number of words that have been assigned to topic z and sentiment l
    // n_{-j, x}^{w_j}
    private int[][] nWordJ2X;   // nWordJ2X[wj][x], number of time word j has been assigned to x excluding current word
    // n_{-j}^{w_j}
    private int[] nWordJ2XSum;  // nWordJ2XSum[wj], number of times word j has been assigned to all x excluding current word

    // for Eq.7 (a derivation from Eq.6)
    // n_{z, l, v}^{-j} ?? the same with **nWordInTopicViewpointSentimentSum** before ??
    // TODO: problem
    //private int[][][] nWord2ZVLInWords;    // nWord2ZLVInWords[z][v][l], how many words that have been assigned to topic z, sentiment l and viewpoint v
    // n_{z, v}^{-j}
    private int[][] nWordInTopicViewpoint;   // nWordInTopicViewpoint[z][v]. how many words are assigned to viewpoint v and topic z excluding w_j

    public sCVR() {

    }

    public void init() {
        rnd = new Random(seed);

        nUserRateItems = new int[U];    // init done
        nRatingViewpointInItem = new int[I][V];    // init done
        nRatingViewpointInItemSum = new int[I];      // init done
        //nUserItemViewpoints = new int[U][I][V]; // init done
        //nUserItemViewpointsSum = new int[U][I]; // init done
        nUserViewpointsRating = new int[U][V][R];
        nUserViewpointsRatingSum = new int[U][V];
        nViewpointInItem = new int[I][V];       // init done
        nViewpointInItemSum = new int[I];       // init done
        thetaUserRatingViewpoint = new double[U][R][V]; // init done DOUBT
        thetaBaseUserRatingViewpoint = new double[U][R][V]; // init done DOUBT
        nViewpointInReview = new int[V];    // init done
        nViewpointInReviewSum = RV;         // init done
        nConceptInViewpoint = new int[V][E];    // init done
        nConceptInViewpointSum = new int[V];    // init done
        nTopicInViewpoint = new int[V][K];      // init done
        nTopicInViewpointSum = new int[V];      // init done
        nWordInTopicViewpointSentiment = new int[K][V][L][N];   // init done
        nWordInTopicViewpointSentimentSum = new int[K][V][L];   // init done
        nWordInTopicViewpoint = new int[K][V];  // init done
        nWordJ2X = new int[N][X];   // init done
        nWordJ2XSum = new int[N];   // init done

        pi = new double[I][V];

        Viewpoint tmpReviewViewpoint;
        Viewpoint tmpRatingViewpoint;
        Review tmpReview;
        for (User u : users) {
            for (User u1 : u.friends) {
                trustValueU0U1.get(u.id).put(u1.id, 1);
            }
            nUserRateItems[u.id] = u.reviews.length;
            for (Item i : items) {
                tmpReview = Review.reviewDocs.get(u.id).get(i.id);
                int tmpRating = tmpReview.rating;
                // assign a random viewpoint for review
                tmpReviewViewpoint = viewpoints[rnd.nextInt(V)];
                // assign a random topic to the viewpoint for review
                tmpReviewViewpoint.topic = topics[rnd.nextInt(K)];
                tmpReviewViewpoint.sentiment = sentiments[rnd.nextInt(L)];
                tmpReviewViewpoint.concept = concepts[rnd.nextInt(E)];
                tmpReview.v = tmpReviewViewpoint;
                nUserViewpointsRating[u.id][tmpReviewViewpoint.id][tmpRating] += 1;
                nViewpointInReview[tmpReviewViewpoint.id] += 1;
                nViewpointInItem[i.id][tmpReviewViewpoint.id] += 1;
                // assign a random viewpoint for rating
                nRatingViewpointInItem[i.id][rnd.nextInt(V)] += 1;
                for (Word w : tmpReview.words) {
                    // assign a random topic
                    w.z = topics[rnd.nextInt(K)];
                    nWordInTopicViewpointSentiment[w.z.id][tmpReviewViewpoint.id][w.l.id][w.id] += 1;
                    nWordJ2X[w.id][w.x] += 1;
                }
            }
            for (Rating r : ratings) {
                for (Viewpoint v : viewpoints) {
                    // we assume uniform distribution at the very beginning
                    // TODO NOT SURE WHETHER THIS IS CORRECT
                    thetaBaseUserRatingViewpoint[u.id][r.val][v.id] = thetaUserRatingViewpoint[u.id][r.val][v.id] = 1 / (R * V);
                }
            }
        }
        for (int i = 0; i < U; i++) {
            for (int j = 0; j < V; j++) {
                for (int k = 0; k < R; k++) {
                    nUserViewpointsRatingSum[i][j] += nUserViewpointsRating[i][j][k];
                }
            }
        }
        for (int i = 0; i < I; i++) {
            for (int j = 0; j < V; j++) {
                nViewpointInItemSum[i] += nViewpointInItem[i][j];
                nRatingViewpointInItemSum[i] += nRatingViewpointInItem[i][j];
            }
        }
        for (Viewpoint v : viewpoints) {
            nConceptInViewpoint[v.id][v.concept.id] += 1;
            nTopicInViewpoint[v.id][v.topic.id] += 1;
        }
        for (int i = 0; i < V; i++) {
            for (int j = 0; j < E; j++) {
                nConceptInViewpointSum[i] += nConceptInViewpoint[i][j];
            }
            for (int j = 0; j < K; j++) {
                nTopicInViewpointSum[i] += nTopicInViewpoint[i][j];
            }
        }
        for (int i = 0; i < K; i++) {
            for (int j = 0; j < V; j++) {
                for (int p = 0; p < L; p++) {
                    for (int q = 0; q < N; q++) {
                        nWordInTopicViewpointSentimentSum[i][j][p] += nWordInTopicViewpointSentiment[i][j][p][q];
                    }
                    nWordInTopicViewpoint[i][j] += nWordInTopicViewpointSentimentSum[i][j][p];
                }
            }
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < X; j++) {
                nWordJ2XSum[i] += nWordJ2X[i][j];
            }
        }
    }

    public void inference(int nIter) {
        for (int i = 0; i < nIter; i++) {
            doEStep();
            doMStep();
        }

    }

    private void doEStep() {
        for (int u = 0; u < U; u++) {
            // Compute Eq.4 to get the updated user-rating-viewpoint distribution
            // Since for current user, we only need to update his thetaUserRatingViewpoint once with his social relation
            // we assume trusted value is either 1 or 0
            User[] friends = users[u].friends;
            int nFriends = friends.length;
            for (User u0 : friends) {
                for (int row = 0; row < R; row++) {
                    for (int col = 0; col < V; col++) {
                        thetaUserRatingViewpoint[u][row][col] += thetaUserRatingViewpoint[u0.id][row][col] / nFriends;
                    }
                }
            }
            for (int i = 0; i < I; i++) {
                // --------------- draw f_{u, i} = y from Eq.3 ---------------
                Review oldReview = Review.reviewDocs.get(u).get(i);
                if (oldReview == null) { continue; }
                Viewpoint oldViewpoint = oldReview.v;
                int oldRating = oldReview.rating;
                Rating oldRatingObj = oldReview.ratingObj;
                // excluding user u
                // TODO: how to decide which rating viewpoint to subtract... Is the following true?
                nViewpointInItem[i][oldRatingObj.viewpoint.id]--;
                nViewpointInItemSum[i]--;
                // excluding item i
                nUserViewpointsRating[u][oldViewpoint.id][oldRating]--;
                nUserViewpointsRatingSum[u][oldViewpoint.id]--;

                // Compute Eq.3 && sampling
                double[] p = new double[V];
                for (int v = 0; v < V; v++) {
                    p[v] = ((nUserViewpointsRating[u][v][oldRating] + thetaUserRatingViewpoint[u][oldRating][v]) /
                            (nUserViewpointsRatingSum[u][v] + nUserRateItems[u] * thetaUserRatingViewpoint[u][oldRating][v]))
                            *
                            ((nRatingViewpointInItem[i][v] + nViewpointInItem[i][v] + alpha) /
                                    (nRatingViewpointInItemSum[i] + nViewpointInItemSum[i] + V * alpha));
                }
                for (int v = 1; v < V; v++) {
                    p[v] += p[v - 1];
                }
                double sampleVPProb = rnd.nextDouble() * p[V - 1];
                int newViewpoint;
                for (newViewpoint = 0; newViewpoint < V; newViewpoint++) {
                    if (sampleVPProb < p[newViewpoint]) { break; }
                }
                // update
                nRatingViewpointInItem[i][newViewpoint]++;
                oldRatingObj.viewpoint = viewpoints[newViewpoint];
                nRatingViewpointInItemSum[i]++;
                nUserViewpointsRating[u][newViewpoint][oldRating]++;
                nUserViewpointsRatingSum[u][newViewpoint]++;

                // --------------- draw v_{d} = v from Eq.5 ---------------
                Topic oldTopic = oldViewpoint.topic;
                Sentiment oldSentiment = oldViewpoint.sentiment;
                // excluding review d
                nViewpointInReview[oldViewpoint.id]--;
                nViewpointInReviewSum--;
                // Compute Eq.5 && sampling
                for (int v = 0; v < V; v++) {
                    double viewpointPart = (nViewpointInReview[v] + nRatingViewpointInItem[v][i] + alpha) /
                            (nViewpointInReviewSum + nRatingViewpointInItemSum[v] + V * alpha);
                    double conceptPart = 0.0;
                    for (int e = 0; e < E; e++) {
                        // exclude review d
                        nConceptInViewpoint[oldViewpoint.id][e]--;
                        nConceptInViewpointSum[oldViewpoint.id]--;
                        conceptPart += (nConceptInViewpoint[v][e] + sigma) / (nConceptInViewpointSum[v] + E * sigma);
                    }
                    double topicPart = 0.0;
                    double tmpTopicPart = 0.0;
                    double sentimentWordPart = 0.0;
                    double wordPart = 0.0;
                    for (int z = 0; z < K; z++) {
                        // exclude review d
                        nTopicInViewpoint[oldViewpoint.id][z]--;
                        nTopicInViewpointSum[oldViewpoint.id]--;
                        tmpTopicPart = (nTopicInViewpoint[v][z] + chi) / (nTopicInViewpointSum[v] + K * chi);
                        for (int l = 0; l < L; l++) {
                            wordPart = 0.0;
                            for (Word w : oldReview.words) {
                                // exclude review d
                                nWordInTopicViewpointSentiment[oldTopic.id][oldViewpoint.id][oldSentiment.id][w.id]--;
                                nWordInTopicViewpointSentimentSum[oldTopic.id][oldViewpoint.id][oldSentiment.id]--;
                                wordPart += (nWordInTopicViewpointSentiment[z][v][l][w.id] + beta) /
                                        (nWordInTopicViewpointSentimentSum[z][v][l] + N * beta);
                            }
                            sentimentWordPart += wordPart;
                        }
                        topicPart += tmpTopicPart * sentimentWordPart;
                    }
                    p[v] = viewpointPart * conceptPart * topicPart * sentimentWordPart;
                }
                for (int v = 1; v < V; v++) {
                    p[v] += p[v - 1];
                }
                sampleVPProb = rnd.nextDouble() * p[V - 1];
                for (newViewpoint = 0; newViewpoint < V; newViewpoint++) {
                    if (sampleVPProb < p[newViewpoint]) { break; }
                }
                // update
                nViewpointInReview[newViewpoint]++;
                nViewpointInReviewSum++;
                for (int e = 0; e < E; e++) {
                    nConceptInViewpoint[newViewpoint][e]++;
                    nConceptInViewpointSum[newViewpoint]++;
                }
                for (int z = 0; z < K; z++) {
                    nTopicInViewpoint[newViewpoint][z]++;
                    nTopicInViewpointSum[newViewpoint]++;
                    for (int l = 0; l < L; l++) {
                        for (Word w : oldReview.words) {
                            nWordInTopicViewpointSentiment[z][newViewpoint][l][w.id]++;
                            nWordInTopicViewpointSentimentSum[z][newViewpoint][l]++;
                        }
                    }
                }
                oldReview.v = viewpoints[newViewpoint];

                for (int wid = 0; wid < oldReview.words.length; wid++) {
                    Word w = oldReview.words[wid];
                    Word wNext = (wid == oldReview.words.length - 1 ? w : oldReview.words[wid + 1]);
                    // --------------- draw <z_j, l_j, x_j> from Eq.6 ---------------
                    // excluding word w_j
                    nTopicInViewpoint[newViewpoint][w.z.id]--;
                    nTopicInViewpointSum[newViewpoint]--;
                    nWordInTopicViewpointSentiment[w.z.id][newViewpoint][w.l.id][w.id]--;
                    nWordInTopicViewpointSentimentSum[w.z.id][newViewpoint][w.l.id]--;
                    nWordJ2X[w.id][w.x]--;
                    nWordJ2XSum[w.id]--;

                    // Compute Eq.6 or Eq.7 and sampling
                    double part1, part2, part3, part4;
                    double[][][] wordP = new double[K][L][X];
                    double accWordP = 0.0;
                    for (int z = 0; z < K; z++) {
                        for (int l = 0; l < L; l++) {
                            for (int x = 0; x < X; x++) {
                                part1 = (nTopicInViewpoint[newViewpoint][z] + chi) /
                                        (nTopicInViewpointSum[newViewpoint] + K * chi);
                                part2 = (nWordInTopicViewpointSentiment[z][newViewpoint][l][w.id] + beta) /
                                        (nWordInTopicViewpointSentimentSum[z][newViewpoint][l] + N * beta);
                                part3 = (nWordJ2X[w.id][x] + tau[x]) / (nWordJ2XSum[w.id] + 1); // assume sum(tau) == 1
                                if (w.x == 0) {
                                    part4 = (nWordInTopicViewpointSentimentSum[z][newViewpoint][l] + eta[l]) /
                                            (nWordInTopicViewpointSentimentSum[z][newViewpoint][0] +
                                                    nWordInTopicViewpointSentimentSum[z][newViewpoint][1] +
                                                    nWordInTopicViewpointSentimentSum[z][newViewpoint][2] + 1);
                                } else {
                                    if (wNext.x == w.x) {
                                        part4 = (nWordJ2X[wNext.id][wNext.x] + 1 + tau[wNext.x]) /
                                                (nWordJ2XSum[wNext.id] + 1 + 1);
                                    } else {
                                        part4 = (nWordJ2X[wNext.id][wNext.x] + 0 + tau[wNext.x]) /
                                                (nWordJ2XSum[wNext.id] + 1 + 1);
                                    }
                                }
                                accWordP += part1 * part2 * part3 * part4;
                                wordP[z][l][x] = accWordP;
                            }
                        }
                    }
                    double sampleWordProb = accWordP * rnd.nextDouble();
                    int newZ = 0, newL = 0, newX = 0;
                    for (newZ = 0; newZ < K; newZ++) {
                        for (newL = 0; newL < L; newL++) {
                            for (newX = 0; newX < X; newX++) {
                                if (sampleWordProb < wordP[newZ][newL][newX]) { break; }
                            }
                        }
                    }
                    w.z = topics[newZ];
                    w.l = sentiments[newL];
                    w.x = newX;
                    nTopicInViewpoint[newViewpoint][newZ]++;
                    nTopicInViewpointSum[newViewpoint]++;
                    nWordInTopicViewpointSentiment[newZ][newViewpoint][newL][w.id]++;
                    nWordInTopicViewpointSentimentSum[newZ][newViewpoint][newL]++;
                    nWordJ2X[w.id][w.x]++;
                    nWordJ2XSum[w.id]++;
                }
            }
        }


    }

    private void doMStep() {
        // re-estimate theta, pi, fai, mew, lambda from Eq. 8

        // re-estimate theta
        for (int u = 0; u < U; u++) {
            for (int r = 0; r < R; r++) {
                for (int v = 0; v < V; v++) {
                    User[] friends = users[u].friends;
                    int nFriends = friends.length;
                    double sumFriendsInfluence = 0.0;
                    for (User f : friends) {
                        sumFriendsInfluence += trustValueU0U1.get(u).get(f.id) * thetaUserRatingViewpoint[f.id][r][v] / nFriends;
                    }
                    sumFriendsInfluence += thetaBaseUserRatingViewpoint[u][r][v];
                    thetaUserRatingViewpoint[u][r][v] = (nUserViewpointsRating[u][v][r] + sumFriendsInfluence) /
                            (nUserViewpointsRatingSum[u][v] + nUserRateItems[u] * sumFriendsInfluence);
                }
            }
        }

        // re-estimate pi
        for (int i = 0; i < I; i++) {
            for (int v = 0; v < V; v++) {
                pi[i][v] = (nViewpointInItem[i][v] + alpha) / (nViewpointInItemSum[i] + V * alpha);
            }
        }

        // not sure whether it is necessary to update fai, mew or lambda

        // maximize baseTheta from Eq.9
    }

}
