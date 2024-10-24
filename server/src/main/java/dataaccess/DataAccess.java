package dataaccess;

import model.*;

import java.util.List;

public interface DataAccess {
    abstract class AuthDAO {
        public abstract AuthData createAuth(UserData userData);
        public abstract AuthData getAuth(String authToken);
        public abstract void clearAllAuth();
    }
    abstract class UserDAO {
        public abstract UserData getUser(UserData userData);
        public abstract void createUser(UserData userData);
        public abstract void clearAllUsers();
    }
    abstract class GameDAO {
        public abstract GameData createGame(GameData gameData);
        public abstract GameData getGame(int gameId);
        public abstract void updateGame(GameData gameData);
        public abstract List<GameData> getGames();
        public abstract void clearAllGames();
    }
}
