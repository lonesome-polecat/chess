package service;

import dataaccess.DataAccess;
import model.GameData;
import model.JoinGameRequest;
import model.ListGamesResponse;
import model.NewGameResponse;
import server.ResponseException;

import java.util.Objects;

public class GameService {
    private final DataAccess.GameDAO gameDAO;

    public GameService(DataAccess.GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public NewGameResponse newGameRequest(GameData gameData) throws ResponseException {
        if (gameData.gameName() != null && !gameData.gameName().isEmpty()) {
            var newGame = gameDAO.createGame(gameData);
            return new NewGameResponse(newGame.gameID());
        } else {
            throw new ResponseException(400, "Error: bad request");
        }
    }

    public ListGamesResponse listGamesRequest() {
        var gameList = gameDAO.getGames();
        return new ListGamesResponse(gameList);
    }

    public void joinGame(JoinGameRequest request, String username) throws ResponseException {
        if (request.gameID() == null) {
            throw new ResponseException(400, "Error: bad request");
        }

        var existingGame = gameDAO.getGame(Integer.parseInt(request.gameID()));
        if (existingGame == null) {
            throw new ResponseException(400, "Error: bad request");
        }

        if (Objects.equals(request.playerColor(), "WHITE")) {
            if (existingGame.whiteUsername() != null) {
                throw new ResponseException(403, "Error: already taken");
            } else {
                var modGame = new GameData(existingGame.gameID(), username, existingGame.blackUsername(), existingGame.gameName(), existingGame.game());
                gameDAO.updateGame(modGame);
            }
        } else if (Objects.equals(request.playerColor(), "BLACK")) {
            if (existingGame.blackUsername() != null) {
                throw new ResponseException(403, "Error: already taken");
            } else {
                var modGame = new GameData(existingGame.gameID(), existingGame.whiteUsername(), username, existingGame.gameName(), existingGame.game());
                gameDAO.updateGame(modGame);
            }
        } else {
            throw new ResponseException(400, "Error: bad request");
        }
    }
}
