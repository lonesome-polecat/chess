package service;

import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    private final MemoryDataAccess.AuthDAO authDAO = new MemoryDataAccess.AuthDAO();
    private final MemoryDataAccess.UserDAO userDAO = new MemoryDataAccess.UserDAO();

    private AuthService authService;

    @BeforeEach
    public void setUp() {
        // Initialize the real AuthService
        authService = new AuthService(authDAO, userDAO);

        // Optionally, clear the database before each test to ensure a known state
        // This assumes clearDB() is a method to clear/reset the database
        MemoryDataAccess.clearDB();
    }

    @Test
    public void testRegisterRequest_Success() throws Exception {
        // Arrange
        UserData userData = new UserData("newUser", "password123", "user@example.com");

        // Act
        RegisterResponse response = authService.registerRequest(userData);

        // Assert
        assertNotNull(response);
        assertEquals("newUser", response.getUsername());
        assertNotNull(response.getAuthToken());
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

        assertEquals(400, thrown.StatusCode());
        assertEquals("This username has already been used. Choose a different one", thrown.getMessage());
    }
}