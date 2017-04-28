package IR.Project.extractor;

import IR.Project.bean.Review;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class ReviewExtractor {
	/*
	 * General Review Extractor
	 * @fileName any yelp review file
	 * @return review list
	 */
	public static List<Review> getReviews(String fileName) throws IOException, JSONException {
		InputStream fis = new FileInputStream(fileName);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        
        List<Review> reList = new ArrayList<Review>();
        
        while ((line = br.readLine()) != null) {
            JSONObject review = new JSONObject(line);
            Review r = new Review();
            
            r.setReview_id((String) review.get("review_id"));
            r.setUser_id((String) review.get("user_id"));
            r.setBusiness_id((String) review.get("business_id"));
            r.setText((String) review.get("text"));
            reList.add(r);
        }
        
        return reList;
	}
	
	public static void main(String[] args) throws IOException, JSONException {
//		List<Review> reList = getReviews("/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_review.json");
		String reviewFile = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_review.json";
		List<Review> reList = getReviews(reviewFile);

//		for (Review t : reList) {
//			System.out.println(t.getText());
//		}
	}
}
