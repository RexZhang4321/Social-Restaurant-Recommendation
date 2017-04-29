package sCVR.preprocess.extractor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sCVR.preprocess.bean.YelpBusiness;
import sCVR.preprocess.bean.YelpReview;
import sCVR.preprocess.bean.YelpUser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Dylan on 4/28/17.
 */
public class UserExtractor {
    /*
	 * General YelpReview Extractor
	 * @fileName any yelp review file
	 * @return review list
	 */
    public static List<YelpUser> getUsers(String fileName, Set<String> userIds, String tempFile) throws IOException, JSONException {
        InputStream fis = new FileInputStream(fileName);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;

        FileWriter fw = new FileWriter(tempFile);
        BufferedWriter bw = new BufferedWriter(fw);

        List<YelpUser> reList = new ArrayList<YelpUser>();

        while ((line = br.readLine()) != null) {
            JSONObject reviewObj = new JSONObject(line);
            String userId = (String) reviewObj.get("user_id");
            if(userIds.contains(userId)){
                YelpUser user = new YelpUser();
                user.setUser_id(userId);
                if (reviewObj.has("friends") && !reviewObj.isNull("friends")) {
                    JSONArray friendArr = (JSONArray) reviewObj.get("friends");
                    List<String> friendList = new ArrayList<String>();
                    for (int i = 0; i < friendArr.length(); i++) {
                        if(!"None".equals(friendArr.getString(i))) {
                            String currFri = friendArr.getString(i);
                            friendList.add(currFri);
                        }
                    }
                    user.setFriends(friendList);
                }
                reList.add(user);

                //Write Json
                JSONObject obj = new JSONObject();
                obj.put("user_id", user.getUser_id());
                JSONArray friendArray = new JSONArray();
                for(String c : user.getFriends()){
                    if(!"None".equals(c)) {
                        friendArray.put(c);
                    }
                }
                obj.put("friends", friendArray);
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
        String yelpBusiness = "/Users/Dylan/Downloads/yelp_dataset_challenge_round9/yelp_academic_dataset_user.json";
        List<YelpUser> result = getUsers(yelpBusiness,new HashSet<String>(),"temp.json");
//		for(YelpBusiness y : result){
//		    System.out.println(y.getBusiness_id());
//        }
    }
}
