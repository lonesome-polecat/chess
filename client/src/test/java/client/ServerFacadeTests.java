package client;

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
        Assertions.assertDoesNotThrow(() -> serverFacade.registerUser(registerRequest), "");
    }

    @Test
    public void loginUserTestInvalidPassword() {
        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        UserData registerRequest = new UserData("player1", "player1", null);
        Assertions.assertDoesNotThrow(() -> serverFacade.registerUser(registerRequest), "");

        UserData loginRequest = new UserData("", "", null);
        Assertions.assertThrows(ResponseException.class, () -> serverFacade.loginUser(loginRequest));
    }

    @Test
    public void loginUserTestValidRequest() {
        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        UserData registerRequest = new UserData("player1", "player1", null);
        Assertions.assertDoesNotThrow(() -> serverFacade.registerUser(registerRequest), "");
    }
}
