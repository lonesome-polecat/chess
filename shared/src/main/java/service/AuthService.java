package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;
import server.ResponseException;
import spark.Request;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AuthService {
    private final DataAccess.AuthDAO authDAO;
    private final DataAccess.UserDAO userDAO;
    private final DataAccess.GameDAO gameDAO;

    public AuthService(DataAccess.AuthDAO authDAO, DataAccess.UserDAO userDAO, DataAccess.GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
    }

    public RegisterResponse registerRequest(UserData userData) throws ResponseException {
        var existingUser = userDAO.getUser(userData);

        if (existingUser == null) {
            userDAO.createUser(userData);
            AuthData authData = authDAO.createAuth(userData);
            return new RegisterResponse(authData);
        } else {
            throw new ResponseException(403, "Error: already taken");
        }
    }

    public RegisterResponse loginRequest(UserData userData) throws ResponseException {
        var existingUser = userDAO.getUser(userData);

        if (existingUser == null) {
            throw new ResponseException(401, "Error: unauthorized");
        }

        if (Objects.equals(existingUser.password(), userData.password())) {
            AuthData authData = authDAO.createAuth(userData);
            return new RegisterResponse(authData);
        } else {
            throw new ResponseException(401, "Error: unauthorized");
        }
    }

    public RegisterResponse logoutRequest(AuthData authData) throws ResponseException {
        throw new ResponseException(401, "Error: unauthorized");
    }

    public void clearDB() {
        authDAO.clearAllAuth();
        userDAO.clearAllUsers();
        gameDAO.clearAllGames();
    }

    public String verifyAuthToken(Request request) throws ResponseException {
        if (request.headers().contains("Authorization")) {
            var authToken = request.headers("Authorization");
            var authData = authDAO.getAuth(authToken);
            if (authData == null) {
                throw new ResponseException(401, "Error: unauthorized");
            }
            return authData.username();
        } else {
            throw new ResponseException(401, "Error: unauthorized");
        }
    }
}
