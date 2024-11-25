package ui;

import chess.ChessGame;
import model.*;
import model.ResponseException;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private State state;
    private ListGamesResponse allGames = null;

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
                case "creategame" -> createGame(params);
                case "listgames" -> listGames();
                case "playgame" -> joinGame(params);
                case "observegame" -> observeGame(params);
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
                    - help
                    """;
        }
        return """
                - createGame <gameName>
                - listGames
                - playGame <game #> <teamColor>
                - observeGame <game #>
                - signOut
                - quit
                - help
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
        try {
            server.registerUser(new UserData(username, password, email));
        } catch (ResponseException e) {
            return "That username is already taken";
        }
        state = State.SIGNED_IN;
        initGamesList();
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
        try {
            server.loginUser(new UserData(username, password, null));
        } catch (ResponseException e) {
            return "Invalid credentials";
        }
        state = State.SIGNED_IN;
        initGamesList();
        return String.format("You are signed in as %s", username);
    }

    public String signOut() throws ResponseException {
        try {
            server.logoutUser();
        } catch (ResponseException e) {
            return "Error: unable to sign out user";
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
        try {
            NewGameResponse response = server.createGame(newGame);
            return "You created a new game!";
        } catch (ResponseException e) {
            return "Error: unable to create new game";
        }
    }

    public String listGames() throws ResponseException {
        // make sure user is signIn
        // list all existing games
        if (state == State.SIGNED_OUT) {
            throw new ResponseException(401, "You must first sign in");
        }
        try {
            ListGamesResponse response = server.listGames();
            allGames = response;
            return printGames(response);
        } catch (ResponseException e) {
            return "Error: unable to list games";
        }
    }

    public String joinGame(String[] params) throws ResponseException {
        // make sure user is signIn
        // return a list of all existing games
        if (state == State.SIGNED_OUT) {
            throw new ResponseException(401, "You must first sign in");
        }
        if (params.length != 2) {
            throw new ResponseException(400, "You must enter which game number and team color to play as");
        }
        var gameID = params[0];
        var playerColor = params[1].toUpperCase();
        // Make sure playerColor is correct string
        if (!playerColor.equals("BLACK") && !playerColor.equals("WHITE")) {
            return "Must specify color to play as: BLACK or WHITE";
        }

        var joinGameRequest = new JoinGameRequest(gameID, playerColor);
        try {
            server.joinGame(joinGameRequest);
            // Enter gameplay state
            var result = displayGame(gameID, playerColor);
            if (!result) {
                return "Error: unable to play game";
            }
            return String.format("You joined a game as %s team", playerColor);
        } catch (ResponseException e) {
            return "Error: unable to play game";
        }
    }

    public String observeGame(String[] params) throws ResponseException {
        // make sure user is signIn
        // return a list of all existing games
        if (state == State.SIGNED_OUT) {
            throw new ResponseException(401, "You must first sign in");
        }
        if (params.length != 1) {
            throw new ResponseException(400, "You must enter a game number to join");
        }
        var gameID = params[0];

        var result = displayGame(gameID, "WHITE");
        if (!result) {
            return "Error: invalid game number";
        }
        return "You joined a game as an observer";
    }

    private void initGamesList() throws ResponseException {
        // Once user is logged in fetch games in background
        allGames = server.listGames();
    }

    private String printGames(ListGamesResponse allGames) {
        if (allGames == null) {
            return "No games have been listed";
        } else if (allGames.games().isEmpty()) {
            return "No games have been created";
        }

        String gameList = "";
        for (var game : allGames.games()) {
            var playersStr = String.format("(TEAM BLACK: %s, TEAM WHITE: %s)", game.blackUsername(), game.whiteUsername());
            var str = String.format("%s: %s %s\n", game.gameID(), game.gameName(), playersStr);
            gameList = gameList.concat(str);
        }
        return gameList;
    }

    private boolean displayGame(String gameID, String playerColor) throws ResponseException {
        for (var game : allGames.games()) {
            if (game.gameID() == Integer.parseInt(gameID)) {
                var chessGame = game.game();
                BoardUI.drawBoard(chessGame.getBoard(), ChessGame.TeamColor.WHITE);
                BoardUI.drawBoard(chessGame.getBoard(), ChessGame.TeamColor.BLACK);
                return true;
            }
        }
        return false;
    }
}
