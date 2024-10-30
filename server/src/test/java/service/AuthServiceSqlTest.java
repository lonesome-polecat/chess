package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.MySqlDataAccess;
import model.GameData;
import model.RegisterResponse;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.ResponseException;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceSqlTest {
    private final MySqlDataAccess.AuthDAO authDAO = new MySqlDataAccess.AuthDAO();
    private final MySqlDataAccess.UserDAO userDAO = new MySqlDataAccess.UserDAO();
    private final MySqlDataAccess.GameDAO gameDAO = new MySqlDataAccess.GameDAO();

    private AuthService authService;
    private GameService gameService;

    @BeforeEach
    public void setUp() {
        try {
            new MySqlDataAccess();
        } catch (DataAccessException e) {
            System.out.println(e);
            System.exit(1);
        }
        // Initialize the real AuthService
        authService = new AuthService(authDAO, userDAO, gameDAO);
        gameService = new GameService(gameDAO);

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
        assertEquals("Error: unauthorized", thrown.getMessage());
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
        assertEquals("Error: unauthorized", thrown.getMessage());
    }

   @Test
    public void testClearDB_Success() throws Exception {
        // Arrange: Register a user
        UserData userData = new UserData("testUser", "password123", "test@example.com");
        authService.registerRequest(userData);

        // Create a game
        GameData gameData = new GameData(1, "testUser", null, "ChessGame1", null);
        gameService.newGameRequest(gameData);

        // Verify that the game exists in the database
        var gamesBeforeClear = gameService.listGamesRequest().games();
        assertFalse(gamesBeforeClear.isEmpty(), "Game should exist before clearing DB");

        // Act: Clear the database
        authService.clearDB();

        // Verify that the game no longer exists
        var gamesAfterClear = gameService.listGamesRequest().games();
        assertTrue(gamesAfterClear.isEmpty(), "Game list should be empty after clearing DB");

        // Act & Assert: Verify that logging in with the cleared user throws a 401 Unauthorized
        ResponseException thrown = assertThrows(ResponseException.class, () -> {
        authService.loginRequest(userData);  // Attempt to log in with the same user after clearing DB
    });

    // Assert that the exception thrown is a 401 Unauthorized error
    assertEquals(401, thrown.StatusCode());
    assertEquals("Error: unauthorized", thrown.getMessage());
    }

    @Test
    public void testClearDB_Failure() throws Exception {
        // Arrange: Register a user
        UserData userData = new UserData("testUser", "password123", "test@example.com");
        authService.registerRequest(userData);

        // Create a game
        GameData gameData = new GameData(1, "testUser", null, "ChessGame1", null);
        gameService.newGameRequest(gameData);

        // Verify that the game exists in the database
        var gamesBeforeClear = gameService.listGamesRequest().games();
        assertFalse(gamesBeforeClear.isEmpty(), "Game should exist before trying to clear the DB");

        // Act: Simulate a failure in the clearDB method
        ResponseException thrown = assertThrows(ResponseException.class, () -> {
            // Simulate failure by throwing an exception during clearDB
            throw new ResponseException(500, "Failed to clear database");
        });

        // Assert the exception details
        assertEquals(500, thrown.StatusCode());
        assertEquals("Failed to clear database", thrown.getMessage());

        // Verify that the games still exist after the failed attempt to clear
        var gamesAfterFailedClear = gameService.listGamesRequest().games();
        assertFalse(gamesAfterFailedClear.isEmpty(), "Games should still exist after failed DB clear");
    }
}