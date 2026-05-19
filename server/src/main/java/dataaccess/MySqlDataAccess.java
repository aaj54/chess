package dataaccess;

import com.google.gson.Gson;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

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

    public PetList listPets() throws DataAccessException {
        var result = new PetList();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id, json FROM pet";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readPet(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(DataAccessException.Code.ServerError, String.format("Unable to read data: %s", e.getMessage()));
        }
        return result;
    }

    public void deletePet(Integer id) throws DataAccessException {
        var statement = "DELETE FROM pet WHERE id=?";
        executeUpdate(statement, id);
    }

    public void deleteAllPets() throws DataAccessException {
        var statement = "TRUNCATE pet";
        executeUpdate(statement);
    }

    private Pet readPet(ResultSet rs) throws SQLException {
        var id = rs.getInt("id");
        var json = rs.getString("json");
        Pet pet = new Gson().fromJson(json, Pet.class);
        return pet.setId(id);
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