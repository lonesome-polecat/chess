package dataaccess;

import java.sql.Connection;

public class MySqlDataAccess implements DataAccess {
    Connection database;

    public MySqlDataAccess() {
        this.configure();
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
