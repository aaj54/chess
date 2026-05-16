package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.MemoryGameDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTests {
    private UserSer userService;
    private ClearSer clearService;
    private GameSer gameService;

    @BeforeEach
    void setup() {
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();
        userService = new UserSer(userDAO, authDAO);
        clearService = new ClearSer(userDAO, authDAO, gameDAO);
        gameService = new GameSer(gameDAO, authDAO);
    }


    //test clear
    @Test
    void clearSuccess() throws Exception {
        userService.register(new RegRequest("user", "pass", "e@mail.com"));
        clearService.clear();
        assertDoesNotThrow(() -> userService.register(new RegRequest("user", "pass", "e@mail.com")));
    }

    //test register success
    @Test
    void regSuccess() throws Exception
    {
        RegResult result = userService.register(new RegRequest("user", "pass", "e@mail.com"));
        assertNotNull(result.authToken());
        assertEquals("user", result.username());
    }

    //test register username duplicate
    @Test
    void regDuplicate() throws Exception
    {
        userService.register(new RegRequest("user", "pass", "e@mail.com"));
        assertThrows(DataAccessException.class, () ->
                userService.register(new RegRequest("user", "pass", "e@mail.com")));
    }

    //test login success
    @Test
    void loginSuccess() throws DataAccessException {
        userService.register(new RegRequest("user", "pass", "e@mail.com"));
        LoginResult result = userService.login(new LoginUser("user", "pass"));
        assertNotNull(result.authToken());
        assertEquals("user", result.username());
    }

    //test login fail
    @Test
    void loginWrongPassword() throws DataAccessException {
        userService.register(new RegRequest("user", "pass", "e@mail.com"));
        assertThrows(DataAccessException.class, () ->
                userService.login(new LoginUser("user", "wrongpass")));
    }

    //successful logout
    @Test
    void logoutSuccess() throws Exception {
        userService.register(new RegRequest("user", "pass", "e@mail.com"));
        LoginResult result = userService.login(new LoginUser("user", "pass"));
        assertDoesNotThrow(() -> userService.logout(result.authToken()));
    }

    //not successful lout
    @Test
    void logoutInvalidToken() {
        assertThrows(DataAccessException.class, () -> userService.logout("invalid-token"));
    }

    //test createGame success
    @Test
    void creGameSuccess() throws DataAccessException {
        userService.register(new RegRequest("user", "pass", "e@mail.com"));
        LoginResult login = userService.login(new LoginUser("user", "pass"));
        CreateGameResult result = gameService.createGame(login.authToken(), new CreateGameRequest("name"));
        assertNotNull(result);
        assertTrue(result.gameID() > 0);
    }

    //test create Game fail
    @Test
    void creGameFail() {
        assertThrows(DataAccessException.class, () -> gameService.createGame("badToken",
                new CreateGameRequest("name")));
    }

    //test list game positive test
    @Test
    void listGamesSuccess() throws Exception {
        userService.register(new RegRequest("user", "pass", "e@mail.com"));
        LoginResult login = userService.login(new LoginUser("user", "pass"));
        gameService.createGame(login.authToken(), new CreateGameRequest("myGame"));
        ListGameRes result = gameService.listGames(login.authToken());
        assertFalse(result.allGames().isEmpty());
    }

    //test list game fail
    @Test
    void listGamesFail() {
        assertThrows(DataAccessException.class, () ->
                gameService.listGames("bad-token"));
    }
}
