package service;

import model.AuthData;

public class RegisterResponse {
    private final String username;
    private final String authToken;

    public RegisterResponse(AuthData authData) {
        username = authData.username();
        authToken = authData.authToken();
    }
}
