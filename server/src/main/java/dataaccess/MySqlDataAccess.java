package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import server.ResponseException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MySqlDataAccess implements DataAccess {
    Connection database;

    public MySqlDataAccess() {
        this.configureDatabase();
    }


    public static class AuthDAO extends DataAccess.AuthDAO {

        public AuthData createAuth(UserData userData) {
        }

        public AuthData getAuth(String authToken) {
        }

        public void deleteAuth(String authToken) {
        }

        public void clearAllAuth() {
        }
    }

    public static class UserDAO extends DataAccess.UserDAO {

        public UserData getUser(UserData userData) {
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "SELECT username, json FROM user WHERE username=?";
                try (var ps = conn.prepareStatement(statement)) {
                    ps.setString(1, userData.username());
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return readUser(rs);
                        }
                    }
                }
            } catch (Exception e) {
               // throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
            }
            return null;
        }

        public void createUser(UserData userData) {

        }

        public void clearAllUsers() {

        }
    }


    public static class GameDAO extends DataAccess.GameDAO {

        public GameData createGame(GameData gameData) {
        }

        public void updateGame(GameData gameData) {
        }

        public GameData getGame(int gameId) {
        }

        public List<GameData> getGames() {
        }

        public void clearAllGames() {
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS user (
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
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
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`gameID`),
              INDEX(gameID)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws ResponseException {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException ex) {
            throw new ResponseException(500, String.format("Unable to create database: %s", ex.getMessage()));
        }
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (DataAccessException ex) {
            throw new ResponseException(500, String.format("Unable to connect to database: %s", ex.getMessage()));
        } catch (SQLException ex) {
            throw new ResponseException(500, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
