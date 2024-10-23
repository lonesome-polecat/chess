package service;

import dataaccess.MemoryDataAccess;
import server.ResponseException;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    private final MemoryDataAccess.AuthDAO authDAO = new MemoryDataAccess.AuthDAO();
    private final MemoryDataAccess.UserDAO userDAO = new MemoryDataAccess.UserDAO();
    private final MemoryDataAccess.GameDAO gameDAO = new MemoryDataAccess.GameDAO();

    private AuthService authService;

    @BeforeEach
    public void setUp() {
        // Initialize the real AuthService
        authService = new AuthService(authDAO, userDAO, gameDAO);

        // Optionally, clear the database before each test to ensure a known state
        // This assumes clearDB() is a method to clear/reset the database
        authService.clearDB();
    }

    @Test
    public void testRegisterRequest_Success() throws Exception {
        // Arrange
        UserData userData = new UserData("newUser", "password123", "user@example.com");

        // Act
        RegisterResponse response = authService.registerRequest(userData);

        // Assert
        assertNotNull(response);
        assertEquals("newUser", response.username);
        assertNotNull(response.authToken);
    }

    @Test
    public void testRegisterRequest_ThrowsException_UserExists() {
        // Arrange
        UserData userData = new UserData("existingUser", "password123", "user@example.com");

        // First, we register the user to make sure they exist in the database
        try {
            authService.registerRequest(userData);
        } catch (ResponseException e) {
            fail("Initial registration should not have failed.");
        }

        // Act & Assert
        ResponseException thrown = assertThrows(ResponseException.class, () -> {
            // Try to register the same user again
            authService.registerRequest(userData);
        });

        assertEquals(403, thrown.StatusCode());
        assertEquals( "Error: already taken", thrown.getMessage());
    }

     @Test
    public void testLoginRequest_Success() throws Exception {
        // Arrange
        UserData userData = new UserData("loginUser", "password123", "login@example.com");

        // Register the user first so they exist in the database
        authService.registerRequest(userData);

        // Act
        RegisterResponse response = authService.loginRequest(userData);

        // Assert
        assertNotNull(response);
        assertEquals("loginUser", response.username);
        assertNotNull(response.authToken);
    }

    @Test
    public void testLoginRequest_ThrowsException_InvalidPassword() throws Exception {
        // Arrange
        UserData validUserData = new UserData("loginUser", "password123", "login@example.com");

        // Register the user with a valid password
        authService.registerRequest(validUserData);

        // Try to log in with an invalid password
        UserData invalidUserData = new UserData("loginUser", "wrongpassword", "login@example.com");

        // Act & Assert
        ResponseException thrown = assertThrows(ResponseException.class, () -> {
            authService.loginRequest(invalidUserData);
        });

        assertEquals(401, thrown.StatusCode());
        assertEquals("Invalid username or password", thrown.getMessage());
    }
     @Test
    public void testLoginRequest_ThrowsException_UnregisteredUser() throws Exception {
        // Arrange
        // Try to log in with an unregistered user
        UserData invalidUserData = new UserData("loginUser", "wrongpassword", "login@example.com");

        // Act & Assert
        ResponseException thrown = assertThrows(ResponseException.class, () -> {
            authService.loginRequest(invalidUserData);
        });

        assertEquals(401, thrown.StatusCode());
        assertEquals("Invalid username or password", thrown.getMessage());
    }
}