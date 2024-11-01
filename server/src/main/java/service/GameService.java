package service;

import dataaccess.DataAccess;
import model.GameData;
import model.JoinGameRequest;
import model.ListGamesResponse;
import model.NewGameResponse;
import server.ResponseException;

import java.util.List;
import java.util.Objects;

public class GameService {
    private final DataAccess.GameDAO gameDAO;

    public GameService(DataAccess.GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public NewGameResponse newGameRequest(GameData gameData) throws ResponseException {
        if (gameData.gameName() != null && !gameData.gameName().isEmpty()) {
            GameData newGame = null;
            try {
                newGame = gameDAO.createGame(gameData);
            } catch (dataaccess.DataAccessException e) {
                throw new RuntimeException(e);
            }
            return new NewGameResponse(newGame.gameID());
        } else {
            throw new ResponseException(400, "Error: bad request");
        }
    }

    public ListGamesResponse listGamesRequest() {
        List<GameData> gameList = null;
        try {
            gameList = gameDAO.getGames();
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }
        return new ListGamesResponse(gameList);
    }

    public void joinGame(JoinGameRequest request, String username) throws ResponseException {
        if (request.gameID() == null) {
            throw new ResponseException(400, "Error: bad request");
        }

        GameData existingGame = null;
        try {
            existingGame = gameDAO.getGame(Integer.parseInt(request.gameID()));
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }
        if (existingGame == null) {
            throw new ResponseException(400, "Error: bad request");
        }

        if (Objects.equals(request.playerColor(), "WHITE")) {
            if (existingGame.whiteUsername() != null) {
                throw new ResponseException(403, "Error: already taken");
            } else {
                var modGame = new GameData(existingGame.gameID(), username, existingGame.blackUsername(), existingGame.gameName(), existingGame.game());
                try {
                    gameDAO.updateGame(modGame);
                } catch (dataaccess.DataAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (Objects.equals(request.playerColor(), "BLACK")) {
            if (existingGame.blackUsername() != null) {
                throw new ResponseException(403, "Error: already taken");
            } else {
                var modGame = new GameData(existingGame.gameID(), existingGame.whiteUsername(), username, existingGame.gameName(), existingGame.game());
                try {
                    gameDAO.updateGame(modGame);
                } catch (dataaccess.DataAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            throw new ResponseException(400, "Error: bad request");
        }
    }
}
