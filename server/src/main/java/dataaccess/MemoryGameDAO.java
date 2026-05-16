package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO {

    private final HashMap<Integer, GameData> games = new HashMap<>();
    private int nextId = 1;

    public void clear() {
        games.clear(); //clear game
        nextId = 1;
    }

    //create game and see if already taken
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

    //get user info
    public GameData getGame(int gameID)
    {
        return games.get(gameID);
    }

    //get game info
    public Collection<GameData> listGames() {
        return games.values();
    }
}