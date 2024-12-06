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
        wsHandler.clear();
        return "{}";
    }

    @WebSocket
    public class WSHandler {
        // ConcurrentHashMap to store WebSocket connections
        private static final ConcurrentHashMap<Integer, List<Session>> SESSION_MAP = new ConcurrentHashMap<>();
        private static final ConcurrentHashMap<Integer, GameData> GAME_DATA_MAP = new ConcurrentHashMap<>();

        @OnWebSocketMessage
        public void onMessage(Session session, String message) throws Exception {
            var userCommand = new Gson().fromJson(message, UserGameCommand.class);
            // Verify user
            String username;
            try {
                username = authService.getUsernameFromAuthToken(userCommand.getAuthToken());
            } catch (Exception e) {
                // Bad gameID
                var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null);
                serverErrorMessage.setErrorMessage("Error: invalid authToken");
                var errorMsg = new Gson().toJson(serverErrorMessage);
                session.getRemote().sendString(errorMsg);
                return;
            }
            var gameID = userCommand.getGameID();

            if (userCommand.getCommandType() == UserGameCommand.CommandType.CONNECT) {
                userConnect(session, username, gameID);
            }
            if (userCommand.getCommandType() == UserGameCommand.CommandType.MAKE_MOVE) {
                userMakeMove(session, username, gameID, userCommand.getMove());
            }
            if (userCommand.getCommandType() == UserGameCommand.CommandType.LEAVE) {
                userLeave(session, username, gameID);
            }
            if (userCommand.getCommandType() == UserGameCommand.CommandType.RESIGN) {
                userResign(session, username, gameID);
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
        }

        // Helper to broadcast messages to a group
        private void broadcastMessage(Integer sessionId, String message, Session session) {
            List<Session> sessions = SESSION_MAP.get(sessionId);
            if (sessions == null) {
                return;
            }
            sessions.forEach(s -> {
                try {
                    if (s.isOpen()) {
                        if (!s.equals(session)) {
                            s.getRemote().sendString(message); // Send the message
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        private void userConnect(Session session, String username, int gameID) throws Exception {
            // Check if there are existing connections to that game
            var sessions = SESSION_MAP.get(gameID);
            if (sessions == null) {
                sessions = new LinkedList<Session>();
            }
            // Check if game is active
            GameData game;
            boolean gameExists = GAME_DATA_MAP.containsKey(gameID);
            if (!gameExists) {
                // fetch game and cache it
                try {
                    game = gameService.getGame(gameID);
                    GAME_DATA_MAP.put(gameID, game);
                } catch (Exception e) {
                    // Bad gameID
                    var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null);
                    serverErrorMessage.setErrorMessage("Error: invalid gameID");
                    var errorMsg = new Gson().toJson(serverErrorMessage);
                    session.getRemote().sendString(errorMsg);
                    return;
                }
            } else {
                game = GAME_DATA_MAP.get(gameID);
            }
            String playerOrObserver = "an observer";
            // Check if user is a player or observer
            if (Objects.equals(username, game.whiteUsername())) {
                playerOrObserver = "WHITE";
            } else if (Objects.equals(username, game.blackUsername())) {
                playerOrObserver = "BLACK";
            }

            // Notify all users that so-and-so joined the game (as color or as observer)
            String msg = String.format("%s joined the game as %s", username, playerOrObserver);
            var serverMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg);
            String notifyMessage = new Gson().toJson(serverMsg);
            broadcastMessage(gameID, notifyMessage, session);
            // Add new connection to game
            sessions.add(session);
            SESSION_MAP.put(gameID, sessions);

            // Send LOAD_GAME to new user
            serverMsg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null);
            serverMsg.setGame(game.game());
            String loadGameMessage = new Gson().toJson(serverMsg);
            session.getRemote().sendString(loadGameMessage);

        }

        private void userMakeMove(Session session, String username, int gameID, ChessMove move) throws Exception {
            ChessGame.TeamColor playerColor;
            // Check if game is active
            GameData gameData = GAME_DATA_MAP.get(gameID);
            if (gameData.blackUsername() == null || gameData.whiteUsername() == null) {
                // Just in case, refresh game
                gameData = gameDAO.getGame(gameID);
                GAME_DATA_MAP.put(gameID, gameData);
            }
            // Check if user is a player or observer
            var players = getPlayerAndOpponentColors(session, username, gameData);
            if (players == null) {
                return;
            }
            playerColor = players[0];
            var opponentColor = players[1];
            // Deserialize the game and check if it is the player's turn
            ChessGame game = gameData.game();
            var currTurn = game.getTeamTurn();
            if (currTurn != playerColor) {
                var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null);
                serverErrorMessage.setErrorMessage("It is not your turn");
                var errorMsg = new Gson().toJson(serverErrorMessage);
                session.getRemote().sendString(errorMsg);
                return;
            }
            // check if game has already ended
            if (game.getGameState() == ChessGame.GameState.GAME_OVER) {
                var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null);
                serverErrorMessage.setErrorMessage("Error: the game is over. No more moves can be made");
                var errorMsg = new Gson().toJson(serverErrorMessage);
                session.getRemote().sendString(errorMsg);
                return;
            }
            // double check that they sent an actual move
            if (move == null) {
                throw new ResponseException(400, "Error: bad request");
            }
            // Check that player's move is valid
            var board = game.getBoard();
            var piece = board.getPiece(move.getStartPosition());
            if (piece == null || piece.getTeamColor() != playerColor) {
                sendInvalidMoveError(session);
                return;
            }
            var validMoves = game.validMoves(move.getStartPosition());
            boolean isValid = false;
            for (var validMove : validMoves) {
                var startPos = validMove.getStartPosition();
                var endPos = validMove.getEndPosition();
                if (startPos.equals(move.getStartPosition())) {
                    if (endPos.equals(move.getEndPosition())) {
                        isValid = true;
                        break;
                    }
                }
            }
            if (!isValid) {
                var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null);
                serverErrorMessage.setErrorMessage("Error: invalid move");
                var errorMsg = new Gson().toJson(serverErrorMessage);
                session.getRemote().sendString(errorMsg);
                return;
            }
            game.makeMove(move);
            // Check to see if opponent in check or checkmate or stalemate
            game.setTeamTurn(opponentColor);
            ServerMessage checkOrGameOverServerMsg = checkIfCheckOrGameOver(username, playerColor, opponentColor, game);
            // Update game in map and DB
            gameData = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
            GAME_DATA_MAP.put(gameID, gameData);
            gameService.updateGame(gameData);
            // Send LOAD_GAME to all users
            var serverMsg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null);
            serverMsg.setGame(gameData.game());
            String loadGameMessage = new Gson().toJson(serverMsg);
            broadcastMessage(gameID, loadGameMessage, null);
            // Send NOTIFICATION of move to all other users
            String startPos = ChessPosition.parsePositionToString(move.getStartPosition());
            String endPos = ChessPosition.parsePositionToString(move.getEndPosition());
            var msg = String.format("%s (%s) moved from %s to %s", username, playerColor, startPos, endPos);
            serverMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg);
            String moveNotification = new Gson().toJson(serverMsg);
            broadcastMessage(gameID, moveNotification, session);
            if (checkOrGameOverServerMsg != null) {
                // Notify users of check or end of game
                String checkOrGameOverMsg = new Gson().toJson(checkOrGameOverServerMsg);
                broadcastMessage(gameID, checkOrGameOverMsg, null);
            }
        }

        private void userLeave(Session session, String username, int gameID) throws Exception {

            ChessGame.TeamColor playerColor = null;

            // Check if game is active
            GameData gameData = GAME_DATA_MAP.get(gameID);

            // Check if user is a player or observer
            if (Objects.equals(username, gameData.whiteUsername())) {
                playerColor = ChessGame.TeamColor.WHITE;
            } else if (Objects.equals(username, gameData.blackUsername())) {
                playerColor = ChessGame.TeamColor.BLACK;
            }

            if (playerColor != null) {
                // remove them as player from the game and update
                if (playerColor == ChessGame.TeamColor.WHITE) {
                    gameData = new GameData(gameID, null, gameData.blackUsername(), gameData.gameName(), gameData.game());
                } else {
                    gameData = new GameData(gameID, gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
                }

                // Update game in map and DB
                GAME_DATA_MAP.put(gameID, gameData);
                gameService.updateGame(gameData);
            }

            var sessions = SESSION_MAP.get(gameID);
            for (int i = 0; i < sessions.size(); i++) {
                if (sessions.get(i).equals(session)) {
                    sessions.remove(i);
                    break;
                }
            }
            SESSION_MAP.put(gameID, sessions);

            // Send NOTIFICATION to all users
            var serverMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s left the game", username));
            String leaveGameMessage = new Gson().toJson(serverMsg);
            broadcastMessage(gameID, leaveGameMessage, null);
            session.close();
        }

        private void userResign(Session session, String username, int gameID) throws Exception {

            ChessGame.TeamColor playerColor;
            ChessGame.TeamColor opponentColor;

            // Check if game is active
            GameData gameData = GAME_DATA_MAP.get(gameID);
            if (gameData.blackUsername() == null || gameData.whiteUsername() == null) {
                // Just in case, refresh game
                gameData = gameDAO.getGame(gameID);
                GAME_DATA_MAP.put(gameID, gameData);
            }

            // Check if user is a player or observer
            var players = getPlayerAndOpponentColors(session, username, gameData);
            if (players == null) {
                return;
            }
            playerColor = players[0];
            opponentColor = players[1];

            // check if game has already ended
            if (gameData.game().getGameState() == ChessGame.GameState.GAME_OVER) {
                var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null);
                serverErrorMessage.setErrorMessage("Error: the game is already over");
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
            GAME_DATA_MAP.put(gameID, gameData);
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
            broadcastMessage(gameID, leaveGameMessage, null);
        }

        private void sendInvalidMoveError(Session session) throws Exception {
            var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null);
            serverErrorMessage.setErrorMessage("Error: invalid move");
            var errorMsg = new Gson().toJson(serverErrorMessage);
            session.getRemote().sendString(errorMsg);
        }

        private ChessGame.TeamColor[] getPlayerAndOpponentColors(Session session, String username, GameData gameData) throws Exception {
            ChessGame.TeamColor playerColor;
            ChessGame.TeamColor opponentColor;

            // Check if user is a player or observer
            if (Objects.equals(username, gameData.whiteUsername())) {
                playerColor = ChessGame.TeamColor.WHITE;
                opponentColor = ChessGame.TeamColor.BLACK;
            } else if (Objects.equals(username, gameData.blackUsername())) {
                playerColor = ChessGame.TeamColor.BLACK;
                opponentColor = ChessGame.TeamColor.WHITE;
            } else {
                // user is not a player
                var serverErrorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null);
                serverErrorMessage.setErrorMessage("Error: user is not a player in game");
                var errorMsg = new Gson().toJson(serverErrorMessage);
                session.getRemote().sendString(errorMsg);
                return null;
            }
            return new ChessGame.TeamColor[]{playerColor, opponentColor};
        }

        private ServerMessage checkIfCheckOrGameOver(String username, ChessGame.TeamColor player, ChessGame.TeamColor opponent, ChessGame game) {
            ServerMessage checkOrGameOverServerMsg = null;
            if (game.isInCheck(opponent)) {
                if (game.isInCheckmate(opponent)) {
                    // user won! prep notification
                    game.gameOver();
                    game.setWinner(player);

                    var msg = String.format("Checkmate! %s (%s) wins!", username, player);
                    checkOrGameOverServerMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg);
                } else {
                    // opponent in check, prep notification
                    var msg = String.format("%s is in check", opponent);
                    checkOrGameOverServerMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg);
                }
            }
            if (game.isInStalemate(opponent)) {
                // It's a tie! prep notification
                game.gameOver();
                var msg = String.format("%s is in stalemate! It's a tie!", opponent);
                checkOrGameOverServerMsg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, msg);
            }
            return checkOrGameOverServerMsg;
        }

        public void clear() {
            GAME_DATA_MAP.clear();
            SESSION_MAP.clear();
        }
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
