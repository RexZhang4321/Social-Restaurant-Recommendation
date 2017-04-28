package IR.Project.extractor;

import IR.Project.bean.Business;
import IR.Project.bean.Review;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
//import org.json.simple.JSONArray;
import org.json.JSONObject;

/*
 * Get the business id set from the extracted business file
 * @fromFileName extracted yelp business file with certain city
 * @return the set of business_id
 */
public class BusinessExtractor {
	public static Set<String> getBusinessIds(String fromFileName) throws IOException, JSONException {
		InputStream fis = new FileInputStream(fromFileName);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        
        Set<String> businessID = new HashSet<String>();
        while ((line = br.readLine()) != null) {
            JSONObject review = new JSONObject(line);
            businessID.add((String)review.get("business_id"));
        }
        br.close();
        return businessID;
	}

	public static void main(String[] args) throws IOException, JSONException {
		Set<String> result = getBusinessIds("/Users/Dylan/Documents/workspace/IRProject/src/ir/project/sample/PleasantHills.json");
	}
}
