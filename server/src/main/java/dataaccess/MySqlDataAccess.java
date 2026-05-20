package dataaccess;

import com.google.gson.Gson;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;


public class MySqlDataAccess implements DataAccess {

    public MySqlDataAccess() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException
    {
        for (String table : new String[]{"auth", "game", "user"}) {
            executeUpdate("TRUNCATE " + table);
        }
    }

    //create user
    @Override
    public void createUser(UserData user) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        executeUpdate("INSERT INTO user (username, password, email) VALUES (?, ?, ?)",
                user.username(), hashedPassword, user.email());
    }
    //getAuth like getPet
    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM user WHERE username=?";
            try (PreparedStatement ps = conn.prepareStatement(statement))
            {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to read user: " + e.getMessage());
        }
        return null;
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        String json = new Gson().toJson(new chess.ChessGame());
        return executeUpdate("INSERT INTO game (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)",
                null, null, gameName, json);
    }

    //getGame like getPet
    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auth WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement))
            {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to read game: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String json = new Gson().toJson(game.game());
        executeUpdate("UPDATE game SET whiteUsername=?, blackUsername=?, gameName=?, game=? WHERE gameID=?",
                game.whiteUsername(), game.blackUsername(), game.gameName(), json, game.gameID());
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        executeUpdate("INSERT INTO auth (authToken, username) VALUES (?, ?)",
                auth.authToken(), auth.username());
    }

    //getAuth like getPet
    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auth WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement))
            {
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(rs.getString("authToken"),
                                rs.getString("username"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to read auth: " + e.getMessage());
        }
        return null;
    }

    public Collection<GameData> listGames() throws DataAccessException {
        var result = new ArrayList<GameData>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGame(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to list games: " + e.getMessage());
        }
        return result;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM auth WHERE authToken=?";
        executeUpdate(statement, authToken);
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        var gameID = rs.getInt("gameID");
        var white = rs.getString("whiteUsername");
        var black = rs.getString("blackUsername");
        var json = rs.getString("game");
        var gameName = rs.getString("gameName");
        chess.ChessGame game = new Gson().fromJson(json, chess.ChessGame.class);
        return new GameData(gameID, white, black, gameName, game);
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int ii = 0; ii < params.length; ii++) {
                    Object param = params[ii];
                    if (param instanceof String p) ps.setString(ii + 1, p);
                    else if (param instanceof Integer p) ps.setInt(ii + 1, p);
                    else if (param == null) ps.setNull(ii + 1, NULL);
                }
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update database: " + e.getMessage());
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  user (
              username VARCHAR(256) NOT NULL,
              password VARCHAR(256) NOT NULL,
              email VARCHAR(256) NOT NULL,
              PRIMARY KEY (username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
             CREATE TABLE IF NOT EXISTS  auth (
              authToken VARCHAR(256) NOT NULL,
              username VARCHAR(256) NOT NULL,
              PRIMARY KEY (authToken)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS  game (
              gameID INT NOT NULL AUTO_INCREMENT,
              whiteUsername VARCHAR(256),
              blackUsername VARCHAR(256),
              gameName VARCHAR(256) NOT NULL,
              game TEXT NOT NULL,
              PRIMARY KEY (gameID)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };


    //configureDatabase from petShop
    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to configure database: " + ex.getMessage());
        }
    }
}