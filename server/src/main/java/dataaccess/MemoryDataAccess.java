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
        private final HashMap<String, GameData> gameMap = new HashMap<String, GameData>();

        public GameData createGame(GameData gameData) {
            var newGame = new ChessGame();
            return new GameData(gameData.gameId(), "", "", "", newGame);
        }

        public Collection<GameData> getGames() {
            return (Collection<GameData>) gameMap;
        }

        public void clearAllGames() {
            gameMap.clear();
        }
    }
}
