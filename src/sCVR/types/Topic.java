package sCVR.types;

/**
 * Created by RexZhang on 4/10/17.
 */
public class Topic implements java.io.Serializable {

    public int id;
    public double[] probW;//p(w|z)
    public double[] sstats;//n^w_z

    public Topic(int _id) {
        id = _id;
    }
}
