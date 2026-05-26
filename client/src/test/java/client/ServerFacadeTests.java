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

    @Test
    public void clear() throws Exception
    {
        facade.clear();
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
        facade.clear();
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
        facade.clear();
        facade.register("user", "pass", "e@mail.com");
        assertThrows(Exception.class, ()->
                facade.login("user", "wrongpass"));
    }

}
