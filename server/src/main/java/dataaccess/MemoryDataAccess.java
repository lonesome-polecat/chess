package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess {

    public static class AuthDAO extends DataAccess.AuthDAO {

        private final HashMap<String, AuthData> authDataMap = new HashMap<String, AuthData>();

        public AuthData createAuth(UserData userData) {
            var authToken = UUID.randomUUID().toString();
            var authData = new AuthData(authToken, userData.username());
            authDataMap.put(userData.username(), authData);
            return authData;
        }

        public void clearAllAuth() {
            authDataMap.clear();
        }
    }

    public static class UserDAO extends DataAccess.UserDAO {
        private final HashMap<String, UserData> userMap = new HashMap<String, UserData>();

        public UserData getUser(UserData userData) {
            return userMap.get(userData.username());
        }

        public void createUser(UserData userData) {
            userMap.put(userData.username(), userData);
        }

        public void clearAllUsers() {
            userMap.clear();
        }
    }

    public static class GameDAO extends DataAccess.GameDAO {
        private final HashMap<Integer, GameData> gameMap = new HashMap<Integer, GameData>();
        private int gameID = 0;

        public GameData createGame(GameData gameData) {
            var game = new ChessGame();
            gameID++;
            System.out.println(gameID);
            var newGame = new GameData(gameID, "", "", "", game);
            gameMap.put(gameID, newGame);
            return newGame;
        }

        public Collection<GameData> getGames() {
            return (Collection<GameData>) gameMap;
        }

        public void clearAllGames() {
            gameMap.clear();
            gameID = 0;
        }
    }
}
