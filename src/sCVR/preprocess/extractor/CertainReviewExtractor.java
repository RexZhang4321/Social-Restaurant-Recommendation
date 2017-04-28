package sCVR.preprocess.extractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
//import org.json.simple.JSONArray;
import org.json.JSONObject;

/*
 * Generate review json file for certain business id.
 */
public class CertainReviewExtractor {
	
	public static void extractReivew(Set<String> businessId,String fromFileName, String toFileName) throws IOException, JSONException{
		InputStream fis = new FileInputStream(fromFileName);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        
        FileWriter fw = new FileWriter(toFileName, false);
        BufferedWriter bw = new BufferedWriter(fw);
        int i=0;
        Set<String> userId = new HashSet<String>();
        while ((line = br.readLine()) != null) {
            JSONObject review = new JSONObject(line);
            
            if(businessId.contains((String)review.get("business_id"))){
            	bw.write(line);
            	bw.write("\r\n");
            	i++;
            	userId.add((String)review.get("user_id"));
            }
        }
        System.out.println("total reviews " + i);
        System.out.println("total users " + userId.size());
        br.close();
        bw.close();
        fw.close();
	}
	

	public static void main(String[] args) throws IOException, JSONException {
		Set<String> result = BusinessExtractor.getBusinessIds("/Users/Dylan/Documents/workspace/IRProject/src/ir/project/sample/PleasantHills.json");
		extractReivew(result,"/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_review.json", "/Users/Dylan/Documents/workspace/IRProject/src/ir/project/sample/reviewPleasantHills.json");
	}
}
