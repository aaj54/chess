package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.*;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTest {

    private DataAccess dataAccess;

    @BeforeEach
    void setup() throws DataAccessException{
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();
    }

    //test clear
    @Test
    void clearSuccess() throws DataAccessException {
        dataAccess.createUser(new UserData("user", "pass", "e@mail.com"));
        dataAccess.clear();
        assertDoesNotThrow(() -> dataAccess.createUser(new UserData("user", "pass", "e@mail.com")));
    }

    //test new user success
    @Test
    void createUserSuccess() throws DataAccessException
    {
        dataAccess.createUser(new UserData("user", "pass", "e@mail.com"));
        assertNotNull(dataAccess.getUser("user"));
    }

    //test register username duplicate
    @Test
    void userDuplicate() throws DataAccessException
    {
        dataAccess.createUser(new UserData("user", "pass", "e@mail.com"));
        assertThrows(DataAccessException.class, () ->
                dataAccess.createUser(new UserData("user", "pass", "e@mail.com")));
    }

    //test get user
    @Test
    void getUser() throws DataAccessException
    {
        dataAccess.createUser(new UserData("user", "pass", "e@mail.com"));
        assertNotNull(dataAccess.getUser("user"));
    }

    //make sure no user is grabbed is there are none
    @Test
    void getUserNotFound() throws DataAccessException {
        assertNull(dataAccess.getUser("nonexistent"));
    }

    //test login success
    @Test
    void creatAuthSuccess() throws DataAccessException {
        dataAccess.createAuth(new AuthData("user", "pass"));
        assertNotNull(dataAccess.getAuth("user"));
    }

    //test login fail
    @Test
    void createAuthDuplicate() throws DataAccessException {
        dataAccess.createAuth(new AuthData("user", "wrongpass"));
        assertThrows(DataAccessException.class, () ->
                dataAccess.createAuth(new AuthData("user", "wrongpass")));
    }

    //test get auth
    @Test
    void getAuth() throws DataAccessException
    {
        dataAccess.createAuth(new AuthData("user", "pass"));
        assertNotNull(dataAccess.getAuth("user"));
    }

    //make sure no auth is grabbed is there are none
    @Test
    void getAuthNotFound() throws DataAccessException {
        assertNull(dataAccess.getAuth("nonexistent"));
    }

    // delete the auth
    @Test
    void deleteAuthSuccess() throws DataAccessException {
        dataAccess.createAuth(new AuthData("user", "pass"));
        dataAccess.deleteAuth("user");
        assertNull(dataAccess.getAuth("user"));
    }

    //make sure no auth if deleted make sure runs okay with no errors
    @Test
    void  deleteAuthNotFound() throws DataAccessException
    {
        assertDoesNotThrow(() -> dataAccess.deleteAuth("badtoken"));
    }

    //test createGame success
    @Test
    void creGameSuccess() throws DataAccessException {
        int id = dataAccess.createGame("game");
        assertTrue(id > 0);
    }

    //test create Game fail
    @Test
    void creGameFail() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> dataAccess.createGame(null));
    }

    //test get game
    @Test
    void getGame() throws DataAccessException
    {
        int gameID = dataAccess.createGame("game");
        assertNotNull(dataAccess.getGame(gameID));
    }

    //make sure no game is grabbed is there are none
    @Test
    void getGameNotFound() throws DataAccessException {
        assertNull(dataAccess.getGame(1));
    }

    //test list game positive test
    @Test
    void listGamesSuccess() throws DataAccessException {
        dataAccess.createGame("game1");
        dataAccess.createGame("game2");
        assertFalse(dataAccess.listGames().isEmpty());
    }

    //test list game fail
    @Test
    void listGamesFail() throws DataAccessException{
        assertTrue(dataAccess.listGames().isEmpty());
    }


    @Test
    void updateGameSuccess() throws DataAccessException {
        int id = dataAccess.createGame("myGame");
        GameData game = dataAccess.getGame(id);
        dataAccess.updateGame(new GameData(id, "white", null, "myGame", game.game()));
        assertEquals("white", dataAccess.getGame(id).whiteUsername());
    }

    @Test
    void updateFail() throws DataAccessException {
        assertThrows(DataAccessException.class, () ->
                dataAccess.updateGame(new GameData(9999, null, null,
                        "myGame", new chess.ChessGame())));
    }
}
