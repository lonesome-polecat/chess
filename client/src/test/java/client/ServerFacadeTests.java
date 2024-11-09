package client;

import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.ResponseException;
import server.Server;
import ui.ServerFacade;


public class ServerFacadeTests {

    private static Server server;
    public static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(1999);
        System.out.println("Started test HTTP server on " + port);

        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        try {
            serverFacade.clearDB();
        } catch (ResponseException e) {
            System.out.println(e);
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void registerUserTestInvalidUsername() {
        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        UserData registerRequest = new UserData("", "", null);
        Assertions.assertThrows(ResponseException.class, () -> serverFacade.registerUser(registerRequest));
    }

    @Test
    public void registerUserTestValidRequest() {
        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        UserData registerRequest = new UserData("player1", "player1", null);
        Assertions.assertDoesNotThrow(() -> serverFacade.registerUser(registerRequest));
    }

    @Test
    public void loginUserTestInvalidPassword() {
        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        UserData registerRequest = new UserData("player1", "player1", null);
        Assertions.assertDoesNotThrow(() -> serverFacade.registerUser(registerRequest), "");

        UserData loginRequest = new UserData("player1", "wrongpassword", null);
        Assertions.assertThrows(ResponseException.class, () -> serverFacade.loginUser(loginRequest));
    }

    @Test
    public void loginUserTestValidRequest() {
        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        UserData registerRequest = new UserData("player1", "player1", null);
        Assertions.assertDoesNotThrow(() -> serverFacade.registerUser(registerRequest), "");

        UserData loginRequest = new UserData("player1", "player1", null);
        Assertions.assertDoesNotThrow(() -> serverFacade.loginUser(loginRequest));
    }

    @Test
    public void logoutUserTestInvalidToken() {
        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        UserData registerRequest = new UserData("player1", "player1", null);
        Assertions.assertDoesNotThrow(() -> serverFacade.registerUser(registerRequest), "");
        // Logout user with current authToken
        Assertions.assertDoesNotThrow(() -> serverFacade.logoutUser());

        // Now that user has been logged out the authToken should be invalid
        Assertions.assertThrows(ResponseException.class, () -> serverFacade.logoutUser());
    }

    @Test
    public void logoutUserTestValidToken() {
        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        UserData registerRequest = new UserData("player1", "player1", null);
        Assertions.assertDoesNotThrow(() -> serverFacade.registerUser(registerRequest), "");
        // Logout user with current authToken
        Assertions.assertDoesNotThrow(() -> serverFacade.logoutUser());
    }

    @Test
    public void createGameTestInvalidGameName() {
        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        UserData registerRequest = new UserData("player1", "player1", null);
        Assertions.assertDoesNotThrow(() -> serverFacade.registerUser(registerRequest), "");

        var newGame = new GameData(0, null, null, null, null);
        Assertions.assertThrows(ResponseException.class, () -> serverFacade.createGame(newGame));
    }

    @Test
    public void createGameTestValidRequest() {
        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        UserData registerRequest = new UserData("player1", "player1", null);
        Assertions.assertDoesNotThrow(() -> serverFacade.registerUser(registerRequest), "");

        var newGame = new GameData(0, null, null, "myGame", null);
        var response = Assertions.assertDoesNotThrow(() -> serverFacade.createGame(newGame));
        Assertions.assertEquals(1, response.gameID());
    }
}
