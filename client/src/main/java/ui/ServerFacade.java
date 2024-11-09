package ui;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.NewGameResponse;
import model.UserData;
import server.ResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {

    private final String serverUrl;
    private String authToken;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public void registerUser(UserData registerRequest) throws ResponseException {
        var path = "/user";
        var response = makeRequest("POST", path, registerRequest, AuthData.class);
        authToken = response.authToken();
    }

    public void loginUser(UserData loginRequest) throws ResponseException {
        var path = "/session";
        var response = makeRequest("POST", path, loginRequest, AuthData.class);
        authToken = response.authToken();
    }

    public void logoutUser() throws ResponseException {
        var path = "/session";
        var response = makeRequest("DELETE", path, null, Object.class);
    }

    public NewGameResponse createGame(GameData createGameRequest) throws ResponseException {
        var path = "/game";
        var response = makeRequest("POST", path, createGameRequest, NewGameResponse.class);
        return response;
    }

    public void clearDB() throws ResponseException {
        var path = "/db";
        var response = makeRequest("DELETE", path, null, Object.class);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setRequestProperty("Authorization", authToken);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

}
