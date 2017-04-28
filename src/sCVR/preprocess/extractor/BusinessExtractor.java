package sCVR.preprocess.extractor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
//import org.json.simple.JSONArray;
import org.json.JSONObject;
import sCVR.preprocess.bean.YelpBusiness;

/*
 * Get the business id set from the extracted business file
 * @fromFileName extracted yelp business file with certain city
 * @return the set of business_id
 */
public class BusinessExtractor {
	public static List<YelpBusiness> getBusinesses(String fromFileName, String city, Set<String> businessIds, Set<String> categories) throws IOException, JSONException {
		InputStream fis = new FileInputStream(fromFileName);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        
        List<YelpBusiness> result = new ArrayList<YelpBusiness>();
        while ((line = br.readLine()) != null) {
            JSONObject review = new JSONObject(line);
            if(city.equals((String)review.get("city"))){
                YelpBusiness curr = new YelpBusiness();
                String bid = (String)review.get("business_id");
                curr.setBusiness_id(bid);
                businessIds.add(bid);
                if (review.has("categories") && !review.isNull("categories")) {
                    JSONArray cateArr = (JSONArray) review.get("categories");
                    List<String> cateList = new ArrayList<String>();
                    for (int i = 0; i < cateArr.length(); i++) {
                        String currCat = cateArr.getString(i);
                        cateList.add(currCat);
                        categories.add(currCat);
                    }
                    curr.setCategories(cateList);
                }
            }
        }
        br.close();
        return result;
	}

	public static void main(String[] args) throws IOException, JSONException {
//        String yelpBusiness = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_business.json";
//		List<YelpBusiness> result = getBusinesses(yelpBusiness,"Pleasant Hills");
//		for(YelpBusiness y : result){
//		    System.out.println(y.getBusiness_id());
//        }
	}
}
