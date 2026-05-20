package service;

import dataaccess.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTests {
    private UserSer userService;
    private ClearSer clearService;
    private GameSer gameService;

    @BeforeEach
    void setup() {
        DataAccess dataAccess = new MemoryDataAccess();
        userService = new UserSer(dataAccess);
        clearService = new ClearSer(dataAccess);
        gameService = new GameSer(dataAccess);
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
        assertFalse(result.games().isEmpty());
    }

    //test list game fail
    @Test
    void listGamesFail() {
        assertThrows(DataAccessException.class, () ->
                gameService.listGames("bad-token"));
    }


    @Test
    void joinGameSuccess() throws Exception {
        userService.register(new RegRequest("user", "pass", "e@mail.com"));
        LoginResult login = userService.login(new LoginUser("user", "pass"));
        CreateGameResult game = gameService.createGame(login.authToken(), new CreateGameRequest("myGame"));
        assertDoesNotThrow(() -> gameService.joinGame(login.authToken(),
                new JoinGame("WHITE", game.gameID())));
    }

    @Test
    void joinGameAlreadyTaken() throws Exception {
        userService.register(new RegRequest("user1", "pass", "e@mail.com"));
        userService.register(new RegRequest("user2", "passw", "e2@mail.com"));
        LoginResult login1 = userService.login(new LoginUser("user1", "pass"));
        LoginResult login2 = userService.login(new LoginUser("user2", "passw"));
        CreateGameResult game = gameService.createGame(login1.authToken(), new CreateGameRequest("myGame"));
        gameService.joinGame(login1.authToken(), new JoinGame("WHITE", game.gameID()));
        assertThrows(DataAccessException.class, () ->
                gameService.joinGame(login2.authToken(), new JoinGame("WHITE", game.gameID())));
    }
}
