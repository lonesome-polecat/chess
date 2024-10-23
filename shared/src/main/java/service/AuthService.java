package service;

import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import server.ResponseException;

public class AuthService {

    private final MemoryDataAccess.AuthDAO authDAO = new MemoryDataAccess.AuthDAO();
    private final MemoryDataAccess.UserDAO userDAO = new MemoryDataAccess.UserDAO();

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
