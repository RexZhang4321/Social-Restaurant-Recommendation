package sCVR.preprocess.extractor;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import sCVR.preprocess.bean.YelpReview;
import org.json.JSONException;

import sCVR.preprocess.word2vec.CalW2V;
import sCVR.preprocess.word2vec.GenW2V;

public class ExtractProcess {
//	public static void main(String[] args) throws IOException, JSONException{
//		//1st yelp_review -> certain_city_review
//		String yelpBusiness = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_business.json";
//		String certainCityBusinessFile = "/Users/Dylan/Documents/workspace/Project/src/main/java/IR/Project/PleasantHills.json";
//		String targetCity = "Pleasant Hills";
//
//		CertainCityExtractor.genCityFile(yelpBusiness, certainCityBusinessFile, targetCity);
//
//		//2nd certain_city_review -> certain_business_ids
//		Set<String> businessIds = BusinessExtractor.getBusinesses(certainCityBusinessFile);
//		String yelpReview = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_review.json";
//
//		//3rd certain_business_ids -> certainReview
//		String certainReviewFile = "/Users/Dylan/Documents/workspace/Project/src/main/java/IR/Project/reviewPleasantHills.json";
//		CertainReviewExtractor.extractReivew(businessIds, yelpReview, certainReviewFile);
//
//		//4th get categories
////		Set<String> categories = CategoriesExtractor.getCategories(certainCityBusinessFile);
//		Set<String> categories = CategoriesExtractor.getCategories(yelpBusiness);
//
//		System.out.println("category size " + categories.size());
//		//5th get reviews
//		List<YelpReview> reviewList = ReviewExtractor.getReviews(certainReviewFile);
//
//		//6th generate W2V model
//		String w2vFile = "/Users/Dylan/Downloads/glove.6B/glove.6B.300d.txt";
//		GenW2V.generate(w2vFile);
//
//		//7th check concepts
//		for(YelpReview r : reviewList){
////			System.out.println(r.getText());
//			String result = CalW2V.getBestScore(r.getTextList(),categories);
//			if(!result.equals("")){
//				System.out.println(r.getText());
//				System.out.println("Category " + result);
//			}
//
//		}
//	}
}
