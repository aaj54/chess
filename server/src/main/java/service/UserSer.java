package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserSer {

    //generate constructor
    private final DataAccess dataAccess;

    public UserSer(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegResult register(RegRequest request) throws DataAccessException {

        if (request == null ||
                request.username() == null || request.username().isBlank() ||
                request.password() == null || request.password().isBlank() ||
                request.email() == null || request.email().isBlank()) {
            throw new DataAccessException("bad request");
        }

        UserData user = new UserData(request.username(), request.password(), request.email());
        dataAccess.createUser(user);

        String token = UUID.randomUUID().toString();
        dataAccess.createAuth(new AuthData(token, request.username()));

        return new RegResult(request.username(), token);
    }

    public LoginResult login(LoginUser request) throws DataAccessException {
        if (request == null || request.username() == null || request.password() == null) {
            throw new DataAccessException("bad request");
        }

        UserData user = dataAccess.getUser(request.username());
        if (user == null || !BCrypt.checkpw(request.password(), user.password())) {
            throw new DataAccessException("unauthorized");
        }

        String token = UUID.randomUUID().toString();
        dataAccess.createAuth(new AuthData(token, request.username()));
        return new LoginResult(request.username(), token);
    }

    public void logout(String authToken) throws DataAccessException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("unauthorized");
        }
        dataAccess.deleteAuth(authToken);
    }
}
