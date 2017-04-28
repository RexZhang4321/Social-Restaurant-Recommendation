package sCVR.preprocess;

import sCVR.preprocess.bean.YelpBusiness;
import sCVR.preprocess.bean.YelpReview;
import sCVR.preprocess.bean.YelpUser;
import sCVR.preprocess.extractor.BusinessExtractor;
import sCVR.preprocess.extractor.CertainCityExtractor;
import sCVR.preprocess.extractor.ReviewExtractor;
import sCVR.preprocess.extractor.UserExtractor;
import sCVR.types.Globals;
import sCVR.types.Item;
import sCVR.types.Review;
import sCVR.types.User;

import java.io.IOException;
import java.util.*;

/**
 * Created by RexZhang on 4/28/17.
 */
public class Proprossor {
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
    }

    private void doLink() {
        ArrayList<Review> reviews;
        ArrayList<Item> items;
        ArrayList<User> users;

        for (YelpReview yelpReview : yelpReviews) {
            Review review = new Review();
        }
    }

}
