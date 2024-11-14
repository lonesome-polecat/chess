package service;

import model.GameData;
import model.JoinGameRequest;
import model.ListGamesResponse;
import model.NewGameResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.ResponseException;
import dataaccess.MemoryDataAccess;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private GameService gameService;
    private MemoryDataAccess.GameDAO gameDAO;

    @BeforeEach
    public void setUp() {
        gameDAO = new MemoryDataAccess.GameDAO();
        gameService = new GameService(gameDAO);  // Assuming authDAO and userDAO are not used in this test
    }

    @Test
    public void testNewGameRequestSuccess() throws ResponseException {
        // Arrange
        GameData gameData = new GameData(0, null, null, "ChessGame", null);

        // Act
        NewGameResponse response = gameService.newGameRequest(gameData);

        // Assert
        assertNotNull(response);
        assertTrue(response.gameID() > 0);  // Check if gameID is set
    }

    @Test
    public void testNewGameRequestThrowsException() {
        // Arrange
        GameData gameData = new GameData(0, null, null, "", null);  // Invalid game name

        // Act & Assert
        ResponseException thrown = assertThrows(ResponseException.class, () -> {
            gameService.newGameRequest(gameData);
        });

        assertEquals(400, thrown.statusCode());
        assertEquals("Error: bad request", thrown.getMessage());
    }

    @Test
    public void testListGamesRequestSuccess() {
        // Arrange
        GameData gameData = new GameData(0, null, null, "ChessGame", null);
        try {
            gameDAO.createGame(gameData);
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }

        // Act
        ListGamesResponse response = gameService.listGamesRequest();

        // Assert
        assertNotNull(response);
        assertFalse(response.games().isEmpty());  // Ensure there are games listed
    }

    @Test
    public void testListGamesRequestNoGames() {
        // Act
        ListGamesResponse response = gameService.listGamesRequest();

        // Assert
        assertNotNull(response);
        assertTrue(response.games().isEmpty());  // No games should be listed
    }

    @Test
    public void testJoinGameSuccess() throws ResponseException {
        // Arrange
        GameData gameData = new GameData(0, null, null, "ChessGame", null);
        try {
            gameDAO.createGame(gameData);
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }
        JoinGameRequest joinRequest = new JoinGameRequest("1", "WHITE");

        // Act
        gameService.joinGame(joinRequest, "player1");

        // Assert
        GameData updatedGame = null;
        try {
            updatedGame = gameDAO.getGame(1);
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }
        assertEquals("player1", updatedGame.whiteUsername());
    }

    @Test
    public void testJoinGameThrowsException() {
        // Arrange
        JoinGameRequest joinRequest = new JoinGameRequest(null, "WHITE");

        // Act & Assert
        ResponseException thrown = assertThrows(ResponseException.class, () -> {
            gameService.joinGame(joinRequest, "player1");
        });

        assertEquals(400, thrown.statusCode());
        assertEquals("Error: bad request", thrown.getMessage());
    }
}