package ui;

import model.*;
import server.ResponseException;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private State state;

    private enum State {
            SIGNED_IN,
            SIGNED_OUT
    }

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.server = new ServerFacade(serverUrl);
        this.state = State.SIGNED_OUT;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "signin" -> signIn(params);
                case "register" -> register(params);
                case "newGame" -> createGame(params);
                case "listGames" -> listGames();
                case "joinGame" -> joinGame(params);
                case "signout" -> signOut();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String help() {
        if (state == State.SIGNED_OUT) {
            return """
                    - signIn <username> <password>
                    - register <username> <password> <email>
                    - quit
                    """;
        }
        return """
                - newGame <gameName>
                - listGames
                - joinGame <gameID> <teamColor>
                - signOut
                - quit
                """;
    }

    public String register(String[] params) throws ResponseException {
        for (var param : params) {
            System.out.println(param);
        }
        if (params.length < 2 || params.length > 3) {
            throw new ResponseException(400, "You must enter a username and password to register (email is optional)");
        }
        var username = params[0];
        var password = params[1];
        String email = null;
        if (params.length == 3) {
            email = params[2];
        }
        server.registerUser(new UserData(username, password, email));
        state = State.SIGNED_IN;
        return String.format("Successfully registered new user\nYou are now signed in as %s", username);
    }

    public String signIn(String[] params) throws ResponseException {
        for (var param : params) {
            System.out.println(param);
        }
        if (params.length != 2) {
            throw new ResponseException(400, "You must enter a username and password to sign in");
        }
        var username = params[0];
        var password = params[1];
        server.loginUser(new UserData(username, password, null));
        state = State.SIGNED_IN;
        return String.format("You are signed in as %s", username);
    }

    public String signOut() throws ResponseException {
        try {
            server.logoutUser();
        } catch (ResponseException e) {
            throw new ResponseException(401, "Error: unable to sign out user");
        }
        state = State.SIGNED_OUT;
        return "You are successfully signed out";
    }

    public String createGame(String[] params) throws ResponseException {
        // make sure user is signIn
        // create game and return gameID
        if (state == State.SIGNED_OUT) {
            throw new ResponseException(401, "You must first sign in");
        }
        if (params.length != 1) {
            throw new ResponseException(400, "You must enter a name for the new game");
        }
        var gameName = params[0];
        var newGame = new GameData(0, null, null, gameName, null);
        NewGameResponse response = server.createGame(newGame);
        return String.format("You created a new game with the gameID: %d", response.gameID());
    }

    public String listGames() throws ResponseException {
        // make sure user is signIn
        // list all existing games
        if (state == State.SIGNED_OUT) {
            throw new ResponseException(401, "You must first sign in");
        }
        ListGamesResponse response = server.listGames();
        return response.toString();
    }

    public String joinGame(String[] params) throws ResponseException {
        // make sure user is signIn
        // return a list of all existing games
        if (state == State.SIGNED_OUT) {
            throw new ResponseException(401, "You must first sign in");
        }
        if (params.length != 2) {
            throw new ResponseException(400, "You must enter a gameID and team color to join");
        }
        var gameID = params[0];
        var playerColor = params[1];
        var joinGameRequest = new JoinGameRequest(gameID, playerColor);
        server.joinGame(joinGameRequest);
        return String.format("You joined a game as %s team", playerColor);
    }
}
