package ui;

import com.google.gson.Gson;
import model.*;
import model.ResponseException;
import websocket.commands.UserGameCommand;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {

    private final String serverHTTPUrl;
    private final String serverWSUrl;
    private String authToken = "";
    private WSClient WSSession;
    private final ChessClient client;
    private String connectedGameId;

    public ServerFacade(ChessClient client, String url) {
        serverHTTPUrl = "http://" + url + "8080";
        serverWSUrl = "ws://" + url + "8080/ws";
        this.client = client;
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
        makeRequest("DELETE", path, null, Object.class);
        authToken = "";
    }

    public NewGameResponse createGame(GameData createGameRequest) throws ResponseException {
        var path = "/game";
        return makeRequest("POST", path, createGameRequest, NewGameResponse.class);
    }

    public void joinGame(JoinGameRequest joinGameRequest) throws ResponseException {
        var path = "/game";
        makeRequest("PUT", path, joinGameRequest, Object.class);
        try {
            WSSession = new WSClient(client, String.format("%s", serverWSUrl));
            connectedGameId = joinGameRequest.gameID();
            connect();
        } catch (Exception e) {
            System.out.printf("Error connecting to websocket: %s%n", e);
            throw new ResponseException(500, "ERROR");
        }
    }

    public ListGamesResponse listGames() throws ResponseException {
        var path = "/game";
        return makeRequest("GET", path, null, ListGamesResponse.class);
    }

    public void clearDB() throws ResponseException {
        var path = "/db";
        var response = makeRequest("DELETE", path, null, Object.class);
    }

    // WS methods
    public void connect() {
        // String msg = "This is my first message from my client";
        var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, Integer.parseInt(connectedGameId));
        try {
            WSSession.sendUserGameCommand(command);
        } catch (Exception e) {
            System.out.printf("Unable to send WS command: %s%n", e);
        }
    }

    public void makeMove() {
        // String msg = "This is my first message from my client";
        var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, Integer.parseInt(connectedGameId));
        try {
            WSSession.sendUserGameCommand(command);
        } catch (Exception e) {
            System.out.printf("Unable to send WS command: %s%n", e);
        }
    }

    public void leaveGame() {

    }

    public void resignGame() {

    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverHTTPUrl + path)).toURL();
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
