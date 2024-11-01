package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlDatabaseTests {
    private MySqlDataAccess.AuthDAO authDAO;
    private MySqlDataAccess.UserDAO userDAO;
    private MySqlDataAccess.GameDAO gameDAO;
    private UserData testUser;
    private GameData testGame;

    @BeforeEach
    public void setUp() throws Exception {
        try {
            new MySqlDataAccess();
        } catch (DataAccessException e) {
            System.exit(1);
        }
        authDAO = new MySqlDataAccess.AuthDAO();
        userDAO = new MySqlDataAccess.UserDAO();
        gameDAO = new MySqlDataAccess.GameDAO();

        authDAO.clearAllAuth();
        userDAO.clearAllUsers();
        gameDAO.clearAllGames();

        testUser = new UserData("testuser", "password", "testuser@example.com");
        testGame = new GameData(1, null, null, "Test Game", new ChessGame());
    }

    // AuthDAO Tests
    @Test
    public void testCreateAuthSuccess() throws Exception {
        userDAO.createUser(testUser);
        AuthData authData = authDAO.createAuth(testUser);
        assertNotNull(authData);
        assertEquals(testUser.username(), authData.username());
    }

    @Test
    public void testCreateAuthFailure() {
        assertThrows(DataAccessException.class, () -> {
            authDAO.createAuth(new UserData(null, "password", "email"));
        });
    }

    @Test
    public void testGetAuthSuccess() throws Exception {
        userDAO.createUser(testUser);
        AuthData authData = authDAO.createAuth(testUser);
        AuthData retrievedAuth = authDAO.getAuth(authData.authToken());
        assertNotNull(retrievedAuth);
        assertEquals(authData.authToken(), retrievedAuth.authToken());
    }

    @Test
    public void testGetAuthFailure() throws DataAccessException {
        userDAO.createUser(testUser);
        assertNull(authDAO.getAuth("invalidToken"));
    }

    @Test
    public void testDeleteAuthSuccess() throws Exception {
        userDAO.createUser(testUser);
        AuthData authData = authDAO.createAuth(testUser);
        authDAO.deleteAuth(authData.authToken());
        assertNull(authDAO.getAuth(authData.authToken()));
    }

    @Test
    public void testDeleteAuthFailure() {
        assertThrows(DataAccessException.class, () -> authDAO.deleteAuth("invalidToken"));
    }

    @Test
    public void testClearAllAuthSuccess() throws Exception {
        userDAO.createUser(testUser);
        authDAO.createAuth(testUser);
        authDAO.clearAllAuth();
        assertNull(authDAO.getAuth(testUser.username()));
    }

    // UserDAO Tests
    @Test
    public void testCreateUserSuccess() throws Exception {
        userDAO.createUser(testUser);
        UserData retrievedUser = userDAO.getUser(testUser);
        assertNotNull(retrievedUser);
        assertEquals(testUser.username(), retrievedUser.username());
    }

    @Test
    public void testCreateUserFailure() throws Exception {
        userDAO.createUser(testUser);
        assertThrows(DataAccessException.class, () -> userDAO.createUser(testUser));
    }

    @Test
    public void testClearAllUsersSuccess() throws Exception {
        userDAO.createUser(testUser);
        userDAO.clearAllUsers();
        assertNull(userDAO.getUser(testUser));
    }

    // GameDAO Tests
    @Test
    public void testCreateGameSuccess() throws Exception {
        GameData createdGame = gameDAO.createGame(testGame);
        assertNotNull(createdGame);
        assertEquals(testGame.gameName(), createdGame.gameName());
    }

    @Test
    public void testCreateGameFailure() {
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(null));
    }

    @Test
    public void testUpdateGameSuccess() throws Exception {
        GameData createdGame = gameDAO.createGame(testGame);
        GameData updatedGame = new GameData(createdGame.gameID(), "player1", null, createdGame.gameName(), createdGame.game());
        gameDAO.updateGame(updatedGame);
        GameData retrievedGame = gameDAO.getGame(createdGame.gameID());
        assertNotNull(retrievedGame);
        assertEquals("player1", retrievedGame.whiteUsername());
    }

    @Test
    public void testUpdateGameFailure() throws DataAccessException {
        GameData createdGame = gameDAO.createGame(testGame);
        GameData updatedGame = new GameData(createdGame.gameID(), "player1", null, createdGame.gameName(), createdGame.game());
        gameDAO.updateGame(updatedGame);

        assertThrows(DataAccessException.class, () -> {
            GameData badGameUpdate = new GameData(-1, "player2", null, createdGame.gameName(), createdGame.game());
            gameDAO.updateGame(badGameUpdate);
        });
    }

    @Test
    public void testGetGamesSuccess() throws Exception {
        gameDAO.createGame(testGame);
        assertFalse(gameDAO.getGames().isEmpty());
    }

    @Test
    public void testGetGamesFailure() throws Exception {
        gameDAO.clearAllGames();
        assertTrue(gameDAO.getGames().isEmpty());
    }

    @Test
    public void testClearAllGamesSuccess() throws Exception {
        gameDAO.createGame(testGame);
        gameDAO.clearAllGames();
        assertTrue(gameDAO.getGames().isEmpty());
    }
}
