package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.UUID;

public class MemoryDataAccess {
    public static class AuthDAO {

        private final HashMap<String, AuthData> authDataMap = new HashMap<String, AuthData>();

        public AuthData createAuth(UserData userData) {
            var authToken = UUID.randomUUID().toString();
            var authData = new AuthData(authToken, userData.username());
            authDataMap.put(userData.username(), authData);
            return authData;
        }
    }

    public static class UserDAO {
        private final HashMap<String, UserData> userMap = new HashMap<String, UserData>();

        public UserData getUser(UserData userData) {
            System.out.println(userMap);
            System.out.println(userData);
            return userMap.get(userData.username());
        }

        public void createUser(UserData userData) {
            userMap.put(userData.username(), userData);
        }
    }
}
