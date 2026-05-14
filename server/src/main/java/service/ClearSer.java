package service;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;

public class ClearSer {

    //generate constructor
    private final MemoryUserDAO userDAO;
    private final MemoryAuthDAO authDAO;
    private final MemoryGameDAO gameDAO;

    public ClearSer(
            MemoryUserDAO userDAO,
            MemoryAuthDAO authDAO,
            MemoryGameDAO gameDAO) {

        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    //clear data function
    public void clear() {

        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }
}