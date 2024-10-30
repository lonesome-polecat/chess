package dataaccess;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import server.ResponseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class MySqlDataAccess implements DataAccess {

    public MySqlDataAccess() throws ResponseException {
        this.configureDatabase();
    }

    public static class AuthDAO extends DataAccess.AuthDAO {

        public AuthData createAuth(UserData userData) throws DataAccessException {
            var authToken = UUID.randomUUID().toString();
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "INSERT INTO auth (username, authToken) VALUES (?, ?)";
                try (var ps = conn.prepareStatement(statement)) {
                    ps.setString(1, userData.username());
                    ps.setString(2, authToken);
                    ps.executeQuery();
                }
            } catch (Exception e) {
                throw new DataAccessException("Error: cannot create auth");
            }
            return new AuthData(authToken, userData.username());
        }

        public AuthData getAuth(String authToken) throws DataAccessException {
            throw new DataAccessException("Not implemented");
        }

        public void deleteAuth(String authToken) throws DataAccessException {
            throw new DataAccessException("Not implemented");
        }

        public void clearAllAuth() throws DataAccessException {
            throw new DataAccessException("Not implemented");
        }
    }

    public static class UserDAO extends DataAccess.UserDAO {

        public UserData getUser(UserData userData) throws DataAccessException {
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
            try (var conn = DatabaseManager.getConnection()) {
                var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
                try (var ps = conn.prepareStatement(statement)) {
                    ps.setString(1, userData.username());
                    ps.setString(2, userData.password());
                    ps.setString(3, userData.email());
                    ps.executeQuery();
                }
            } catch (Exception e) {
                throw new DataAccessException("Error: cannot create user");
            }
        }

        public void clearAllUsers() throws DataAccessException {
            throw new DataAccessException("Not implemented");
        }

        private UserData readUser(ResultSet rs) throws SQLException {
            var username = rs.getString("username");
            var password = rs.getString("password");
            var email = rs.getString("email");
            return new UserData(username, password, email);
        }
    }


    public static class GameDAO extends DataAccess.GameDAO {

        public GameData createGame(GameData gameData) throws DataAccessException {
            throw new DataAccessException("Not implemented");
        }

        public void updateGame(GameData gameData) throws DataAccessException {
            throw new DataAccessException("Not implemented");
        }

        public GameData getGame(int gameId) throws DataAccessException {
            throw new DataAccessException("Not implemented");
        }

        public List<GameData> getGames() throws DataAccessException {
            throw new DataAccessException("Not implemented");
        }

        public void clearAllGames() throws DataAccessException {
            throw new DataAccessException("Not implemented");
        }
    }

    private AuthData readAuth(ResultSet rs) throws SQLException {
        var authToken = rs.getString("authToken");
        var username = rs.getString("username");
        return new AuthData(authToken, username);
    }


    private GameData readGame(ResultSet rs) throws SQLException {
        var json = rs.getString("json");
        return new Gson().fromJson(json, GameData.class);
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
