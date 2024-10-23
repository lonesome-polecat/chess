package service;

import dataaccess.DataAccess;
import model.AuthData;
import model.UserData;
import server.ResponseException;

public class AuthService {
    private final DataAccess.AuthDAO authDAO;
    private final DataAccess.UserDAO userDAO;

    public AuthService(DataAccess.AuthDAO authDAO, DataAccess.UserDAO userDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
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
}
