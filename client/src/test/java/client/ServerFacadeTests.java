package client;

import chess.ChessGame;
import model.GameData;
import model.JoinGameRequest;
import model.ListGamesResponse;
import model.UserData;
import org.junit.jupiter.api.*;
import server.ResponseException;
import server.Server;
import ui.ServerFacade;

import java.util.LinkedList;


public class ServerFacadeTests {

    private static Server server;
    public static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(1999);
        System.out.println("Started test HTTP server on " + port);
    }

    @BeforeEach
    public void clearDB() {
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

    @Test
    public void joinGameTestInvalidGameId() {
        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        UserData registerRequest = new UserData("player1", "player1", null);
        Assertions.assertDoesNotThrow(() -> serverFacade.registerUser(registerRequest), "");

        var newGame = new GameData(0, null, null, "myGame", null);
        var response = Assertions.assertDoesNotThrow(() -> serverFacade.createGame(newGame));
        Assertions.assertEquals(1, response.gameID());

        var joinGameRequest = new JoinGameRequest("2", "WHITE");
        Assertions.assertThrows(ResponseException.class, () -> serverFacade.joinGame(joinGameRequest));
    }

    @Test
    public void joinGameTestValidRequest() {
        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        UserData registerRequest = new UserData("player1", "player1", null);
        Assertions.assertDoesNotThrow(() -> serverFacade.registerUser(registerRequest), "");

        var newGame = new GameData(0, null, null, "myGame", null);
        var response = Assertions.assertDoesNotThrow(() -> serverFacade.createGame(newGame));
        Assertions.assertEquals(1, response.gameID());

        var joinGameRequest = new JoinGameRequest("1", "WHITE");
        Assertions.assertDoesNotThrow(() -> serverFacade.joinGame(joinGameRequest));
    }

    @Test
    public void listGamesTestInvalidToken() {
        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        UserData registerRequest = new UserData("player1", "player1", null);
        Assertions.assertDoesNotThrow(() -> serverFacade.registerUser(registerRequest), "");

        var firstGame = new GameData(0, null, null, "myGame", null);
        var response = Assertions.assertDoesNotThrow(() -> serverFacade.createGame(firstGame));
        Assertions.assertEquals(1, response.gameID());

        var secondGame = new GameData(0, null, null, "myGame", null);
        response = Assertions.assertDoesNotThrow(() -> serverFacade.createGame(secondGame));
        Assertions.assertEquals(2, response.gameID());

        // Logout user with current authToken
        Assertions.assertDoesNotThrow(() -> serverFacade.logoutUser());

        Assertions.assertThrows(ResponseException.class, () -> serverFacade.listGames());
    }

    @Test
    public void listGamesTestValidRequest() {
        var serverFacade = new ServerFacade(String.format("http://localhost:%d", port));
        UserData registerRequest = new UserData("player1", "player1", null);
        Assertions.assertDoesNotThrow(() -> serverFacade.registerUser(registerRequest), "");

        var firstGame = new GameData(1, null, null, "myGame", new ChessGame());
        var response = Assertions.assertDoesNotThrow(() -> serverFacade.createGame(firstGame));
        Assertions.assertEquals(1, response.gameID());

        var secondGame = new GameData(2, null, null, "myGame", new ChessGame());
        response = Assertions.assertDoesNotThrow(() -> serverFacade.createGame(secondGame));
        Assertions.assertEquals(2, response.gameID());

        LinkedList<GameData> games = new LinkedList<GameData>();
        games.add(firstGame);
        games.add(secondGame);
        var expectedGames = new ListGamesResponse(games);

        var listGamesResponse = Assertions.assertDoesNotThrow(() -> serverFacade.listGames());
        Assertions.assertEquals(expectedGames, listGamesResponse);
    }

}
