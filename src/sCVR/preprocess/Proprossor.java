package sCVR.preprocess;

import sCVR.preprocess.bean.YelpBusiness;
import sCVR.preprocess.bean.YelpReview;
import sCVR.preprocess.bean.YelpUser;
import sCVR.types.Globals;
import sCVR.types.Item;
import sCVR.types.Review;
import sCVR.types.User;

import java.util.ArrayList;

/**
 * Created by RexZhang on 4/28/17.
 */
public class Proprossor {

    private ArrayList<YelpReview> yelpReviews;
    private ArrayList<YelpUser> yelpUsers;
    private ArrayList<YelpBusiness>yelpBusinesses;

    public static void preprocess() {

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
