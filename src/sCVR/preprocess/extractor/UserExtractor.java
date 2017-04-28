package sCVR.preprocess.extractor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sCVR.preprocess.bean.YelpReview;
import sCVR.preprocess.bean.YelpUser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
    public static List<YelpUser> getUsers(String fileName, Set<String> userIds) throws IOException, JSONException {
        InputStream fis = new FileInputStream(fileName);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line;

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
                        String currFri = friendArr.getString(i);
                        friendList.add(currFri);
                    }
                    user.setFriends(friendList);
                }

            }
        }

        return reList;
    }
}
