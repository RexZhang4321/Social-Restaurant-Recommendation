package sCVR.types;

import java.util.ArrayList;

/**
 * Created by RexZhang on 4/10/17.
 */
public class Item {
    // for an item i
    public int id;
    public ArrayList<Review> reviews;    // reviews from user u

    /* -------------- Gibbs Sampling Variables Start ------------ */

    // n_{f, -(u, i)}^{i ,y} || n_{f}^{i ,v}
    // in item i, number of times variable f (rating viewpoint has been assigned to y
    // size is as large as all rating viewpoints
    // nRatingViewpointInItem[f]
    public int[] nRatingViewpointInItem; // done

    // n_{f, -(u, i)}^{i}  || n_{f}^{i}
    // in item i, number of rating viewpoints has been assigned
    public int nRatingViewpointInItemSum;  // done

    // n_{v}^{i, y}
    // in item i, number of times review viewpoint v has been assigned (to y)
    // nReviewViewpointInItem[y]
    public int[] nReviewViewpointInItem;  // done

    // n_{v}^{i}
    // number of viewpoints has been assigned to i
    public int nReviewViewpointInItemSum;  // done

    // TODO: I don't think there is any difference between this one and the one above
    // n_{-d}^{i, v}
    // number of times viewpoint v has been assigned to user reviews
    // nReviewViewpointInReviews[v]
    //public int[] nReviewViewpointInReviews;  // done

    // n_{-d}^{i}
    // number of viewpoints(that have been assigned to user reviews)
    //public int nReviewViewpointInReviewsSum;  // done

    /* -------------- Gibbs Sampling Variables End------------ */

    public Item() {
        nRatingViewpointInItem = new int[Globals.V];
        nRatingViewpointInItemSum = 0;
        nReviewViewpointInItem = new int[Globals.V];
        nReviewViewpointInItemSum = 0;
//        nReviewViewpointInReviews = new int[Globals.V];
//        nReviewViewpointInReviewsSum = 0;
    }
}
