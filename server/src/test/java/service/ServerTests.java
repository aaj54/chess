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

    @BeforeEach
    void setup() {
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        MemoryGameDAO gameDAO = new MemoryGameDAO();
        userService = new UserSer(userDAO, authDAO);
        clearService = new ClearSer(userDAO, authDAO, gameDAO);
    }


    //tets clear
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
}
