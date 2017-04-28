package IR.Project.extractor;

import IR.Project.bean.Business;
import IR.Project.bean.Review;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
//import org.json.simple.JSONArray;
import org.json.JSONObject;

public class CategoriesExtractor {
	/*
	 * Get Categories from business file
	 * @fileName extracted business file with certain city
	 */
	
	public static Set<String> getCategories(String fileName) throws IOException, JSONException {
		InputStream fis = new FileInputStream(fileName);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        
        List<Business> reList = new ArrayList<Business>();
        Set<String> categories = new HashSet<String>();
        while ((line = br.readLine()) != null) {
            JSONObject review = new JSONObject(line);
            Business r = new Business();
            
            r.setBusiness_id((String) review.get("business_id"));
            if (review.has("categories") && !review.isNull("categories")) {
                JSONArray cateArr = (JSONArray) review.get("categories");
                List<String> cateList = new ArrayList<String>();
                for (int i = 0; i < cateArr.length(); i++) {
                    String currCat = cateArr.getString(i);
                    cateList.add(currCat);
                    categories.add(currCat);
                }
                r.setCategories(cateList);
            }

            reList.add(r);
        }
        return categories;
	}

	public static void main(String[] args) throws IOException, JSONException {
		String businessFile = "src/ir/project/sample/category.json";
		Set<String> categories = getCategories(businessFile);
	}
}
