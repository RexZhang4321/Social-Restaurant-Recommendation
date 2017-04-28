package sCVR.preprocess.extractor;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.json.JSONArray;
import sCVR.preprocess.bean.YelpReview;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import sCVR.preprocess.nlp.SentimentCal;
import sCVR.preprocess.word2vec.CalW2V;
import sCVR.preprocess.word2vec.GenW2V;

public class ReviewExtractor {
	/*
	 * General YelpReview Extractor
	 * @fileName any yelp review file
	 * @return review list
	 */
	public static List<YelpReview> getReviews(String fileName, Set<String> userIds, Set<String> businessIds, Set<String> categories, String tempFile) throws IOException, JSONException {
		InputStream fis = new FileInputStream(fileName);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        
        List<YelpReview> reList = new ArrayList<YelpReview>();

        FileWriter fw = new FileWriter(tempFile);
        BufferedWriter bw = new BufferedWriter(fw);

        while ((line = br.readLine()) != null) {
            JSONObject review = new JSONObject(line);

            if(businessIds.contains((String) review.get("business_id"))) {
                YelpReview r = new YelpReview();
                String userid = (String) review.get("user_id");
                r.setReview_id((String) review.get("review_id"));
                r.setUser_id(userid);
                r.setBusiness_id((String) review.get("business_id"));
                r.setText((String) review.get("text"));
                System.out.println("userid :: " + userid + " wordScores done");
                r.setSentiment(SentimentCal.findSentiment(r.getText()));
                String result = CalW2V.getBestScore(r.getTextList(),categories);
                System.out.println("userid :: " + userid + " concept :: " + r.getConcept());
                r.setConcept(result);
                userIds.add(userid);
                reList.add(r);

                //Write Json

                JSONObject obj = new JSONObject();
                obj.put("review_id", r.getReview_id());
                obj.put("user_id", r.getUser_id());
                obj.put("business_id", r.getBusiness_id());

                JSONArray textJson = new JSONArray();
                for(String c : r.getTextList()){
                    textJson.put(c);
                }
                obj.put("textList", textJson);

                obj.put("concept", r.getConcept());
                obj.put("sentiment", r.getSentiment());

                JSONArray wordScores = new JSONArray();
                for(String[] c : r.getWordScores()){
                    wordScores.put(c);
                }
                obj.put("wordscore", wordScores);
                bw.write(obj.toString());
                bw.write("\r\n");
            }
        }

        bw.close();
        fw.close();
        br.close();
        return reList;
	}
	
	public static void main(String[] args) throws IOException, JSONException {
        SentimentCal.init();
        String w2vFile = "/Users/Dylan/Downloads/glove.6B/glove.6B.300d.txt";
        GenW2V.generate(w2vFile);
		String reviewFile = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_review.json";
		Set<String> score = new HashSet<String>();
		score.add("Food");
		score.add("China");
		List<YelpReview> reList = getReviews(reviewFile, new HashSet<String>(), new HashSet<String>(), score, "temp.json");

	}
}
