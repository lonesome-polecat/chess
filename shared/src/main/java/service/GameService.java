package service;

import dataaccess.DataAccess;
import model.GameData;
import server.ResponseException;

import java.util.Objects;

public class GameService {
    private final DataAccess.AuthDAO authDAO;
    private final DataAccess.UserDAO userDAO;
    private final DataAccess.GameDAO gameDAO;

    public GameService(DataAccess.AuthDAO authDAO, DataAccess.UserDAO userDAO, DataAccess.GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
    }

    public NewGameResponse newGameRequest(GameData gameData) throws ResponseException {
        if (gameData.gameName() != null && gameData.gameName().matches("^[a-zA-Z0-9]+$")) {
            var newGame = gameDAO.createGame(gameData);
            return new NewGameResponse(newGame.gameId());
        } else {
            throw new ResponseException(400, "Error: bad request");
        }
    }

    public ListGamesResponse listGamesRequest() {
        var gameList = gameDAO.getGames();
        return new ListGamesResponse(gameList);
    }

    public void joinGame(JoinGameRequest request, String username) throws ResponseException {
        var existingGame = gameDAO.getGame(Integer.parseInt(request.gameId()));

        if (existingGame == null) {
            throw new ResponseException(400, "Error: bad request");
        }

        if (Objects.equals(request.playerColor(), "WHITE")) {
            if (!Objects.equals(existingGame.whiteUsername(), "")) {
                throw new ResponseException(403, "Error: already taken");
            } else {
                var modGame = new GameData(existingGame.gameId(), username, existingGame.blackUsername(), existingGame.gameName(), existingGame.game());
                gameDAO.updateGame(modGame);
            }
        } else if (Objects.equals(request.playerColor(), "BLACK")) {
            if (!Objects.equals(existingGame.blackUsername(), "")) {
                throw new ResponseException(403, "Error: already taken");
            } else {
                var modGame = new GameData(existingGame.gameId(), existingGame.whiteUsername(), username, existingGame.gameName(), existingGame.game());
                gameDAO.updateGame(modGame);
            }
        } else {
            throw new ResponseException(400, "Error: bad request");
        }
    }
}
