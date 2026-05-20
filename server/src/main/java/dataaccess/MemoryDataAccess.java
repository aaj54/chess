package dataaccess;

import chess.ChessGame;
import model.*;
import java.util.*;

public class MemoryDataAccess implements DataAccess{

    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private final HashMap<String, AuthData> auths = new HashMap<>();

    private int nextId = 1;

    @Override
    public void createUser(UserData user) throws DataAccessException
    {
        if (users.containsKey(user.username()))
        {
            throw new DataAccessException("already taken");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
            return users.get(username);
    }

    @Override
    public void clear() {
        users.clear();
        auths.clear();
        games.clear();
    }

    @Override
    public void createAuth(AuthData auth) {
        auths.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken)
    {
        return auths.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken)
    {
        auths.remove(authToken);
    }

    @Override
    public int createGame(String gameName) throws DataAccessException
    {
        int gameID = nextId++;

        if (games.containsKey(gameID))
        {
            throw new DataAccessException("already taken");
        }

        GameData game = new GameData(gameID,null, null, gameName, new ChessGame());
        games.put(gameID, game);

        return gameID;
    }

    @Override
    public GameData getGame(int gameID)
    {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames()
    {
        return games.values();
    }

    @Override
    public void updateGame(GameData game)
    {
        games.put(game.gameID(), game);
    }
}
