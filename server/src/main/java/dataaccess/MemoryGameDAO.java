package dataaccess;

import model.GameData;

import java.util.HashMap;

public class MemoryGameDAO {

    private final HashMap<String, GameData> games = new HashMap<>();

    public void clear() {
        games.clear(); //clear game
    }

    //create game and see if already taken
    public void createGame(GameData game) throws DataAccessException
    {
        if (games.containsKey(game.gameName()))
        {
            throw new DataAccessException("already taken");
        }
        games.put(game.gameName(), game);
    }

    //get user info
    public GameData getGame(String gameName)
    {
        return games.get(gameName);
    }
}