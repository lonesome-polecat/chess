package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.RegisterResponse;
import model.UserData;
import server.ResponseException;
import spark.Request;

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
        UserData existingUser = null;
        try {
            existingUser = userDAO.getUser(userData);
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }

        if (existingUser == null) {
            if (userData.username() == null || userData.password() == null) {
                throw new ResponseException(400, "Error: bad request");
            }
            try {
                userDAO.createUser(userData);
            } catch (dataaccess.DataAccessException e) {
                throw new RuntimeException(e);
            }
            AuthData authData = null;
            try {
                authData = authDAO.createAuth(userData);
            } catch (dataaccess.DataAccessException e) {
                throw new RuntimeException(e);
            }
            return new RegisterResponse(authData);
        } else {
            throw new ResponseException(403, "Error: already taken");
        }
    }

    public RegisterResponse loginRequest(UserData userData) throws ResponseException {
        UserData existingUser = null;
        try {
            existingUser = userDAO.getUser(userData);
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }

        if (existingUser == null) {
            throw new ResponseException(401, "Error: unauthorized");
        }

        if (Objects.equals(existingUser.password(), userData.password())) {
            AuthData authData = null;
            try {
                authData = authDAO.createAuth(userData);
            } catch (dataaccess.DataAccessException e) {
                throw new RuntimeException(e);
            }
            return new RegisterResponse(authData);
        } else {
            throw new ResponseException(401, "Error: unauthorized");
        }
    }

    public void clearDB() {
        try {
            authDAO.clearAllAuth();
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            userDAO.clearAllUsers();
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            gameDAO.clearAllGames();
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthData verifyAuthToken(Request request) throws ResponseException {
        if (request.headers().contains("Authorization")) {
            var authToken = request.headers("Authorization");
            AuthData authData = null;
            try {
                authData = authDAO.getAuth(authToken);
            } catch (dataaccess.DataAccessException e) {
                throw new RuntimeException(e);
            }
            if (authData == null) {
                throw new ResponseException(401, "Error: unauthorized");
            }
            return authData;
        } else {
            throw new ResponseException(401, "Error: unauthorized");
        }
    }

    public void logoutRequest(String authToken) {
        try {
            authDAO.deleteAuth(authToken);
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
