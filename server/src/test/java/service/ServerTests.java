package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTests {
    // reset DAOs before each test so they don't bleed into each other
    private UserSer userService;

    @BeforeEach
    void setup() {
        MemoryUserDAO userDAO = new MemoryUserDAO();
        MemoryAuthDAO authDAO = new MemoryAuthDAO();
        userService = new UserSer(userDAO, authDAO);
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
