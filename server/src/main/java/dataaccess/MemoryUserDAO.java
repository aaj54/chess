package dataaccess;

import model.UserInfo;

import java.util.HashMap;

public class MemoryUserDAO {

    private final HashMap<String, UserInfo> users = new HashMap<>();

    public void clear() {
        users.clear();  //clear user
    }
}