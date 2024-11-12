package ui;

import model.UserData;
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
//                case "list" -> listPets();
                case "signout" -> signOut();
//                case "adopt" -> adoptPet(params);
//                case "adoptall" -> adoptAllPets();
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
                - createGame <gameName>
                - listGames
                - joinGame <game id>
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
}
