package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryUserDAO {

    private final HashMap<String, UserData> users = new HashMap<>();

    //create username and see if already taken
    public void createUser(UserData user) throws DataAccessException
    {
        if (users.containsKey(user.username()))
        {
            throw new DataAccessException("already taken");
        }
        users.put(user.username(), user);
    }

    //get user info
    public UserData getUser(String username)
    {
        return users.get(username);
    }

    public void clear() {
        users.clear();  //clear user
    }
}