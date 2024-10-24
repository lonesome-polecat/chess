package model;

public class RegisterResponse {
    public final String username;
    public final String authToken;

    public RegisterResponse(AuthData authData) {
        username = authData.username();
        authToken = authData.authToken();
    }
}
