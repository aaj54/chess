package client;

import model.*;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer()
    {
        server.stop();
    }

    @BeforeEach
    void clearDB() throws Exception {
        facade.clear();
    }

    @Test
    public void clearSuccess() throws Exception
    {
        facade.register("user", "pass", "e@mail.com");
        facade.clear();
        assertThrows(Exception.class, ()->
                facade.login("user", "pass"));
    }

    @Test
    void regSuccess() throws Exception
    {
        AuthData auth = facade.register("user", "pass", "e@mail.com");
        assertNotNull(auth.authToken());
        assertEquals("user", auth.username());
    }

    @Test
    void regFail() throws Exception
    {
        facade.register("user", "pass", "e@mail.com");
        assertThrows(Exception.class, () ->
                facade.register("user", "pass", "e@mail.com"));
    }

    @Test
    void loginSuccess() throws Exception
    {
        facade.register("user", "pass", "e@mail.com");
        AuthData auth = facade.login("user", "pass");
        assertNotNull(auth.authToken());
        assertEquals("user", auth.username());
    }

    @Test
    void loginFail() throws Exception
    {
        facade.register("user", "pass", "e@mail.com");
        assertThrows(Exception.class, ()->
                facade.login("user", "wrongpass"));
    }

    @Test
    void logoutSuccess() throws Exception
    {
        AuthData auth = facade.register("user", "pass", "e@mail.com");
        int gameID = facade.createGame(auth.authToken(), "myGame");
        assertTrue(gameID > 0);
    }

    @Test
    void logoutFail()
    {
        assertThrows(Exception.class, () ->
                facade.logout("badtoken"));
    }

    @Test
    void createGameSuccess() throws Exception
    {
        AuthData auth = facade.register("user", "pass", "e@mail.com");
        int gameID = facade.createGame(auth.authToken(),"myGame");
        assertTrue(gameID>0);
    }

    @Test
    void createGameFail()
    {
        assertThrows(Exception.class, () ->
                facade.createGame("badtoken", "myGame"));
    }

    @Test
    void listGamesSuccess() throws Exception
    {
        AuthData auth = facade.register("user", "pass", "e@mail.com");
        facade.createGame(auth.authToken(), "myGame");
        var games = facade.listGames(auth.authToken());
        assertFalse(games.isEmpty());
    }

    @Test
    void listGamesFail() throws Exception
    {
        assertThrows(Exception.class, ()->
                facade.listGames("badtoken"));
    }

    @Test
    void joinGameSucess() throws Exception
    {
        AuthData auth = facade.register("user", "pass", "e@mail.com");
        int gameID = facade. createGame(auth.authToken(), "myGame");
        assertDoesNotThrow(() ->
                facade.joinGame(auth.authToken(), "WHITE", gameID));
    }

    @Test
    void joinGameFail() {
        assertThrows(Exception.class, () ->
                facade.joinGame("badtoken", "WHITE", 1234));
    }

}
