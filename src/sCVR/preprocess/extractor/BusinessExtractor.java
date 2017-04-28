package sCVR.preprocess.extractor;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
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
	public static List<YelpBusiness> getBusinesses(String fromFileName, String city, Set<String> businessIds, Set<String> categories, String tempFile) throws IOException, JSONException {
		InputStream fis = new FileInputStream(fromFileName);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;

        FileWriter fw = new FileWriter(tempFile);
        BufferedWriter bw = new BufferedWriter(fw);

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
                result.add(curr);

                //Write Json
                JSONObject obj = new JSONObject();
                obj.put("business_id", curr.getBusiness_id());
                JSONArray categoryJson = new JSONArray();
                for(String c : curr.getCategories()){
                    categoryJson.put(c);
                }
                obj.put("categories", categoryJson);
                bw.write(obj.toString());
                bw.write("\r\n");
            }
        }
        bw.close();
        fw.close();
        br.close();
        return result;
	}

	public static void main(String[] args) throws IOException, JSONException {
        String yelpBusiness = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_business.json";
		List<YelpBusiness> result = getBusinesses(yelpBusiness,"Pleasant Hills",new HashSet<String>(), new HashSet<String>(), "temp.json");
//		for(YelpBusiness y : result){
//		    System.out.println(y.getBusiness_id());
//        }
	}
}
