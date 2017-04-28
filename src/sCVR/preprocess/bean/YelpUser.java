package sCVR.preprocess.bean;

import java.util.List;

/**
 * Created by RexZhang on 4/28/17.
 */
public class YelpUser {
    String user_id;
    List<String> friends;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }
}
