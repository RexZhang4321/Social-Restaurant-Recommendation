package sCVR.preprocess.bean;

/**
 * Created by Dylan on 4/27/17.
 */
public class CatSco {
    String category;
    double score;

    public CatSco(String category, double score) {
        this.category = category;
        this.score = score;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

}
