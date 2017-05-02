package sCVR.preprocess.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sCVR.preprocess.Preprossor;
import sCVR.preprocess.bean.YelpBusiness;
import sCVR.preprocess.bean.YelpReview;
import org.json.JSONException;

import sCVR.preprocess.bean.YelpUser;
import sCVR.preprocess.nlp.SentimentCal;
import sCVR.preprocess.word2vec.CalW2V;
import sCVR.preprocess.word2vec.GenW2V;

public class ExtractProcess {
    private static String yelpBusinessFile = "./yelp_academic_dataset_business.json";
    private static String yelpUserFile = "./yelp_academic_dataset_user.json";
    private static String yelpReviewFile = "./yelp_academic_dataset_review.json";
    private static String w2vFile = "./glove.6B.50d.txt";

    private static List<YelpReview> yelpReviews;
    private static List<YelpUser> yelpUsers;
    private static List<YelpBusiness>yelpBusinesses;

    private static String reviewTemp = "reviewTemp2.json";
    private static String userTemp = "userTemp2.json";
    private static String businessTemp = "businessTemp2.json";
    private static String categoryTemp = "categoryTemp2.json";

    private static List<String> yelpCategories;
    public static void main(String[] args) throws IOException, JSONException{
//        String city = "Pleasant Hills";
        String city = "Fairlawn";
        Set<String> businessIds = new HashSet<String>();
        Set<String> categories = new HashSet<String>();
        System.out.println("Start ");
        yelpBusinesses = BusinessExtractor.getBusinesses(yelpBusinessFile, city, businessIds, categories,businessTemp);
        System.out.println("Finish Business ");
        Set<String> userIds = new HashSet<String>();
        SentimentCal.init();
        System.out.println("Finish SentimentCal Init ");
        GenW2V.generate(w2vFile);
        System.out.println("Finish w2vFile ");
        yelpReviews = ReviewExtractor.getReviews(yelpReviewFile, userIds, businessIds, categories,reviewTemp);
        System.out.println("Finish yelpReviews ");
        yelpUsers = UserExtractor.getUsers(yelpUserFile, userIds, userTemp);
        System.out.println("Finish yelpUsers ");
        yelpCategories = new ArrayList(categories);
        Preprossor.categoryJsonParsor(yelpCategories,categoryTemp);
        System.out.println("Finish yelpCategories ");
    }
}
