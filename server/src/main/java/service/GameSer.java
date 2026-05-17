package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.GameData;


public class GameSer {

    //set up constructor
    private final MemoryGameDAO gameDAO;
    private final MemoryAuthDAO authDAO;

    public GameSer(MemoryGameDAO gameDAO, MemoryAuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest req) throws DataAccessException {
        if (req == null || req.gameName() == null || req.gameName().isBlank()) {
            throw new DataAccessException("bad request");
        }
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("unauthorized");
        }
        int gameID = gameDAO.createGame(req.gameName());
        return new CreateGameResult(gameID);
    }

    public ListGameRes listGames(String authToken) throws DataAccessException {
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("unauthorized");
        }
        return new ListGameRes(gameDAO.listGames());
    }

    public void joinGame(String authToken, JoinGame req) throws DataAccessException {
        if (req == null || req.playerColor() == null) {
            throw new DataAccessException("bad request");
        }
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("unauthorized");
        }
        GameData game = gameDAO.getGame(req.gameID());
        if (game == null) {
            throw new DataAccessException("bad request");
        }
        String username = authDAO.getAuth(authToken).username();
        if (req.playerColor().equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("already taken");
            }
            gameDAO.updateGame(new GameData(game.gameID(), username,
                    game.blackUsername(), game.gameName(), game.game()));
        } else if (req.playerColor().equals("BLACK")) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("already taken");
            }
            gameDAO.updateGame(new GameData(game.gameID(), game.whiteUsername(),
                    username, game.gameName(), game.game()));
        } else {
            throw new DataAccessException("bad request");
        }
    }
}
