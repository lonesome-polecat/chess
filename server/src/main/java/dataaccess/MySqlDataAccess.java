package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class MySqlDataAccess implements DataAccess {

    public MySqlDataAccess() throws DataAccessException {
        this.configureDatabase();
    }

    public static class AuthDAO extends DataAccess.AuthDAO {

        public AuthData createAuth(UserData userData) throws DataAccessException {
            // Insert an AuthData object into auth table and return authToken
            var authToken = UUID.randomUUID().toString();
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "INSERT INTO auth (username, authToken) VALUES (?, ?)";
                try (var ps = conn.prepareStatement(statement)) {
                    ps.setString(1, userData.username());
                    ps.setString(2, authToken);
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                throw new DataAccessException("Error: cannot create auth");
            }
            return new AuthData(authToken, userData.username());
        }

        public AuthData getAuth(String authToken) throws DataAccessException {
            // Return matching authToken from database else return null
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "SELECT username, authToken FROM auth WHERE authToken=?";
                try (var ps = conn.prepareStatement(statement)) {
                    ps.setString(1, authToken);
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return readAuth(rs);
                        } else {
                            return null;
                        }
                    }
                }
            } catch (Exception e) {
                throw new DataAccessException("Error: cannot get auth");
            }
        }

        public void deleteAuth(String authToken) throws DataAccessException {
            // delete matching authToken in auth table for logging out user
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "DELETE from auth where authToken=?";
                try (var ps = conn.prepareStatement(statement)) {
                    ps.setString(1, authToken);
                    var rowsAffected = ps.executeUpdate();
                    if (rowsAffected < 1) {
                        throw new DataAccessException("Error: invalid authToken");
                    }
                }
            } catch (Exception e) {
                throw new DataAccessException("Error: cannot delete from auth table");
            }
        }

        public void clearAllAuth() throws DataAccessException {
            // Clear auth table in database
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "TRUNCATE auth";
                try (var ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                throw new DataAccessException("Error: cannot clear auth table");
            }
        }

        private AuthData readAuth(ResultSet rs) throws SQLException {
            // Convert auth table ResultSet into AuthData object
            var authToken = rs.getString("authToken");
            var username = rs.getString("username");
            return new AuthData(authToken, username);
        }
    }

    public static class UserDAO extends DataAccess.UserDAO {

        public UserData getUser(UserData userData) throws DataAccessException {
            // Find user data in database by username and return UserData object
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "SELECT username, password, email FROM user WHERE username=?";
                try (var ps = conn.prepareStatement(statement)) {
                    ps.setString(1, userData.username());
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return readUser(rs);
                        }
                    }
                }
            } catch (Exception e) {
               throw new DataAccessException("Error: cannot get user");
            }
            return null;
        }

        public void createUser(UserData userData) throws DataAccessException {
            // Insert a UserData object into user table
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
                try (var ps = conn.prepareStatement(statement)) {
                    ps.setString(1, userData.username());
                    ps.setString(2, userData.password());
                    ps.setString(3, userData.email());
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                throw new DataAccessException("Error: cannot create user");
            }
        }

        public void clearAllUsers() throws DataAccessException {
            // Clear user table in database
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "TRUNCATE user";
                try (var ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                throw new DataAccessException("Error: cannot clear user table");
            }
        }

        private UserData readUser(ResultSet rs) throws SQLException {
            // Convert user table ResultSet into UserData object
            var username = rs.getString("username");
            var password = rs.getString("password");
            var email = rs.getString("email");
            return new UserData(username, password, email);
        }
    }

    public static class GameDAO extends DataAccess.GameDAO {

        public GameData createGame(GameData gameData) throws DataAccessException {
            // Insert GameData object into game table
            int gameId = 0;
            var game = new ChessGame();
            var gameString = new Gson().toJson(game);

            try (var conn = DatabaseManager.getConnection()) {
                var statement = "INSERT INTO game (whiteUsername, blackUsername, gameName, gameString) VALUES (null, null, ?, ?)";
                try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                    ps.setString(1, (gameData.gameName()));
                    ps.setString(2, (gameString));
                    ps.executeUpdate();
                    var rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        gameId = rs.getInt(1);
                    }
                    return new GameData(gameId, null, null, gameData.gameName(), game);
                }
            } catch (Exception e) {
                throw new DataAccessException("Error: cannot create game");
            }
        }

        public void updateGame(GameData gameData) throws DataAccessException {
            // Modify existing game with new player information in game table in database
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "UPDATE game SET whiteUsername=?, blackUsername=? WHERE gameID=?";
                try (var ps = conn.prepareStatement(statement)) {
                    ps.setString(1, gameData.whiteUsername());
                    ps.setString(2, gameData.blackUsername());
                    ps.setInt(3, gameData.gameID());
                    var rowsAffected = ps.executeUpdate();
                    if (rowsAffected < 1) {
                        throw new DataAccessException("Error: cannot update name");
                    }
                }
            } catch (Exception e) {
                throw new DataAccessException("Error: cannot get game");
            }
        }

        public GameData getGame(int gameId) throws DataAccessException {
            // Find game in database with matcing gameID and return GameData object
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, gameString FROM game WHERE gameID=?";
                try (var ps = conn.prepareStatement(statement)) {
                    ps.setInt(1, gameId);
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return readGame(rs);
                        }
                    }
                }
            } catch (Exception e) {
                throw new DataAccessException("Error: cannot get game");
            }
            return null;
        }

        public List<GameData> getGames() throws DataAccessException {
            // Find and return all games in game table in database
            var gamesList = new LinkedList<GameData>();
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "SELECT * FROM game";
                try (var ps = conn.prepareStatement(statement)) {
                    try (var rs = ps.executeQuery()) {
                        while (rs.next()) {
                            gamesList.add(readGame(rs));
                        }
                    }
                }
            } catch (Exception e) {
                throw new DataAccessException("Error: cannot get game");
            }
            return gamesList;
        }

        public void clearAllGames() throws DataAccessException {
            // Clear game table in database
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "TRUNCATE game";
                try (var ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                throw new DataAccessException("Error: cannot clear game table");
            }
        }

        private GameData readGame(ResultSet rs) throws SQLException {
            // Convert game ResultSet into GameData object
            var gameID = rs.getInt("gameID");
            var whiteUsername = rs.getString("whiteUsername");
            var blackUsername = rs.getString("blackUsername");
            var gameName = rs.getString("gameName");
            var gameString = rs.getString("gameString");
            var game = new Gson().fromJson(gameString, ChessGame.class);
            return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS user (
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) DEFAULT NULL,
              PRIMARY KEY (`username`),
              INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,

            """
            CREATE TABLE IF NOT EXISTS auth (
              `username` varchar(256) NOT NULL,
              `authToken` varchar(256) NOT NULL,
              PRIMARY KEY (`authToken`),
              INDEX(authToken)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,

            """
            CREATE TABLE IF NOT EXISTS game (
              `gameID` int NOT NULL AUTO_INCREMENT,
              `whiteUsername` varchar(256) DEFAULT NULL,
              `blackUsername` varchar(256) DEFAULT NULL,
              `gameName` varchar(256) DEFAULT NULL,
              `gameString` TEXT DEFAULT NULL,
              PRIMARY KEY (`gameID`),
              INDEX(gameID)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws DataAccessException {
        // Create database and user, auth, and game tables if they do not already exist
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to create database: %s", ex.getMessage()));
        }
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (DataAccessException ex) {
            throw new DataAccessException(String.format("Unable to connect to database: %s", ex.getMessage()));
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to query database: %s", ex.getMessage()));
        }
    }
}
