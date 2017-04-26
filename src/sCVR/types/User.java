package sCVR.types;

import java.util.ArrayList;

/**
 * Created by RexZhang on 4/10/17.
 */
public class User {

    public int id;
    public ArrayList<User> friends;      // probably assuming trusted value is 1 once there is a friend
    public double[] theta0;     // base distribution over viewpoints
    public double[] theta;      // distribution over viewpoints with the influence of social relations
    public ArrayList<Review> reviews;
    public ArrayList<Item> items;
}
