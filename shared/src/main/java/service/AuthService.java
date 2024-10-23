package service;

import dataaccess.MemoryDataAccess;
import model.UserData;

public class AuthService {

    private final MemoryDataAccess.AuthDAO dataAccess = new MemoryDataAccess.AuthDAO();

    public String registerRequest(UserData userData) {
        return dataAccess.getThing();
    }
}
