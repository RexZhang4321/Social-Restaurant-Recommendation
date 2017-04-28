package sCVR.model;

import sCVR.types.*;
import sCVR.Utils;

import java.util.*;

/**
 * Created by RexZhang on 4/10/17.
 */
public class sCVR {

    private Random rnd;
    private final static long seed = 0;

    public double alpha = 0.01;
    public double sigma = 0.01;
    public double chi = 0.01;
    public double beta = 0.01;
    public double tau[] = {0.3, 0.4, 0.3};
    public double eta[] = {0.3, 0.4, 0.3};

    public double pi[][];

    /* -------------- Gibbs Sampling Variables Start ------------ */

    // n_{z, l, v}^{w, -d}
    // number of word w in certain <z, v, l>
    // nWordInTopicViewpointSentiment[z][v][l][w]
    public int[][][][] nWordInTopicViewpointSentiment;    // done

    // n_{z, l, v}^{-d}
    // number of words have been assigned to this z, v, l
    // nWordInTopicViewpointSentimentSum[z][v][l]
    public int[][][] nWordInTopicViewpointSentimentSum;  // done

    // n_{z, v}^{-j}
    // how many words are assigned to viewpoint v and topic z excluding w_j
    // nWordInTopicViewpoint[z][v]
    public int[][] nWordInTopicViewpoint;    // done

    // n_{-j, x}^{w_j}
    // number of time word j has been assigned to x excluding current word
    // nWordJInX[w][x]
    public int[][] nWordJInX; // done

    // n_{-j}^{w_j}
    // number of times word j has been assigned to all x excluding current word
    // nWordJInXSum[w]
    public int[] nWordJInXSum;    // done

    /* -------------- Gibbs Sampling Variables End------------ */

    public sCVR(ArrayList<Review> reviews, ArrayList<Item> items, ArrayList<User> users) {
        setUpGlobals(reviews, items, users);
        init();
    }

    // do the linkage and setup globals
    public void setUpGlobals(ArrayList<Review> reviews, ArrayList<Item> items, ArrayList<User> users) {

    }

    public void init() {
        rnd = new Random(seed);



        pi = new double[Globals.I][Globals.V];

        nWordInTopicViewpointSentiment = new int[Globals.K][Globals.V][Globals.L][Globals.N];
        nWordInTopicViewpointSentimentSum = new int[Globals.K][Globals.V][Globals.L];
        nWordInTopicViewpoint = new int[Globals.K][Globals.V];
        nWordJInX = new int[Globals.N][Globals.X];
        nWordJInXSum = new int[Globals.N];

        // Randomize latent variables
        for (User u : Globals.users) {
            for (Review review : u.reviews) {
                review.ratingViewpoint = Globals.viewpoints[rnd.nextInt(Globals.V)];
                review.reviewViewpoint = Globals.viewpoints[rnd.nextInt(Globals.V)];
                for (Word word : review.words) {
                    word.topic = Globals.topics[rnd.nextInt(Globals.K)];
                    word.sentiment = rnd.nextInt(Globals.L);
                    word.x = rnd.nextInt(Globals.X);
                    review.topicCnt[word.topic.id]++;
                }
            }
            for (int r = 0; r < Globals.R; r++) {
                for (int v = 0; v < Globals.V; v++) {
                    u.theta0RatingViewpoint[r][v] = u.thetaRatingViewpoint[r][v] = 1 / (Globals.R * Globals.V);
                }
            }
        }

        for (Item item : Globals.items) {
            for (Review review : item.reviews) {
                item.nRatingViewpointInItem[review.ratingViewpoint.id]++;
                item.nRatingViewpointInItemSum++;
                item.nReviewViewpointInItem[review.reviewViewpoint.id]++;
                item.nReviewViewpointInItemSum++;
                //item.nReviewViewpointInReviews[review.reviewViewpoint.id]++;
                //item.nReviewViewpointInReviewsSum++;
            }
        }

        for (User user : Globals.users) {
            user.nAllItemRatings = user.reviews.size();
            for (Review review : user.reviews) {
                for (Word word : review.words) {
                    nWordInTopicViewpointSentiment[word.topic.id][review.reviewViewpoint.id][word.sentiment][word.id]++;
                    nWordJInX[word.x][word.id]++;
                    nWordJInXSum[word.id]++;
                    nWordInTopicViewpointSentimentSum[word.topic.id][review.reviewViewpoint.id][word.sentiment]++;
                    nWordInTopicViewpoint[word.topic.id][review.reviewViewpoint.id]++;
                }
            }
        }

        for (Review review : Globals.reviews) {
            Globals.viewpoints[review.ratingViewpoint.id].nRatingViewpointsForRating[review.user.id][review.rating]++;
            Globals.viewpoints[review.ratingViewpoint.id].nRatingViewpointsForRatingSum[review.user.id]++;
            Globals.viewpoints[review.reviewViewpoint.id].nConceptInViewpoint[review.concept.id]++;
            Globals.viewpoints[review.reviewViewpoint.id].nConceptInViewpointSum++;
            for (int i = 0; i < review.topicCnt.length; i++) {
                Globals.viewpoints[review.reviewViewpoint.id].nTopicInViewpoint[i] += review.topicCnt[i];
                Globals.viewpoints[review.reviewViewpoint.id].nTopicInViewpointSum += review.topicCnt[i];
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
        double p_V[] = new double[Globals.V];
        for (User user : Globals.users) {

            // Compute Eq.4 to get the updated user-rating-viewpoint distribution
            // Since for current user, we only need to update his thetaUserRatingViewpoint once with his social relation
            // we assume trusted value is either 1 or 0
            int nFriends = user.friends.size();
            for (User friend : user.friends) {
                for (int row = 0; row < Globals.R; row++) {
                    for (int col = 0; col < Globals.V; col++) {
                        user.thetaRatingViewpoint[row][col] += friend.thetaRatingViewpoint[row][col] / nFriends;
                    }
                }
            }
            for (Item item : user.items) {

                Review oldReview = Globals.reviewMap.get(user.id).get(item.id);
                Viewpoint oldRatingViewpoint = oldReview.ratingViewpoint;
                Viewpoint oldReviewViewpoint = oldReview.reviewViewpoint;
                int oldRating = oldReview.rating;

                // --------------- draw f_{u, i} = y from Eq.3 ---------------

                // excluding user u : item.nRatingViewpointInItem
                int relateToUser = 0;
                for (Review review : item.reviews) {
                    if (review.user.id == user.id && review.ratingViewpoint.id == oldRatingViewpoint.id) {
                        relateToUser++;
                    }
                }
                item.nRatingViewpointInItem[oldRatingViewpoint.id] -= relateToUser;
                item.nRatingViewpointInItemSum -= relateToUser;

                // excluding item i : oldRatingViewpoint.nRatingViewpointsForRating
                int relateToItem = 0;
                for (Review review : user.reviews) {
                    if (review.item.id == item.id && review.rating == oldRating && review.ratingViewpoint.id == oldRatingViewpoint.id) {
                        relateToItem++;
                    }
                }
                oldRatingViewpoint.nRatingViewpointsForRating[user.id][oldRating] -= relateToItem;
                oldRatingViewpoint.nRatingViewpointsForRatingSum[user.id] -= relateToItem;

                // sampling
                for (int y = 0; y < Globals.V; y++) {
                    p_V[y] = ((oldRatingViewpoint.nRatingViewpointsForRating[user.id][oldRating] + user.thetaRatingViewpoint[oldRating][y]) /
                              (oldRatingViewpoint.nRatingViewpointsForRatingSum[user.id] + user.nAllItemRatings * user.thetaRatingViewpoint[oldRating][y]))
                             *
                             ((item.nRatingViewpointInItem[y] + item.nReviewViewpointInItem[y] + alpha) /
                                     (item.nRatingViewpointInItemSum + item.nReviewViewpointInItemSum + Globals.V * alpha));
                }
                for (int y = 1; y < Globals.V; y++) {
                    p_V[y] += p_V[y - 1];
                }
                double sampleRatingViewpointProb = rnd.nextDouble() * p_V[Globals.V - 1];
                int newRatingViewpoint;
                for (newRatingViewpoint = 0; newRatingViewpoint < Globals.V; newRatingViewpoint++) {
                    if (sampleRatingViewpointProb < p_V[newRatingViewpoint]) { break; }
                }

                // update ss
                item.nRatingViewpointInItem[newRatingViewpoint] += relateToUser;
                item.nRatingViewpointInItemSum += relateToUser;
                Globals.viewpoints[newRatingViewpoint].nRatingViewpointsForRating[user.id][oldRating] += relateToItem;
                Globals.viewpoints[newRatingViewpoint].nRatingViewpointsForRatingSum[user.id] += relateToItem;

                // update vars
                for (Review review : item.reviews) {
                    if (review.user.id == user.id && review.ratingViewpoint.id == oldRatingViewpoint.id) {
                        review.ratingViewpoint = Globals.viewpoints[newRatingViewpoint];
                    }
                }
                for (Review review : user.reviews) {
                    if (review.item.id == item.id && review.rating == oldRating && review.ratingViewpoint.id == oldRatingViewpoint.id) {
                        review.ratingViewpoint = Globals.viewpoints[newRatingViewpoint];
                    }
                }

                // --------------- draw v_{d} = v from Eq.5 ---------------

                // excluding d : nReviewViewpointInItem
                item.nReviewViewpointInItem[oldReviewViewpoint.id]--;
                item.nReviewViewpointInItemSum--;
                oldReviewViewpoint.nConceptInViewpoint[oldReview.concept.id]--;
                oldReviewViewpoint.nConceptInViewpointSum--;
                /*
                //this should be equivalent to the following inside word loop
                for (int i = 0; i < oldReview.topicCnt.length; i++) {
                    oldReviewViewpoint.nTopicInViewpoint[i] -= oldReview.topicCnt[i];
                    oldReviewViewpoint.nTopicInViewpointSum -= oldReview.topicCnt[i];
                }
                */
                for (Word word : oldReview.words) {
                    nWordInTopicViewpointSentiment[word.topic.id][oldReviewViewpoint.id][oldReview.sentiment][word.id]--;
                    nWordInTopicViewpointSentimentSum[word.topic.id][oldReviewViewpoint.id][oldReview.sentiment]--;
                    oldReviewViewpoint.nTopicInViewpoint[word.topic.id]--;
                    oldReviewViewpoint.nTopicInViewpointSum--;
                }

                for (int v = 0; v < Globals.V; v++) {
                    Viewpoint curVP = Globals.viewpoints[v];
                    double viewpointPart = (item.nReviewViewpointInItem[v] + item.nRatingViewpointInItem[v] + alpha) /
                            (item.nReviewViewpointInItemSum + item.nRatingViewpointInItemSum + Globals.V * alpha);
                    double conceptPart = 0.0;
                    for (int e = 0; e < Globals.E; e++) {
                        conceptPart += (curVP.nConceptInViewpoint[e] + sigma) / (curVP.nConceptInViewpointSum + Globals.E * sigma);
                    }
                    double topicPart = 0.0;
                    double tmpTopicPart;
                    double sentimentWordPart = 0.0;
                    double wordPart;
                    for (int z = 0; z < Globals.K; z++) {
                        tmpTopicPart = (curVP.nTopicInViewpoint[z] + chi) / (curVP.nTopicInViewpointSum + Globals.K * chi);
                        for (int l = 0; l < Globals.L; l++) {
                            wordPart = 0.0;
                            for (Word word : oldReview.words) {
                                wordPart += (nWordInTopicViewpointSentiment[z][v][l][word.id] + beta) /
                                        (nWordInTopicViewpointSentimentSum[z][v][l] + Globals.N * beta);
                            }
                            sentimentWordPart += wordPart;
                        }
                        topicPart += tmpTopicPart * sentimentWordPart;
                    }
                    p_V[v] = viewpointPart * conceptPart * topicPart;
                }
                for (int v = 1; v < Globals.V; v++) {
                    p_V[v] += p_V[v - 1];
                }
                double sampleReviewViewpointProb = rnd.nextDouble() * p_V[Globals.V - 1];
                int newReviewViewpoint;
                for (newReviewViewpoint = 0; newReviewViewpoint < Globals.V; newReviewViewpoint++) {
                    if (sampleReviewViewpointProb < p_V[newReviewViewpoint]) { break; }
                }

                // update ss
                Viewpoint newVP = Globals.viewpoints[newReviewViewpoint];
                item.nReviewViewpointInItem[newReviewViewpoint]++;
                item.nReviewViewpointInItemSum++;
                newVP.nConceptInViewpoint[oldReview.concept.id]++;
                newVP.nConceptInViewpointSum++;
                for (Word word : oldReview.words) {
                    nWordInTopicViewpointSentiment[word.topic.id][newReviewViewpoint][oldReview.sentiment][word.id]--;
                    nWordInTopicViewpointSentimentSum[word.topic.id][newReviewViewpoint][oldReview.sentiment]--;
                    oldReviewViewpoint.nTopicInViewpoint[word.topic.id]++;
                    oldReviewViewpoint.nTopicInViewpointSum++;
                }

                // update vars
                oldReview.reviewViewpoint = newVP;

                for (int i = 0; i < oldReview.words.size(); i++) {
                    Word w = oldReview.words.get(i);
                    Word wNext = (i == oldReview.words.size() - 1 ? w : oldReview.words.get(i + 1));

                    // --------------- draw <z_j, l_j, x_j> from Eq.6 ---------------

                    // excluding word w_j
                    newVP.nTopicInViewpoint[w.id]--;
                    newVP.nTopicInViewpointSum--;
                    nWordInTopicViewpointSentiment[w.topic.id][newVP.id][oldReview.sentiment][w.id]--;
                    nWordInTopicViewpointSentimentSum[w.topic.id][newVP.id][oldReview.sentiment]--;
                    nWordJInX[w.id][w.x]--;
                    nWordJInXSum[w.id]--;
                    if (w.x != 0) {
                        nWordJInX[wNext.id][wNext.x]--;
                        nWordJInXSum[wNext.id]--;
                    }

                    // sampling
                    double part1, part2, part3, part4;
                    double[][][] wordP = new double[Globals.K][Globals.L][Globals.X];
                    double accumulateWordP = 0.0;
                    for (int z = 0; z < Globals.K; z++) {
                        for (int l = 0; l < Globals.L; l++) {
                            for (int x = 0; x < Globals.X; x++) {
                                part1 = (newVP.nTopicInViewpoint[z] + chi) / (newVP.nTopicInViewpointSum + Globals.K * chi);
                                part2 = (nWordInTopicViewpointSentiment[z][newVP.id][l][w.id] + beta) /
                                        (nWordInTopicViewpointSentimentSum[z][newVP.id][l] + Globals.N * beta);
                                part3 = (nWordJInX[w.id][x] + tau[x]) / (nWordJInXSum[w.id] + 1);
                                if (w.x == 0) {
                                    part4 = (nWordInTopicViewpointSentimentSum[z][newVP.id][l] + eta[l]) /
                                            (nWordInTopicViewpointSentimentSum[z][newVP.id][1] +
                                                    nWordInTopicViewpointSentimentSum[z][newVP.id][2] +
                                                    nWordInTopicViewpointSentimentSum[z][newVP.id][3] + 1);
                                } else {
                                    if (wNext.x == w.x) {
                                        part4 = (nWordJInX[wNext.id][wNext.x] + 1 + tau[wNext.x]) /
                                                (nWordJInXSum[wNext.id] + 1 + 1);
                                    } else {
                                        part4 = (nWordJInX[wNext.id][wNext.x] + 0 + tau[wNext.x]) /
                                                (nWordJInXSum[wNext.id] + 1 + 1);
                                    }
                                }
                                accumulateWordP += part1 * part2 * part3 * part4;
                                wordP[z][l][x] = accumulateWordP;
                            }
                        }
                    }
                    double sampleWordProb = accumulateWordP * rnd.nextDouble();
                    int newZ, newL = 0, newX = 0;
                    for (newZ = 0; newZ < Globals.K; newZ++) {
                        for (newL = 0; newL < Globals.L; newL++) {
                            for (newX = 0; newX < Globals.X; newX++) {
                                if (sampleWordProb < wordP[newZ][newL][newX]) { break; }
                            }
                        }
                    }
                    w.topic = Globals.topics[newZ];
                    w.sentiment = newL;
                    w.x = newX;
                    newVP.nTopicInViewpoint[newZ]++;
                    newVP.nTopicInViewpointSum++;
                    nWordInTopicViewpointSentiment[newZ][newVP.id][newL][w.id]++;
                    nWordInTopicViewpointSentimentSum[newZ][newVP.id][newL]++;
                    nWordJInX[w.id][newX]++;
                    nWordJInXSum[w.id]++;
                }
            }
        }
    }

    private void doMStep() {
        // re-estimate theta, pi, fai, mew, lambda from Eq. 8

        // re-estimate theta
        for (int u = 0; u < Globals.U; u++) {
            for (int r = 0; r < Globals.R; r++) {
                for (int v = 0; v < Globals.V; v++) {
                    User curUser = Globals.users[u];
                    ArrayList<User> friends = curUser.friends;
                    int nFriends = friends.size();
                    double sumFriendsInfluence = 0.0;
                    for (User f : friends) {
                        sumFriendsInfluence += User.trustValueU0U1.get(u).get(f.id) * f.thetaRatingViewpoint[r][v] / nFriends;
                    }
                    sumFriendsInfluence += curUser.theta0RatingViewpoint[r][v];
                    curUser.thetaRatingViewpoint[r][v] = (Globals.viewpoints[v].nRatingViewpointsForRating[u][r] + sumFriendsInfluence) /
                            (Globals.viewpoints[v].nRatingViewpointsForRatingSum[u] + curUser.nAllItemRatings * sumFriendsInfluence);
                }
            }
        }

        // re-estimate pi
        for (int i = 0; i < Globals.I; i++) {
            for (int v = 0; v < Globals.V; v++) {
                pi[i][v] = (Globals.items[i].nReviewViewpointInItem[v] + alpha) / (Globals.items[i].nReviewViewpointInItemSum + Globals.V * alpha);
            }
        }

        // it is not necessary to update fai, mew or lambda

        // maximize baseTheta from Eq.9
        for (User u : Globals.users) {
            for (int r = 0; r < Globals.R; r++) {
                double numerator = 0.0;
                double denominator = 0.0;
                for (Viewpoint v : Globals.viewpoints) {
                    numerator += Utils.digamma(v.nRatingViewpointsForRating[u.id][v.id] + u.thetaRatingViewpoint[r][v.id])
                            - Utils.digamma(u.thetaRatingViewpoint[r][v.id]);
                    denominator += Utils.digamma(v.nRatingViewpointsForRatingSum[u.id] + u.nAllItemRatings * u.thetaRatingViewpoint[r][v.id])
                            - Utils.digamma(u.nAllItemRatings * u.thetaRatingViewpoint[r][v.id]);
                }
                double tmp = numerator / denominator;
                for (int v = 0; v < Globals.V; v++) {
                    u.theta0RatingViewpoint[r][v] = u.theta0RatingViewpoint[r][v] * tmp;
                }
            }
        }
    }

}
