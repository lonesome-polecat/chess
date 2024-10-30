package dataaccess;

import model.*;

import java.util.List;

public interface DataAccess {
    abstract class AuthDAO {
        public abstract AuthData createAuth(UserData userData) throws DataAccessException;
        public abstract AuthData getAuth(String authToken) throws DataAccessException;
        public abstract void deleteAuth(String authToken) throws DataAccessException;
        public abstract void clearAllAuth() throws DataAccessException;
    }
    abstract class UserDAO {
        public abstract UserData getUser(UserData userData) throws DataAccessException;
        public abstract void createUser(UserData userData) throws DataAccessException;
        public abstract void clearAllUsers() throws DataAccessException;
    }
    abstract class GameDAO {
        public abstract GameData createGame(GameData gameData) throws DataAccessException;
        public abstract GameData getGame(int gameId) throws DataAccessException;
        public abstract void updateGame(GameData gameData) throws DataAccessException;
        public abstract List<GameData> getGames() throws DataAccessException;
        public abstract void clearAllGames() throws DataAccessException;
    }
}
