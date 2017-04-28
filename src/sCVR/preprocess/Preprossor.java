package sCVR.preprocess;

import sCVR.preprocess.bean.YelpBusiness;
import sCVR.preprocess.bean.YelpReview;
import sCVR.preprocess.bean.YelpUser;
import sCVR.preprocess.extractor.BusinessExtractor;
import sCVR.preprocess.extractor.ReviewExtractor;
import sCVR.preprocess.extractor.UserExtractor;
import sCVR.types.Globals;
import sCVR.types.Item;
import sCVR.types.Review;
import sCVR.types.User;

import java.io.IOException;
import java.util.*;
import sCVR.types.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RexZhang on 4/28/17.
 */
public class Preprossor {
    private static String yelpBusinessFile = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_business.json";
    private static String yelpUserFile = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_user.json";
    private static String yelpReviewFile = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_review.json";

    private static String temporaryFile = "businessTemp.json";

    private List<YelpReview> yelpReviews;
    private List<YelpUser> yelpUsers;
    private List<YelpBusiness>yelpBusinesses;

    private List<String> yelpCategories;

    public void preprocess(String city) throws IOException {
//        CertainCityExtractor.genCityFile(yelpBusinessFile,temporaryFile,city);
        Set<String> businessIds = new HashSet<String>();
        Set<String> categories = new HashSet<String>();
        yelpBusinesses = BusinessExtractor.getBusinesses(yelpBusinessFile, city, businessIds, categories);
        Set<String> userIds = new HashSet<String>();
        yelpReviews = ReviewExtractor.getReviews(yelpReviewFile, userIds, businessIds);
        yelpUsers = UserExtractor.getUsers(yelpUserFile, userIds);
        yelpCategories = new ArrayList(categories);
    }

    private void setUpGlobals() {
        Globals.U = yelpUsers.size();
        Globals.I = yelpBusinesses.size();
        Globals.RV = yelpReviews.size();
        Globals.K = 30;
        Globals.V = 30;
        Globals.R = 6;
        Globals.L = 3;
        Globals.X = 3;
//        Globals.N = ??;
//        Globals.E = ??;
    }

    private void doLink() {
        ArrayList<Review> reviews = new ArrayList<Review>();
        ArrayList<Item> items = new ArrayList<Item>();
        ArrayList<User> users = new ArrayList<User>();
        HashMap<String, Integer> wordHm = new HashMap<String, Integer>();
        HashMap<String, Integer> reviewHm = new HashMap<String, Integer>();
        HashMap<String, Integer> itemHm = new HashMap<String, Integer>();
        HashMap<String, Integer> userHm = new HashMap<String, Integer>();

        // add to review arrayList
        for (YelpReview yelpReview : yelpReviews) {
            Review review = new Review();
            review.id = reviews.size();
            review.hashId = yelpReview.getReview_id();
            for (String wd : yelpReview.getTextList()) {
                if (!wordHm.containsKey(wd)) {
                    wordHm.put(wd, wordHm.size());
                }
                Word word = new Word();
                word.id = wordHm.get(wd);
                word.word = wd;
                review.words.add(word);
            }
            reviewHm.put(review.hashId, review.id);
            reviews.add(review);
        }

        // add to item arrayList
        for (YelpBusiness yelpBusiness : yelpBusinesses) {
            Item item = new Item();
            item.id = items.size();
            item.hashId = yelpBusiness.getBusiness_id();
            itemHm.put(item.hashId, item.id);
            items.add(item);
        }

        //add to user arrayList
        for (YelpUser yelpUser : yelpUsers) {
            User user = new User();
            user.id = users.size();
            user.hashId = yelpUser.getUser_id();
            userHm.put(user.hashId, user.id);
            users.add(user);
        }

        // link to reviews
        for (YelpReview yelpReview : yelpReviews) {
            Review review = reviews.get(reviewHm.get(yelpReview.getReview_id()));
            review.user = users.get(userHm.get(yelpReview.getUser_id()));
            review.user.reviews.add(review);
            review.item = items.get(itemHm.get(yelpReview.getBusiness_id()));
            review.user.items.add(review.item);
            review.item.reviews.add(review);
        }

        // link friends
        for (YelpUser yelpUser : yelpUsers) {
            User user = users.get(userHm.get(yelpUser.getUser_id()));
            for (String friendHs : yelpUser.getFriends()) {
                user.friends.add(users.get(userHm.get(friendHs)));
            }
        }

        // set to Globals
        Globals.reviews = reviews.toArray(new Review[reviews.size()]);
        Globals.items = items.toArray(new Item[items.size()]);
        Globals.users = users.toArray(new User[users.size()]);
    }

}
