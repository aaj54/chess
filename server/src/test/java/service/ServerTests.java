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

    @Test
    void clearSuccess() throws Exception {
        userService.register(new RegRequest("user", "pass", "e@mail.com"));
        clearService.clear();
        assertDoesNotThrow(() -> userService.register(new RegRequest("user", "pass", "e@mail.com")));
    }

    @Test
    void loginSuccess() throws DataAccessException {
        userService.register(new RegRequest("user", "pass", "e@mail.com"));
        LoginResult result = userService.login(new LoginUser("user", "pass"));
        assertNotNull(result.authToken());
        assertEquals("user", result.username());
    }

    @Test
    void loginWrongPassword() throws DataAccessException {
        userService.register(new RegRequest("user", "pass", "e@mail.com"));
        assertThrows(DataAccessException.class, () ->
                userService.login(new LoginUser("user", "wrongpass")));
    }
}
