package sCVR.model;

import sCVR.types.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

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

    public sCVR() {
        init();
    }

    public void init() {
        rnd = new Random(seed);

        pi = new double[Globals.I][Globals.V];

        // Randomize latent variables
        for (User u : Globals.users) {
            for (Review review : u.reviews) {
                review.ratingViewpoint = Globals.viewpoints[rnd.nextInt(Globals.V)];
                review.reviewViewpoint = Globals.viewpoints[rnd.nextInt(Globals.V)];
                review.topic = Globals.topics[rnd.nextInt(Globals.K)];
                for (Word word : review.words) {
                    word.topic = Globals.topics[rnd.nextInt(Globals.K)];
                    word.sentiment = Globals.sentiments[rnd.nextInt(Globals.L)];
                    word.x = rnd.nextInt(Globals.X);
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
                    Globals.words[word.id].nWordInTopicViewpointSentiment[word.topic.id][review.reviewViewpoint.id][word.sentiment.id]++;
                    Globals.words[word.id].nWordJInX[word.x]++;
                    Globals.words[word.id].nWordJInXSum++;
                    Word.nWordInTopicViewpointSentimentSum[word.topic.id][review.reviewViewpoint.id][word.sentiment.id]++;
                    Word.nWordInTopicViewpoint[word.topic.id][review.reviewViewpoint.id]++;
                }
            }
        }

        for (Review review : Globals.reviews) {
            Globals.viewpoints[review.ratingViewpoint.id].nRatingViewpointsForRating[review.user.id][review.rating]++;
            Globals.viewpoints[review.ratingViewpoint.id].nRatingViewpointsForRatingSum[review.user.id]++;
            Globals.viewpoints[review.reviewViewpoint.id].nConceptInViewpoint[review.concept.id]++;
            Globals.viewpoints[review.reviewViewpoint.id].nConceptInViewpointSum++;
            // TODO: How to define the topic of viewpoint? Binding the topic to review as the topic of this viewpoint?
            Globals.viewpoints[review.reviewViewpoint.id].nTopicInViewpoint[review.topic.id]++;
            Globals.viewpoints[review.reviewViewpoint.id].nTopicInViewpointSum++;
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
                    // TODO: in reviewViewpoint, do we need to sample as the ratingViewpoint does? item.nReviewViewpointInItem[oldReviewViewpoint.id]
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

                for (int v = 0; v < Globals.V; v++) {
                    Viewpoint curVP = Globals.viewpoints[v];
                    double viewpointPart = (item.nReviewViewpointInItem[v] + item.nRatingViewpointInItem[v] + alpha) /
                            (item.nReviewViewpointInItemSum + item.nRatingViewpointInItemSum + Globals.V * alpha);
                    double conceptPart = 0.0;
                    for (int e = 0; e < Globals.E; e++) {
                        // excluding d : oldReviewViewpoint.nConceptInViewpoint
                        // TODO: how should I express "excluding d" here?? It's weired
                        if (oldReviewViewpoint.nConceptInViewpoint[e] > 0) {
                            oldReviewViewpoint.nConceptInViewpoint[e]--;
                            oldReviewViewpoint.nConceptInViewpointSum--;
                        }
                        conceptPart += (curVP.nConceptInViewpoint[e] + sigma) / (curVP.nConceptInViewpointSum + Globals.E * sigma);
                    }
                    double topicPart = 0.0;
                    double tmpTopicPart = 0.0;
                    double sentimentWordPart = 0.0;
                    double wordPart = 0.0;
                    for (int z = 0; z < Globals.K; z++) {
                        // excluding d : oldReviewViewpoint.nTopicInViewpoint
                        if (oldReviewViewpoint.nTopicInViewpoint[z] > 0) {
                            oldReviewViewpoint.nTopicInViewpoint[z]--;
                            oldReviewViewpoint.nTopicInViewpointSum--;
                        }
                        tmpTopicPart = (curVP.nTopicInViewpoint[z] + chi) / (curVP.nTopicInViewpointSum + Globals.K * chi);
                        for (int l = 0; l < Globals.L; l++) {
                            // excluding d : word.....
                            wordPart = 0.0;
                            HashMap<Integer, Integer> hm = new HashMap<>();
                            for (Word word : oldReview.words) {
                                hm.put(word.id, hm.getOrDefault(word.id, 0) + 1);
                            }
                            for (int wid : hm.keySet()) {
                                Word word = Globals.words[wid];
                                if (word.nWordInTopicViewpointSentiment[z][v][l] > 0) {
                                    word.nWordInTopicViewpointSentiment[z][v][l] -= hm.get(wid);
                                    Word.nWordInTopicViewpointSentimentSum[z][v][l] -= hm.get(wid);
                                }
                                wordPart += (word.nWordInTopicViewpointSentiment[z][v][l] + beta) /
                                        (Word.nWordInTopicViewpointSentimentSum[z][v][l] + Globals.N * beta);
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
                double sampleReviewViewpoint = rnd.nextDouble() * p_V[Globals.V - 1];
                for (newRatingViewpoint = 0; newRatingViewpoint < Globals.V; newRatingViewpoint++) {
                    if (sampleRatingViewpointProb < p_V[newRatingViewpoint]) { break; }
                }

                // update

            }
        }

        // --------------- draw <z_j, l_j, x_j> from Eq.6 ---------------
    }

    private void doMStep() {
        // re-estimate theta, pi, fai, mew, lambda from Eq. 8

        // re-estimate theta


        // re-estimate pi


        // not sure whether it is necessary to update fai, mew or lambda

        // maximize baseTheta from Eq.9
    }

}
