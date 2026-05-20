package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;


public class GameSer {

    //set up constructor
    private final DataAccess dataAccess;

    public GameSer(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest req) throws DataAccessException {
        if (req == null || req.gameName() == null || req.gameName().isBlank()) {
            throw new DataAccessException("bad request");
        }
        if (dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("unauthorized");
        }
        int gameID = dataAccess.createGame(req.gameName());
        return new CreateGameResult(gameID);
    }

    public ListGameRes listGames(String authToken) throws DataAccessException {
        if (dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("unauthorized");
        }
        return new ListGameRes(dataAccess.listGames());
    }

    public void joinGame(String authToken, JoinGame req) throws DataAccessException {
        if (req == null || req.playerColor() == null) {
            throw new DataAccessException("bad request");
        }
        if (dataAccess.getAuth(authToken) == null) {
            throw new DataAccessException("unauthorized");
        }
        GameData game = dataAccess.getGame(req.gameID());
        if (game == null) {
            throw new DataAccessException("bad request");
        }
        String username = dataAccess.getAuth(authToken).username();
        if (req.playerColor().equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("already taken");
            }
            dataAccess.updateGame(new GameData(game.gameID(), username,
                    game.blackUsername(), game.gameName(), game.game()));
        } else if (req.playerColor().equals("BLACK")) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("already taken");
            }
            dataAccess.updateGame(new GameData(game.gameID(), game.whiteUsername(),
                    username, game.gameName(), game.game()));
        } else {
            throw new DataAccessException("bad request");
        }
    }
}
