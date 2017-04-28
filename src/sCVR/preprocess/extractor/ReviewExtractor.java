package sCVR.preprocess.extractor;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import sCVR.preprocess.bean.YelpReview;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import sCVR.preprocess.nlp.SentimentCal;
import sCVR.preprocess.word2vec.CalW2V;

public class ReviewExtractor {
	/*
	 * General YelpReview Extractor
	 * @fileName any yelp review file
	 * @return review list
	 */
	public static List<YelpReview> getReviews(String fileName, Set<String> userIds, Set<String> businessIds, Set<String> categories) throws IOException, JSONException {
		InputStream fis = new FileInputStream(fileName);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        
        List<YelpReview> reList = new ArrayList<YelpReview>();
        
        while ((line = br.readLine()) != null) {
            JSONObject review = new JSONObject(line);

            if(businessIds.contains((String) review.get("business_id"))) {
                YelpReview r = new YelpReview();
                String userid = (String) review.get("user_id");
                r.setReview_id((String) review.get("review_id"));
                r.setUser_id(userid);
                r.setBusiness_id((String) review.get("business_id"));
                r.setText((String) review.get("text"));
                r.setSentiment(SentimentCal.findSentiment(r.getText()));
                String result = CalW2V.getBestScore(r.getTextList(),categories);
                r.setConcept(result);
                userIds.add(userid);
                reList.add(r);
            }
        }
        
        return reList;
	}
	
//	public static void main(String[] args) throws IOException, JSONException {
////		List<YelpReview> reList = getReviews("/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_review.json");
//		String reviewFile = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_review.json";
//		List<YelpReview> reList = getReviews(reviewFile);
//
////		for (YelpReview t : reList) {
////			System.out.println(t.getText());
////		}
//	}
}
