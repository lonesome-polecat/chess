package dataaccess;

import model.UserData;
import server.ResponseException;

import java.sql.Connection;

public class MySqlDataAccess implements DataAccess {
    Connection database;

    public MySqlDataAccess() {
        this.configure();
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

    private void configure() {
        try {
            DatabaseManager.createDatabase();
            database = DatabaseManager.getConnection();
        } catch (DataAccessException e) {
            System.out.println(e);
        }
    }
}
