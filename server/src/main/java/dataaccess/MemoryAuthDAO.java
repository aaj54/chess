package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class MemoryAuthDAO {

    private final HashMap<String, AuthData> auths = new HashMap<>();

    public void clear() {
        auths.clear(); //clear auth
    }
}