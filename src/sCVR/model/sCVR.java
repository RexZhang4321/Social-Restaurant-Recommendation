package sCVR.model;

import javafx.util.Pair;
import org.joda.time.LocalDateTime;
import sCVR.types.*;
import sCVR.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by RexZhang on 4/10/17.
 */
public class sCVR {

    private Random rnd;
    private final static long seed = 0;

    private double alpha = 0.05;
    private double sigma = 0.05;
    private double chi = 0.05;
    private double beta = 0.05;
    private double tau[] = {0.3, 0.4, 0.3};
    private double eta[] = {0.5, 0.5};

    private double pi[][];

    /* -------------- Gibbs Sampling Variables Start ------------ */

    // n_{z, l, v}^{w, -d}
    // number of word w in certain <z, v, l>
    // nWordInTopicViewpointSentiment[z][v][l][w]
    private int[][][][] nWordInTopicViewpointSentiment;    // done

    // n_{z, l, v}^{-d}
    // number of words have been assigned to this z, v, l
    // nWordInTopicViewpointSentimentSum[z][v][l]
    private int[][][] nWordInTopicViewpointSentimentSum;  // done

    // n_{-j, x}^{w_j}
    // number of time word j has been assigned to x excluding current word
    // nWordJInX[w][x]
    private int[][] nWordJInX; // done

    // n_{-j}^{w_j}
    // number of times word j has been assigned to all x excluding current word
    // nWordJInXSum[w]
    private int[] nWordJInXSum;    // done

    /* -------------- Gibbs Sampling Variables End------------ */

    // for prediction
    public static double[][][] userThetaRatingViewpoint;

    public static HashMap<Concept, Topic> conceptTopicHashMap;

    HashMap<Topic, String[]> topicWordsHashMap;

    public sCVR() {
        readConfig();
        System.out.println("Current parameter set:\n" +
                "alpha: " + alpha + "\n" +
                "sigma: " + sigma + "\n" +
                "chi: " + chi + "\n" +
                "beta: " + beta + "\n" +
                "tau: " + tau[0] + ", " + tau[1] + ", " + tau[2] + "\n" +
                "eta: " + eta[0] + ", " + eta[1] + "\n" +
                "number of viewpoints: " + Globals.V + "\n" +
                "number of topics: " + Globals.K
        );
        init();
    }

    private void readConfig() {
        try {
            Properties properties = new Properties();
            FileInputStream in = new FileInputStream("config.txt");
            properties.load(in);
            in.close();
            alpha = Double.parseDouble(properties.getProperty("ALPHA"));
            sigma = Double.parseDouble(properties.getProperty("SIGMA"));
            chi = Double.parseDouble(properties.getProperty("CHI"));
            beta = Double.parseDouble(properties.getProperty("BETA"));
            for (int i = 0; i < tau.length; i++) {
                tau[i] = Double.parseDouble(properties.getProperty("TAU_" + i));
            }
            for (int i = 0; i < eta.length; i++) {
                eta[i] = Double.parseDouble(properties.getProperty("ETA_" + i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        rnd = new Random(seed);

        Globals.viewpoints = new Viewpoint[Globals.V];
        for (int v = 0; v < Globals.V; v++) {
            Globals.viewpoints[v] = new Viewpoint(v);
        }
        Globals.topics = new Topic[Globals.K];
        for (int z = 0; z < Globals.K; z++) {
            Globals.topics[z] = new Topic(z);
        }
        Globals.reviewMap = new HashMap<Integer, HashMap<Integer, Review>>();

        pi = new double[Globals.I][Globals.V];

        nWordInTopicViewpointSentiment = new int[Globals.K][Globals.V][Globals.L][Globals.N];
        nWordInTopicViewpointSentimentSum = new int[Globals.K][Globals.V][Globals.L];
        nWordJInX = new int[Globals.N][Globals.X];
        nWordJInXSum = new int[Globals.N];

        // Randomize latent variables
        for (User u : Globals.users) {
            for (Review review : u.reviews) {
                review.ratingViewpoint = Globals.viewpoints[rnd.nextInt(Globals.V)];
                review.reviewViewpoint = Globals.viewpoints[rnd.nextInt(Globals.V)];
                for (Word cur : review.words) {
                    // -1 => 0, 0 => 1, 1 => 2
                    if (Globals.conjWord.contains(cur.word)) {
                        cur.x = 2;
                    } else if (Globals.advWord.contains(cur.word)) {
                        cur.x = 0;
                    } else {
                        cur.x = 1;
                    }
                    cur.topic = Globals.topics[rnd.nextInt(Globals.K)];
                    review.topicCnt[cur.topic.id]++;
                }
                if (!Globals.reviewMap.containsKey(u.id)) {
                    Globals.reviewMap.put(u.id, new HashMap<Integer, Review>());
                }
                Globals.reviewMap.get(u.id).put(review.item.id, review);
            }
            for (int r = 0; r < Globals.R; r++) {
                for (int v = 0; v < Globals.V; v++) {
                    u.theta0RatingViewpoint[r][v] = 1.0 / (Globals.R);
                    u.thetaRatingViewpoint[r][v] = 1.0 / (Globals.R);
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
                    nWordJInX[word.id][word.x]++;
                    nWordJInXSum[word.id]++;
                    nWordInTopicViewpointSentimentSum[word.topic.id][review.reviewViewpoint.id][word.sentiment]++;
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
        System.out.println("Starting EM...");
        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        for (int i = 0; i < nIter; i++) {
            System.out.println(LocalDateTime.now() + ": Iter " + (i + 1));
            doEStep();
            System.out.println(LocalDateTime.now() + ": E-step done");
            doMStep();
            System.out.println(LocalDateTime.now() + ": M-step done");
        }
        System.out.println(LocalDateTime.now() + ": EM done");
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
//                    System.out.println(oldRatingViewpoint.nRatingViewpointsForRating[user.id][oldRating]);
//                    System.out.println(user.thetaRatingViewpoint[oldRating][y]);
//                    System.out.println(oldRatingViewpoint.nRatingViewpointsForRatingSum[user.id]);
//                    System.out.println(user.nAllItemRatings);
//                    System.out.println(user.thetaRatingViewpoint[oldRating][y]);
//                    System.out.println(item.nRatingViewpointInItem[y]);
//                    System.out.println(item.nReviewViewpointInItem[y]);
//                    System.out.println(item.nRatingViewpointInItemSum);
//                    System.out.println(item.nReviewViewpointInItemSum);
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
                if (newRatingViewpoint == Globals.V) {
                    newRatingViewpoint--;
                }
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
                    nWordInTopicViewpointSentiment[word.topic.id][oldReviewViewpoint.id][word.sentiment][word.id]--;
                    nWordInTopicViewpointSentimentSum[word.topic.id][oldReviewViewpoint.id][word.sentiment]--;
                    oldReviewViewpoint.nTopicInViewpoint[word.topic.id]--;
                    oldReviewViewpoint.nTopicInViewpointSum--;
                }

                // TODO: need to convert to log space
                for (int v = 0; v < Globals.V; v++) {
                    Viewpoint curVP = Globals.viewpoints[v];
                    double viewpointPart = Math.log((item.nReviewViewpointInItem[v] + item.nRatingViewpointInItem[v] + alpha) /
                            (item.nReviewViewpointInItemSum + item.nRatingViewpointInItemSum + Globals.V * alpha));
                    double conceptPart = 0.0;
                    for (int e = 0; e < Globals.E; e++) {
                        conceptPart += Math.log((curVP.nConceptInViewpoint[e] + sigma) / (curVP.nConceptInViewpointSum + Globals.E * sigma));
                    }
                    double topicPart = 0.0;
                    double tmpTopicPart;
                    double sentimentWordPart = 0.0;
                    double wordPart;
                    for (int z = 0; z < Globals.K; z++) {
                        tmpTopicPart = Math.log((curVP.nTopicInViewpoint[z] + chi) / (curVP.nTopicInViewpointSum + Globals.K * chi));
                        for (int l = 0; l < Globals.L; l++) {
                            wordPart = 0.0;
                            for (Word word : oldReview.words) {
                                wordPart += Math.log((nWordInTopicViewpointSentiment[z][v][l][word.id] + beta) /
                                        (nWordInTopicViewpointSentimentSum[z][v][l] + Globals.N * beta));
                            }
                            sentimentWordPart += wordPart;
                        }
                        topicPart += tmpTopicPart + sentimentWordPart;
                    }
                    p_V[v] = viewpointPart + conceptPart + topicPart;
                }
                for (int v = 1; v < Globals.V; v++) {
                    p_V[v] += p_V[v - 1];
                }
                double sampleReviewViewpointProb = rnd.nextDouble() * p_V[Globals.V - 1];
                int newReviewViewpoint;
                for (newReviewViewpoint = 0; newReviewViewpoint < Globals.V; newReviewViewpoint++) {
                    if (sampleReviewViewpointProb > p_V[newReviewViewpoint]) { break; }
                }
                if (newReviewViewpoint == Globals.V) {
                    newReviewViewpoint--;
                }

                // update ss
                Viewpoint newVP = Globals.viewpoints[newReviewViewpoint];
                item.nReviewViewpointInItem[newReviewViewpoint]++;
                item.nReviewViewpointInItemSum++;
                newVP.nConceptInViewpoint[oldReview.concept.id]++;
                newVP.nConceptInViewpointSum++;
                for (Word word : oldReview.words) {
                    nWordInTopicViewpointSentiment[word.topic.id][newReviewViewpoint][word.sentiment][word.id]++;
                    nWordInTopicViewpointSentimentSum[word.topic.id][newReviewViewpoint][word.sentiment]++;
                    newVP.nTopicInViewpoint[word.topic.id]++;
                    newVP.nTopicInViewpointSum++;
                }

                // update vars
                oldReview.reviewViewpoint = newVP;

                for (int i = 0; i < oldReview.words.size(); i++) {
                    Word w = oldReview.words.get(i);
                    Word wNext = (i == oldReview.words.size() - 1 ? w : oldReview.words.get(i + 1));

                    // --------------- draw <z_j, l_j, x_j> from Eq.6 ---------------

                    // excluding word w_j
                    newVP.nTopicInViewpoint[w.topic.id]--;
                    newVP.nTopicInViewpointSum--;
                    nWordInTopicViewpointSentiment[w.topic.id][newVP.id][w.sentiment][w.id]--;
                    nWordInTopicViewpointSentimentSum[w.topic.id][newVP.id][w.sentiment]--;
                    nWordJInX[w.id][w.x]--;
                    nWordJInXSum[w.id]--;
                    if (w.x != 0 && wNext.id != w.id) {
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
                                            (nWordInTopicViewpointSentimentSum[z][newVP.id][0] +
                                                    nWordInTopicViewpointSentimentSum[z][newVP.id][1] + 1);
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
                                //System.out.println("z: " + z + ", l: " + l + ", x: " + x + " : " + accumulateWordP);
                            }
                        }
                    }
                    double sampleWordProb = accumulateWordP * rnd.nextDouble();
                    int newZ, newL = 0, newX = 0;
                    boolean breakFlg = false;
                    for (newZ = 0; newZ < Globals.K; newZ++) {
                        for (newL = 0; newL < Globals.L; newL++) {
                            for (newX = 0; newX < Globals.X; newX++) {
                                if (sampleWordProb < wordP[newZ][newL][newX]) {
                                    breakFlg = true;
                                    break;
                                }
                            }
                            if (breakFlg) {
                                break;
                            }
                        }
                        if (breakFlg) {
                            break;
                        }
                    }
                    if (newZ == Globals.K) {
                        newZ--;
                        newL--;
                        newX--;
                    }
                    if (w.x != 0 && wNext.id != w.id) {
                        nWordJInX[wNext.id][wNext.x]++;
                        nWordJInXSum[wNext.id]++;
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
        double sumFriendsInfluence = 0.0;
        for (int u = 0; u < Globals.U; u++) {
            for (int r = 0; r < Globals.R; r++) {
                for (int v = 0; v < Globals.V; v++) {
                    User curUser = Globals.users[u];
                    ArrayList<User> friends = curUser.friends;
                    int nFriends = friends.size();
                    sumFriendsInfluence = 0.0;
                    for (User f : friends) {
                        /* assume User.trustValueU0U1.get(u).get(f.id) == 1 if there is a relationship*/
                        sumFriendsInfluence += 1.0 * f.thetaRatingViewpoint[r][v] / nFriends;
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
        int zeros = 0;
        for (User u : Globals.users) {
            for (int r = 0; r < Globals.R; r++) {
                double numerator = 0.0;
                double denominator = 0.0;
                for (Viewpoint v : Globals.viewpoints) {
                    numerator += Utils.digamma(v.nRatingViewpointsForRating[u.id][r] + u.thetaRatingViewpoint[r][v.id]) - Utils.digamma(u.thetaRatingViewpoint[r][v.id]);
                    denominator += Utils.digamma(v.nRatingViewpointsForRatingSum[u.id] + u.nAllItemRatings * u.thetaRatingViewpoint[r][v.id]) - Utils.digamma(u.nAllItemRatings * u.thetaRatingViewpoint[r][v.id]);
                }
                double tmp = (numerator) / (denominator);
                if (tmp == 0) {
                    zeros++;
                    continue;
                }
                for (int v = 0; v < Globals.V; v++) {
                    u.theta0RatingViewpoint[r][v] = u.theta0RatingViewpoint[r][v] * tmp;
                }
            }
        }
        System.out.println(zeros + " updates on base distribution skipped.");
    }

    public int predict(int uid, int iid, boolean show) {
        User user = Globals.users[uid];
        double p[] = new double[Globals.R];
        for (int r = 0; r < Globals.R; r++) {
            for (int v = 0; v < Globals.V; v++) {
                p[r] += userThetaRatingViewpoint[uid][r][v] * pi[iid][v];
            }
        }
        int maxidx = 2;
        Double maxp = p[0];
        for (int i = 0; i < p.length; i++) {
            if (show) {
                System.out.println("Prob@" + i + ": " + p[i]);
            }
            if (p[i] > maxp) {
                maxp = p[i];
                maxidx = i;
            }
        }
        return maxidx;
    }

    public void recommendForUser(int uid) {
        PriorityQueue<Pair<Integer, Integer>> heap = new PriorityQueue<>(new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                if (o1.getValue() > o2.getValue()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        for (int iid = 0; iid < Globals.I; iid++) {
            int rating = predict(uid, iid, false);
            heap.add(new Pair<>(iid, rating));
        }
        System.out.println("For user " + uid);
        for (int i = 0; i < 5; i++) {
            Pair<Integer, Integer> tmp = heap.poll();
            System.out.println("Item " + tmp.getKey() + " , predicting rating: " + tmp.getValue());
            // from rating get viewpoint
            int vp = 0;
            double vpval = 0.0;
            double[] vpprobs = Globals.users[uid].thetaRatingViewpoint[tmp.getValue()];
            for (int vpidx = 0; vpidx < vpprobs.length; vpidx++) {
                if (vpprobs[vpidx] > vpval) {
                    vpval = vpprobs[vpidx];
                    vp = vpidx;
                }
            }
            // from viewpoint get concept
            int conceptId = 0;
            int conceptCnt = 0;
            int[] ccnts = Globals.viewpoints[vp].nConceptInViewpoint;
            for (int cidx = 0; cidx < ccnts.length; cidx++) {
                if (ccnts[cidx] > conceptCnt) {
                    conceptCnt = ccnts[cidx];
                    conceptId = cidx;
                }
            }
            System.out.println("Reason: ");
            Concept concept = Globals.concepts[conceptId];
            System.out.println("Concept: " + concept.concept);
            // from concept get topic
            Topic topic = conceptTopicHashMap.get(concept);
            System.out.print("Topic #" + topic.id);
            // from topic get words
            System.out.println(" : " + Arrays.toString(topicWordsHashMap.get(topic)));
        }
    }

    public void evaluate() {
        double RMSE = 0.0;
        double MAE = 0.0;
        int totalItem = 0;
        for (User user : Globals.users) {
            for (Review review : user.reviews) {
                RMSE += Math.pow((review.rating - predict(user.id, review.item.id, false)), 2);
                MAE += Math.abs(review.rating - predict(user.id, review.item.id, false));
                totalItem++;
            }
        }
        RMSE = Math.sqrt(RMSE / totalItem);
        MAE = Math.sqrt(MAE / totalItem);
        System.out.println("RMSE: " + RMSE);
        System.out.println("MAE: " + MAE);
    }

    public void collectStats() {
        rnd = new Random(0);
        conceptTopicHashMap = new HashMap<>();
        topicWordsHashMap = new HashMap<>();
        ArrayList<HashMap<String, Integer>> topicVSword = new ArrayList<>();
        int[][] conceptVStopic = new int[Globals.E][Globals.K];
        for (int i = 0; i < Globals.K; i++) {
            topicVSword.add(new HashMap<String, Integer>());
        }
        for (User user : Globals.users) {
            for (Review review : user.reviews) {
                for (Word word : review.words){
                    int tid = word.topic.id;
                    if (topicVSword.get(tid).containsKey(word.word)) {
                        int n = topicVSword.get(tid).get(word.word);
                        topicVSword.get(tid).put(word.word, ++n);
                    } else {
                        topicVSword.get(tid).put(word.word, 1);
                    }
                    conceptVStopic[review.concept.id][tid]++;
                }
            }
        }
        for (int i = 0; i < Globals.K; i++) {
            int maxloop = 20;
            System.out.print("Topic #" + i + ": ");
            for (String w: topicVSword.get(i).keySet()) {
                System.out.print(w + ", ");
                if (maxloop < 0) {
                    break;
                } else {
                    maxloop--;
                }
            }
            System.out.println();
        }
        for (int i = 0 ; i < Globals.E; i++) {
            System.out.print(Globals.concepts[i].concept + ": ");
            int maxidx = 0;
            int maxval = Integer.MIN_VALUE;
            for (int j = 0; j < conceptVStopic[i].length; j++) {
                if (conceptVStopic[i][j] > maxval) {
                    maxidx = j;
                    maxval = conceptVStopic[i][j];
                }
            }
            conceptTopicHashMap.put(Globals.concepts[i], Globals.topics[maxidx]);
            String[] words = new String[10];
            int maxloop = 10;
            Set<String> wordSet = topicVSword.get(maxidx).keySet();
            String[] wordAll = wordSet.toArray(new String[wordSet.size()]);
            for (int widx = 0; widx < maxloop; widx++) {
                words[widx] = wordAll[rnd.nextInt(wordAll.length)];
            }
            topicWordsHashMap.put(Globals.topics[maxidx], words);
            for (String w : topicVSword.get(maxidx).keySet()) {
                System.out.print(w + ", ");
                if (maxloop < 0) {
                    break;
                } else {
                    maxloop--;
                }
            }
            System.out.println();
        }
    }

    public void saveModel() {
        collectStats();
        try {
            Properties properties = new Properties();
            FileInputStream in = new FileInputStream("config.txt");
            properties.load(in);
            in.close();
            FileOutputStream fos = new FileOutputStream(properties.getProperty("USER_DUMP"));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            userThetaRatingViewpoint = new double[Globals.U][Globals.R][Globals.V];
            for (User user : Globals.users) {
                userThetaRatingViewpoint[user.id] = user.thetaRatingViewpoint;
            }
            oos.writeObject(userThetaRatingViewpoint);
            oos.close();
            fos.close();

            fos = new FileOutputStream(properties.getProperty("PI_DUMP"));
            oos = new ObjectOutputStream(fos);
            oos.writeObject(pi);
            oos.close();
            fos.close();

            fos = new FileOutputStream(properties.getProperty("CONCEPT_TOPIC_HM"));
            oos = new ObjectOutputStream(fos);
            oos.writeObject(conceptTopicHashMap);
            oos.close();
            fos.close();

            fos = new FileOutputStream(properties.getProperty("TOPIC_WORDS_HM"));
            oos = new ObjectOutputStream(fos);
            oos.writeObject(topicWordsHashMap);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readModel() {
        try {
            Properties properties = new Properties();
            FileInputStream in = new FileInputStream("config.txt");
            properties.load(in);
            in.close();
            FileInputStream fis = new FileInputStream(properties.getProperty("USER_DUMP"));
            ObjectInputStream ois = new ObjectInputStream(fis);
            userThetaRatingViewpoint = (double[][][]) ois.readObject();
            fis.close();
            ois.close();

            fis = new FileInputStream(properties.getProperty("PI_DUMP"));
            ois = new ObjectInputStream(fis);
            pi = (double[][]) ois.readObject();
            ois.close();
            fis.close();

            fis = new FileInputStream(properties.getProperty("CONCEPT_TOPIC_HM"));
            ois = new ObjectInputStream(fis);
            conceptTopicHashMap = (HashMap<Concept, Topic>) ois.readObject();
            ois.close();
            fis.close();

            fis = new FileInputStream(properties.getProperty("TOPIC_WORDS_HM"));
            ois = new ObjectInputStream(fis);
            topicWordsHashMap = (HashMap<Topic, String[]>) ois.readObject();
            ois.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        collectStats();
    }

}
