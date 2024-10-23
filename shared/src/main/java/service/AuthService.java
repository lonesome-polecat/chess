package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;
import server.ResponseException;

import java.util.Objects;

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
            throw new ResponseException(400, "This username has already been used. Choose a different one");
        }
    }

    public RegisterResponse loginRequest(UserData userData) throws ResponseException {
        var existingUser = userDAO.getUser(userData);

        if (existingUser == null) {
            throw new ResponseException(401, "Invalid username or password");
        }

        if (Objects.equals(existingUser.password(), userData.password())) {
            AuthData authData = authDAO.createAuth(userData);
            return new RegisterResponse(authData);
        } else {
            throw new ResponseException(401, "Invalid username or password");
        }
    }

    public void clearDB() {
        authDAO.clearAllAuth();
        userDAO.clearAllUsers();
        gameDAO.clearAllGames();
    }
}
