package dataaccess;

import model.*;

import java.util.Collection;

public interface DataAccess {
    abstract class AuthDAO {
        public abstract AuthData createAuth(UserData userData);
        public abstract void clearAllAuth();
    }
    abstract class UserDAO {
        public abstract UserData getUser(UserData userData);
        public abstract void createUser(UserData userData);
        public abstract void clearAllUsers();
    }
    abstract class GameDAO {
        public abstract GameData createGame(GameData gameData);
        public abstract Collection<GameData> getGames();
        public abstract void clearAllGames();
    }
}
