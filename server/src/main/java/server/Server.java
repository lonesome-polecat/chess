package server;

import chess.*;
import dataaccess.MySqlDataAccess;
import model.*;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.*;
import spark.*;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    private final MySqlDataAccess.AuthDAO authDAO = new MySqlDataAccess.AuthDAO();
    private final MySqlDataAccess.UserDAO userDAO = new MySqlDataAccess.UserDAO();
    private final MySqlDataAccess.GameDAO gameDAO = new MySqlDataAccess.GameDAO();

    private final AuthService authService = new AuthService(authDAO, userDAO, gameDAO);
    private final GameService gameService = new GameService(gameDAO);

    private WSHandler wsHandler = new WSHandler();

    public int run(int desiredPort) {
        // Initialize database
        try {
            new MySqlDataAccess();
        } catch (Exception e) {
            System.out.printf("Cannot initialize databases: %s%n", e);
            System.exit(1);
        }

        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.webSocket("/ws/*", wsHandler);
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearDB);
        Spark.exception(ResponseException.class, this::exceptionHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.statusCode());
        res.type("application/json");
        var response = new ErrorResponse(ex.getMessage());
        res.body(new Gson().toJson(response));
    }

    private Object registerUser(Request request, Response response) throws ResponseException {
        var userData = new Gson().fromJson(request.body(), UserData.class);
        var registerResponse = authService.registerRequest(userData);
        return new Gson().toJson(registerResponse);
    }

    private Object loginUser(Request request, Response response) throws ResponseException {
        var userData = new Gson().fromJson(request.body(), UserData.class);
        var registerResponse = authService.loginRequest(userData);
        return new Gson().toJson(registerResponse);
    }

    private Object logoutUser(Request request, Response response) throws ResponseException {
        var authData = authService.verifyAuthToken(request);
        authService.logoutRequest(authData.authToken());
        return "{}";
    }

    private Object listGames(Request request, Response response) throws ResponseException {
        authService.verifyAuthToken(request);
        ListGamesResponse res = gameService.listGamesRequest();
        return new Gson().toJson(res);
    }

    private Object createGame(Request request, Response response) throws ResponseException {
        authService.verifyAuthToken(request);
        var gameData = new Gson().fromJson(request.body(), GameData.class);
        NewGameResponse res = gameService.newGameRequest(gameData);
        return new Gson().toJson(res);
    }

    private Object joinGame(Request request, Response response) throws ResponseException {
        var authData = authService.verifyAuthToken(request);
        var joinRequest = new Gson().fromJson(request.body(), JoinGameRequest.class);
        gameService.joinGame(joinRequest, authData.username());
        return "{}";
    }

    private Object clearDB(Request request, Response response) {
        authService.clearDB();
        return "{}";
    }

    @WebSocket
    public class WSHandler {
        // ConcurrentHashMap to store WebSocket connections
        private static final ConcurrentHashMap<Integer, List<Session>> sessionMap = new ConcurrentHashMap<>();
        private static final ConcurrentHashMap<Integer, GameData> gameDataMap = new ConcurrentHashMap<>();

        @OnWebSocketMessage
        public void onMessage(Session session, String message) throws Exception {
            var userCommand = new Gson().fromJson(message, UserGameCommand.class);
            // Verify user
            String username = authService.getUsernameFromAuthToken(userCommand.getAuthToken());

            var gameID = userCommand.getGameID();

            System.out.printf("%nReceived request from %s to join game #%d", username, userCommand.getGameID());

            if (userCommand.getCommandType() == UserGameCommand.CommandType.CONNECT) {
                // Check if there are existing connections to that game
                var sessions = sessionMap.get(gameID);
                if (sessions == null) {
                    sessions = new LinkedList<Session>();
                }

                // Check if game is active
                GameData game;
                boolean gameExists = gameDataMap.containsKey(gameID);
                if (!gameExists) {
                    // fetch game and cache it
                    game = gameService.getGame(gameID);
                    gameDataMap.put(gameID, game);
                } else {
                    game = gameDataMap.get(gameID);
                }

                // Notify all users that so-and-so joined the game (as color or as observer)
                String msg = String.format("%s joined the game", username);
                var serverMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg);
                String notifyMessage = new Gson().toJson(serverMsg);
                broadcastMessage(gameID, notifyMessage);

                // Add new connection to game
                sessions.add(session);
                sessionMap.put(gameID, sessions);

                // Send LOAD_GAME to new user
                var gameJson = new Gson().toJson(game);
                serverMsg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameJson);
                String loadGameMessage = new Gson().toJson(serverMsg);
                session.getRemote().sendString(loadGameMessage);
            }

            if (userCommand.getCommandType() == UserGameCommand.CommandType.MAKE_MOVE) {
                ChessGame.TeamColor playerColor;

                // Check if game is active
                GameData gameData = gameDataMap.get(gameID);

                // Check if user is a player or observer
                if (Objects.equals(username, gameData.whiteUsername())) {
                    playerColor = ChessGame.TeamColor.WHITE;
                } else if (Objects.equals(username, gameData.blackUsername())) {
                    playerColor = ChessGame.TeamColor.BLACK;
                } else {
                    // user is not a player
                    var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: user is not a player in game");
                    var errorMsg = new Gson().toJson(serverErrorMessage);
                    session.getRemote().sendString(errorMsg);
                    return;
                }

                // Deserialize the game and check if it is the player's turn
                ChessGame game = gameData.game();
                var currTurn = game.getTeamTurn();
                if (currTurn != playerColor) {
                    var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "It is not your turn");
                    var errorMsg = new Gson().toJson(serverErrorMessage);
                    session.getRemote().sendString(errorMsg);
                    return;
                }

                // double check that they sent an actual move
                if (userCommand.getMove() == null) {
                    throw new ResponseException(400, "Error: bad request");
                }

                // Get startPosition and endPosition in list
                // TODO: the following code is incomplete and needs to parse promotion piece
                ChessPosition[] positions = parseMove(userCommand.getMove());

                // Check that player's move is valid
                var board = game.getBoard();
                var piece = board.getPiece(positions[0]);
                if (piece == null) {
                    var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: invalid move");
                    var errorMsg = new Gson().toJson(serverErrorMessage);
                    session.getRemote().sendString(errorMsg);
                    return;
                } else if (piece.getTeamColor() != playerColor) {
                    var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: invalid move");
                    var errorMsg = new Gson().toJson(serverErrorMessage);
                    session.getRemote().sendString(errorMsg);
                    return;
                }
                var validMoves = game.validMoves(positions[0]);

                boolean isValid = false;
                ChessMove officialMove = null;
                for (var move : validMoves) {
                    var startPos = move.getStartPosition();
                    var endPos = move.getEndPosition();
                    if (startPos.equals(positions[0])) {
                        if (endPos.equals(positions[1])) {
                            isValid = true;
                            officialMove = move;
                            break;
                        }
                    }
                }

                if (!isValid) {
                    var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: invalid move");
                    var errorMsg = new Gson().toJson(serverErrorMessage);
                    session.getRemote().sendString(errorMsg);
                    return;
                }

                game.makeMove(officialMove);

                // Check to see if opponent in check or checkmate or stalemate
                ChessGame.TeamColor opponentColor = switch (playerColor) {
                    case BLACK -> ChessGame.TeamColor.WHITE;
                    case WHITE -> ChessGame.TeamColor.BLACK;
                };
                game.setTeamTurn(opponentColor);

                ServerMessage checkOrGameOverServerMsg = null;
                if (game.isInCheck(opponentColor)) {
                    if (game.isInCheckmate(opponentColor)) {
                        // user won! prep notification
                        game.gameOver();
                        game.setWinner(playerColor);

                        var msg = String.format("Checkmate! %s (%s) wins!", username, playerColor);
                        checkOrGameOverServerMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg);
                    } else {
                        // opponent in check, prep notification
                        var msg = String.format("%s is in check", opponentColor);
                        checkOrGameOverServerMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg);
                    }
                }
                if (game.isInStalemate(opponentColor)) {
                    // It's a tie! prep notification
                    game.gameOver();
                    var msg = String.format("%s is in stalemate! It's a tie!", opponentColor);
                    checkOrGameOverServerMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg);
                }

                // Update game in map and DB
                gameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
                gameDataMap.put(gameID, gameData);
                gameService.updateGame(gameData);

                // Send LOAD_GAME to all users
                var gameJson = new Gson().toJson(game);
                var serverMsg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameJson);
                String loadGameMessage = new Gson().toJson(serverMsg);
                broadcastMessage(gameID, loadGameMessage);

                if (checkOrGameOverServerMsg != null) {
                    // Notify users of check or end of game
                    String checkOrGameOverMsg = new Gson().toJson(checkOrGameOverServerMsg);
                    broadcastMessage(gameID, checkOrGameOverMsg);
                }
            }
            if (userCommand.getCommandType() == UserGameCommand.CommandType.LEAVE) {
                var sessions = sessionMap.get(gameID);
                for (int i = 0; i < sessions.size(); i++) {
                    if (sessions.get(i).equals(session)) {
                        sessions.remove(i);
                        break;
                    }
                }
                sessionMap.put(gameID, sessions);

                // Send NOTIFICATION to all users
                var serverMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s left the game", username));
                String leaveGameMessage = new Gson().toJson(serverMsg);
                broadcastMessage(gameID, leaveGameMessage);
                session.close();
            }

            if (userCommand.getCommandType() == UserGameCommand.CommandType.RESIGN) {
                ChessGame.TeamColor playerColor;
                ChessGame.TeamColor opponentColor;

                // Check if game is active
                GameData gameData = gameDataMap.get(gameID);

                // Check if user is a player or observer
                if (Objects.equals(username, gameData.whiteUsername())) {
                    playerColor = ChessGame.TeamColor.WHITE;
                    opponentColor = ChessGame.TeamColor.BLACK;
                } else if (Objects.equals(username, gameData.blackUsername())) {
                    playerColor = ChessGame.TeamColor.BLACK;
                    opponentColor = ChessGame.TeamColor.WHITE;
                } else {
                    // user is not a player
                    var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, "Error: user is not a player in game");
                    var errorMsg = new Gson().toJson(serverErrorMessage);
                    session.getRemote().sendString(errorMsg);
                    return;
                }

                // End game
                var game = gameData.game();
                game.gameOver();
                game.setWinner(opponentColor);

                // Update game in map and DB
                gameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
                gameDataMap.put(gameID, gameData);
                gameService.updateGame(gameData);

                // Get opponent username for message
                String opponentUsername;
                if (opponentColor == ChessGame.TeamColor.WHITE) {
                    opponentUsername = gameData.whiteUsername();
                } else {
                    opponentUsername = gameData.blackUsername();
                }

                // Send NOTIFICATION to all users
                var msg = String.format("%s (%s) resigned. %s (%s) wins!", username, playerColor, opponentUsername, opponentColor);
                var serverMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg);
                String leaveGameMessage = new Gson().toJson(serverMsg);
                broadcastMessage(gameID, leaveGameMessage);
            }
        }

        @OnWebSocketError
        public void onError(Session session, Throwable error) throws ResponseException {
            if (error.getClass() == ResponseException.class) {
                throw new ResponseException(((ResponseException) error).statusCode(), error.getMessage());
            }
            String sessionId = "unknown";
            if (session != null) {
                String path = session.getUpgradeRequest().getRequestURI().getPath();
            }
            System.err.println("Error in session " + sessionId + ": " + error.getMessage());
            error.printStackTrace();

            // Optionally notify the client
            if (session != null && session.isOpen()) {
                try {
                    session.getRemote().sendString("An error occurred: " + error.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // On close, remove the session from the group
        @OnWebSocketClose
        public void onClose(Session session, int statusCode, String reason) {
//
//            // Remove the session
//            List<Session> sessions = sessionMap.get(sessionId);
//            if (sessions != null) {
//                sessions.remove(session);
//                System.out.println("Disconnected: " + sessionId + " | Remaining connections: " + sessions.size());
//
//                if (sessions.isEmpty()) {
//                    sessionMap.remove(sessionId); // Clean up if no connections remain
//                }
//            }
        }

        // Helper to broadcast messages to a group
        private void broadcastMessage(Integer sessionId, String message) {
            List<Session> sessions = sessionMap.get(sessionId);

            if (sessions != null) {
                sessions.forEach(s -> {
                    try {
                        if (s.isOpen()) {
                            s.getRemote().sendString(message); // Send the message
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        /**
         * Parse ChessMove from String chess notation (i.e. a2a3)
         * @return ChessMove
         */
        private ChessPosition[] parseMove(String moveString) {
            // IMPORTANT! The first letter is the COL and the second is the ROW (inverse)
            int[] posIndices = {0, 0, 0, 0};
            for (int i = 0; i < moveString.length(); i++) {
                char pos = moveString.charAt(i);
                posIndices[i] = switch (pos) {
                    case 'a' -> 1;
                    case 'b' -> 2;
                    case 'c' -> 3;
                    case 'd' -> 4;
                    case 'e' -> 5;
                    case 'f' -> 6;
                    case 'g' -> 7;
                    case 'h' -> 8;
                    default -> (pos - '0');
                };
            }
            ChessPosition startPos = new ChessPosition(posIndices[1], posIndices[0]);
            ChessPosition endPos = new ChessPosition(posIndices[3], posIndices[2]);
            return new ChessPosition[]{startPos, endPos};
        }
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
