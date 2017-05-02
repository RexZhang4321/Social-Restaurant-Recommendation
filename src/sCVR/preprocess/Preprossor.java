package sCVR.preprocess;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sCVR.preprocess.bean.YelpBusiness;
import sCVR.preprocess.bean.YelpReview;
import sCVR.preprocess.bean.YelpUser;
import sCVR.types.Globals;
import sCVR.types.Item;
import sCVR.types.Review;
import sCVR.types.User;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import sCVR.types.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by RexZhang on 4/28/17.
 */
public class Preprossor {
    private static String baseDir = "/Users/RexZhang/Documents/Dev/Github/Social-Restaurant-Recommendation/src/sCVR";

    private static String yelpBusinessFile = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_business.json";
    private static String yelpUserFile = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_user.json";
    private static String yelpReviewFile = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_review.json";
    private static String w2vFile = "/Users/Dylan/Downloads/glove.6B/glove.6B.300d.txt";

    private static String reviewTemp = baseDir + "/data/reviewTemp_100.json";
    private static String userTemp = baseDir + "/data/userTemp_100.json";
    private static String businessTemp = baseDir + "/data/businessTemp_100.json";
    private static String categoryTemp = baseDir + "/data/categoryTemp_100.json";

    private List<YelpReview> yelpReviews;
    private List<YelpUser> yelpUsers;
    private List<YelpBusiness>yelpBusinesses;

    private List<String> yelpCategories;

    public Preprossor() {
        try {
            Properties properties = new Properties();
            FileInputStream in = new FileInputStream("config.txt");
            properties.load(in);
            in.close();
            if (!properties.get("YELP_BUSINESS_FILE").equals("")) {
                yelpBusinessFile = properties.getProperty("YELP_BUSINESS_FILE");
            }
            if (!properties.get("YELP_USER_FILE").equals("")) {
                yelpUserFile = properties.getProperty("YELP_USER_FILE");
            }
            if (!properties.get("YELP_REVIEW_FILE").equals("")) {
                yelpReviewFile = properties.getProperty("YELP_REVIEW_FILE");
            }
            if (!properties.get("WORD2VEC_FILE").equals("")) {
                w2vFile = properties.getProperty("WORD2VEC_FILE");
            }
            reviewTemp = properties.getProperty("REVIEW_JSON");
            userTemp = properties.getProperty("USER_JSON");
            businessTemp = properties.getProperty("BUSINESS_JSON");
            categoryTemp = properties.getProperty("CATEGORY_JSON");
            Globals.K = Integer.parseInt(properties.getProperty("K"));
            Globals.V = Integer.parseInt(properties.getProperty("V"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void preprocess(String city, boolean fromFile) throws IOException {
//        if (!fromFile) {
//            Set<String> businessIds = new HashSet<String>();
//            Set<String> categories = new HashSet<String>();
//            yelpBusinesses = BusinessExtractor.getBusinesses(yelpBusinessFile, city, businessIds, categories, businessTemp);
//            Set<String> userIds = new HashSet<String>();
//            SentimentCal.init();
//            GenW2V.generate(w2vFile);
//            yelpReviews = ReviewExtractor.getReviews(yelpReviewFile, userIds, businessIds, categories, reviewTemp);
//            yelpUsers = UserExtractor.getUsers(yelpUserFile, userIds, userTemp);
//            yelpCategories = new ArrayList(categories);
//            categoryJsonParsor(yelpCategories, categoryTemp);
//        } else {
//            preprocessFromFile();
//        }
        preprocessFromFile();
        System.out.println("Preprocessing completed");
        setUpGlobals();
        System.out.println("Globals set up");
        doLink();
        System.out.println("Linking finished");
    }

    private void setUpGlobals() {
        Globals.U = yelpUsers.size();
        Globals.I = yelpBusinesses.size();
        Globals.RV = yelpReviews.size();
//        Globals.K = 20;
//        Globals.V = 30;
        Globals.R = 6;
        Globals.L = 2;
        Globals.X = 3;
        Globals.E = yelpCategories.size();
    }

    private void doLink() {
        ArrayList<Review> reviews = new ArrayList<Review>();
        ArrayList<Item> items = new ArrayList<Item>();
        ArrayList<User> users = new ArrayList<User>();
        ArrayList<Concept> concepts = new ArrayList<Concept>();
        HashMap<String, Integer> wordHm = new HashMap<String, Integer>();
        HashMap<String, Integer> reviewHm = new HashMap<String, Integer>();
        HashMap<String, Integer> itemHm = new HashMap<String, Integer>();
        HashMap<String, Integer> userHm = new HashMap<String, Integer>();
        HashMap<String, Integer> conceptHm = new HashMap<String, Integer>();

        // add to concept arrayList
        for (String category : yelpCategories) {
            if (conceptHm.get(category) == null) {
                Concept concept = new Concept();
                concept.concept = category;
                concept.id = concepts.size();
                conceptHm.put(category, concept.id);
                concepts.add(concept);
            }
        }

        Globals.concepts = concepts.toArray(new Concept[concepts.size()]);

        // add to review arrayList
        for (YelpReview yelpReview : yelpReviews) {
            Review review = new Review();
            review.id = reviews.size();
            review.hashId = yelpReview.getReview_id();
            review.rating = yelpReview.getStar();
            //review.concept = concepts.get(conceptHm.get(yelpReview.getConcept()));
            for (String[] wd_score : yelpReview.getWordScores()) {
                // 0 is score / sentiment
                int score = Integer.parseInt(wd_score[0]);
                // 1 is word
                String wd = wd_score[1].toLowerCase();
                if (!wordHm.containsKey(wd)) {
                    wordHm.put(wd, wordHm.size());
                }
                Word word = new Word();
                word.id = wordHm.get(wd);
                word.word = wd;
                word.sentiment = score;
                review.words.add(word);
            }
            reviewHm.put(review.hashId, review.id);
            reviews.add(review);
        }

        Globals.N = wordHm.size();

        // add to item arrayList
        for (YelpBusiness yelpBusiness : yelpBusinesses) {
            Item item = new Item();
            item.id = items.size();
            item.hashId = yelpBusiness.getBusiness_id();
            item.category = conceptHm.get(yelpBusiness.getCategories().get(0));
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
            review.concept = concepts.get(review.item.category);
            review.item.reviews.add(review);
        }

        // link friends
        for (YelpUser yelpUser : yelpUsers) {
            User user = users.get(userHm.get(yelpUser.getUser_id()));
            for (String friendHs : yelpUser.getFriends()) {
                Integer friendId = userHm.get(friendHs);
                if (friendId != null) {
                    user.friends.add(users.get(friendId));
                }
            }
        }

        // set to Globals
        Globals.reviews = reviews.toArray(new Review[reviews.size()]);
        Globals.items = items.toArray(new Item[items.size()]);
        Globals.users = users.toArray(new User[users.size()]);
    }

    public static void categoryJsonParsor(List<String> categories, String tempFile) throws IOException, JSONException {
        FileWriter fw = new FileWriter(categoryTemp);
        BufferedWriter bw = new BufferedWriter(fw);

        JSONArray categoryJson = new JSONArray();
        for (String c : categories) {
            categoryJson.put(c);
        }
        bw.write(categoryJson.toString());
        bw.write("\r\n");
        bw.close();
    }

    void preprocessFromFile() {
        yelpBusinesses = new ArrayList<YelpBusiness>();
        yelpReviews = new ArrayList<YelpReview>();
        yelpUsers = new ArrayList<YelpUser>();
        yelpCategories = new ArrayList<String>();
        try {
            // read business
            InputStream fis = new FileInputStream(businessTemp);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                JSONObject yBusiness = new JSONObject(line);
                YelpBusiness yelpBusiness = new YelpBusiness();
                yelpBusiness.setBusiness_id(yBusiness.getString("business_id"));
                JSONArray tmparr = yBusiness.getJSONArray("categories");
                List<String> tmplist = new ArrayList<String>();
                for (int i = 0; i < tmparr.length(); i++) {
                    tmplist.add(tmparr.getString(i));
                }
                yelpBusiness.setCategories(tmplist);
                yelpBusinesses.add(yelpBusiness);
            }

            // read users
            fis = new FileInputStream(userTemp);
            isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                JSONObject yUser = new JSONObject(line);
                YelpUser yelpUser = new YelpUser();
                yelpUser.setUser_id(yUser.getString("user_id"));
                JSONArray tmparr = yUser.getJSONArray("friends");
                List<String> tmplist = new ArrayList<String>();
                for (int i = 0; i < tmparr.length(); i++) {
                    tmplist.add(tmparr.getString(i));
                }
                yelpUser.setFriends(tmplist);
                yelpUsers.add(yelpUser);
            }

            // read reviews
            fis = new FileInputStream(reviewTemp);
            isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                JSONObject yReview = new JSONObject(line);
                YelpReview yelpReview = new YelpReview();
                yelpReview.setBusiness_id(yReview.getString("business_id"));
                yelpReview.setUser_id(yReview.getString("user_id"));
                yelpReview.setReview_id(yReview.getString("review_id"));
                //yelpReview.setConcept(yReview.getString("concept"));
                yelpReview.setStar(yReview.getInt("rate"));
                JSONArray tmparr = yReview.getJSONArray("wordscore");
                List<String[]> tmplist = new ArrayList<String[]>();
                for (int i = 0; i < tmparr.length(); i++) {
                    JSONArray tmparr2 = (JSONArray) tmparr.get(i);
                    String[] wscores = new String[2];
                    // 0 is score, 1 is word
                    wscores[0] = tmparr2.getString(0);
                    wscores[1] = tmparr2.getString(1);
                    tmplist.add(wscores);
                }
                yelpReview.setWordScores(tmplist);
                yelpReviews.add(yelpReview);
            }

            // read categories
            fis = new FileInputStream(categoryTemp);
            isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                JSONArray tmparr =  new JSONArray(line);
                for (int i = 0; i < tmparr.length(); i++) {
                    yelpCategories.add(tmparr.getString(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
