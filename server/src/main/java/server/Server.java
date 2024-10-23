package server;

import dataaccess.MemoryDataAccess;
import jdk.jshell.spi.ExecutionControl;
import model.UserData;
import service.AuthService;
import spark.*;
import com.google.gson.Gson;

public class Server {

    private final MemoryDataAccess.AuthDAO authDAO = new MemoryDataAccess.AuthDAO();
    private final MemoryDataAccess.UserDAO userDAO = new MemoryDataAccess.UserDAO();
    private final MemoryDataAccess.GameDAO gameDAO = new MemoryDataAccess.GameDAO();

    private AuthService authService = new AuthService(authDAO, userDAO, gameDAO);

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
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
        res.status(ex.StatusCode());
        res.type("application/json");
        res.body(ex.getMessage());
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

    private Object logoutUser(Request request, Response response) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("");
    }

    private Object listGames(Request request, Response response) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("");
    }

    private Object createGame(Request request, Response response) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("");
    }

    private Object joinGame(Request request, Response response) throws ExecutionControl.NotImplementedException {
        throw new ExecutionControl.NotImplementedException("");
    }

    private Object clearDB(Request request, Response response) throws ResponseException {
        authService.clearDB();
        return "";
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
