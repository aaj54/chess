package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserSer {

    //generate constructor
    private final MemoryUserDAO userDAO;
    private final MemoryAuthDAO authDAO;

    public UserSer(MemoryUserDAO userDAO, MemoryAuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegResult register(RegRequest request) throws DataAccessException {

        UserData user = new UserData(request.username(), request.password(), request.email());
        userDAO.createUser(user);

        String token = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, request.username()));

        return new RegResult(request.username(), token);
    }

    public LoginResult login(LoginUser request) throws DataAccessException {
        UserData user = userDAO.getUser(request.username());
        if (user == null || !user.password().equals(request.password())) {
            throw new DataAccessException("unauthorized");
        }
        String token = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, request.username()));
        return new LoginResult(request.username(), token);
    }
}
