package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.*;

public class MemoryDataAccess implements DataAccess {

    public static class AuthDAO extends DataAccess.AuthDAO {

        private final HashMap<String, AuthData> authMap = new HashMap<String, AuthData>();

        public AuthData createAuth(UserData userData) {
            var authToken = UUID.randomUUID().toString();
            var authData = new AuthData(authToken, userData.username());
            authMap.put(authToken, authData);
            return authData;
        }

        public AuthData getAuth(String authToken) {
            return authMap.get(authToken);
        }

        public void clearAllAuth() {
            authMap.clear();
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
            var newGame = new GameData(gameID, "", "", gameData.gameName(), game);
            gameMap.put(gameID, newGame);
            return newGame;
        }

        public void updateGame(GameData gameData) {
            gameMap.put(gameData.gameId(), gameData);
        }

        public GameData getGame(int gameId) {
            return gameMap.get(gameId);
        }

        public List<GameData> getGames() {
            LinkedList<GameData> gameList= new LinkedList<GameData>();
            for (int i = 1; i <= gameID; i++) {
                gameList.add(gameMap.get(i));
            }
            return gameList;
        }

        public void clearAllGames() {
            gameMap.clear();
            gameID = 0;
        }
    }
}
