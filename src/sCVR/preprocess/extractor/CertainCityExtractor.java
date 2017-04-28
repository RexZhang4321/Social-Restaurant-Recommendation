package sCVR.preprocess.extractor;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.json.JSONException;
//import org.json.simple.JSONArray;
import org.json.JSONObject;
/*
 * Generate business json file for target city
 * @fromFileName Original yelp business json file
 * @toFileName Filtered business json file
 * @targetCity wanted city
 */
public class CertainCityExtractor {
	public static void genCityFile(String fromFileName, String toFileName, String targetCity) throws IOException, JSONException {
		InputStream fis = new FileInputStream(fromFileName);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;
        
        FileWriter fw = new FileWriter(toFileName,false);
        BufferedWriter bw = new BufferedWriter(fw);
		
        while ((line = br.readLine()) != null) {
            JSONObject review = new JSONObject(line);
            String city = (String)review.get("city");
            if(city.equals(targetCity)){
            	bw.write(line);
            	bw.write("\r\n");
            }
        }
        br.close();
        bw.close();
        fw.close();
	}

	public static void main(String[] args) throws IOException, JSONException {
		String fromFile = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_business.json";
		String toFile = "/Users/Dylan/Documents/workspace/IRProject/src/ir/project/sample/PleasantHills.json";
		String city = "Pleasant Hills";
		genCityFile(fromFile,toFile,city);
	}
}
