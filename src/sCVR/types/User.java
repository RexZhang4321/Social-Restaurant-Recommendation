package sCVR.types;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RexZhang on 4/10/17.
 */
public class User {

    public int id;
    public String hashId;
    public ArrayList<User> friends;      // probably assuming trusted value is 1 once there is a friend
    public ArrayList<Review> reviews;
    public ArrayList<Item> items;
    public static HashMap<Integer, HashMap<Integer, Integer>> trustValueU0U1;

    /* -------------- Gibbs Sampling Variables Start ------------ */

    // R_{u}
    // number of times user u rates all items
    public int nAllItemRatings; // done

    // base distribution over viewpoints
    // theta0RatingViewpoint[r][v]
    public double[][] theta0RatingViewpoint;

    // distribution over viewpoints with the influence of social relations
    // thetaRatingViewpoint[r][v]
    public double[][] thetaRatingViewpoint;

    /* -------------- Gibbs Sampling Variables End------------ */

    public User() {
        nAllItemRatings = 0;
        theta0RatingViewpoint = new double[Globals.R][Globals.V];
        thetaRatingViewpoint = new double[Globals.R][Globals.V];
    }
}
