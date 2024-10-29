package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import server.ResponseException;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MySqlDataAccess implements DataAccess {
    Connection database;

    public MySqlDataAccess() {
        this.configure();
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

    private void configure() {
        try {
            DatabaseManager.createDatabase();
            database = DatabaseManager.getConnection();
        } catch (DataAccessException e) {
            System.out.println(e);
        }
    }
}
