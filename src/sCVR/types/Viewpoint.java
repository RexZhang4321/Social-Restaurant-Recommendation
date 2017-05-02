package sCVR.types;

/**
 * Created by RexZhang on 4/10/17.
 */
public class Viewpoint implements java.io.Serializable {

    public int id;

    /* -------------- Gibbs Sampling Variables Start ------------ */

    // n_{u, -i}^{r_{u,i}, y}
    // number of times user u rates item i with viewpoint f
    // nRatingViewpointsForRating[u][r]
    public int[][] nRatingViewpointsForRating;  // done

    // n_{u}^{r_{u,i}, y}
    // number of times user u gives rating using viewpoint f
    // nRatingViewpointsForRating[u]
    public int[] nRatingViewpointsForRatingSum; // done

    // n_{v, e}^{-d}
    // number of times concept e has been assigned to viewpoint v
    // nConceptInViewpoint[e]
    public int[] nConceptInViewpoint;

    // n_{v}^{-d}
    // number of concepts that have been assigned to viewpoint v
    public int nConceptInViewpointSum;

    // n_{v, z}^{-d}
    // number of times topic z has been assigned to viewpoint v
    // nTopicInViewpoint[z]
    public int[] nTopicInViewpoint;

    // n_{v}^{-d}
    // number of topics that have been assigned to viewpoint v
    public int nTopicInViewpointSum;

    /* -------------- Gibbs Sampling Variables End------------ */

    public Viewpoint(int _id) {
        id = _id;
        nRatingViewpointsForRating = new int[Globals.U][Globals.R];
        nRatingViewpointsForRatingSum = new int[Globals.U];
        nConceptInViewpoint = new int[Globals.E];
        nConceptInViewpointSum = 0;
        nTopicInViewpoint = new int[Globals.K];
        nTopicInViewpointSum = 0;
    }
}
