package service;

import dataaccess.DataAccess;
import dataaccess.MySqlDataAccess;
import model.GameData;
import model.JoinGameRequest;
import model.ListGamesResponse;
import model.NewGameResponse;
import model.ResponseException;

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

        GameData currGame = null;
        try {
            currGame = gameDAO.getGame(Integer.parseInt(request.gameID()));
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }
        if (currGame == null) {
            throw new ResponseException(400, "Error: bad request");
        }

        if (Objects.equals(request.playerColor(), "WHITE")) {
            if (currGame.whiteUsername() != null && !currGame.whiteUsername().equals(username)) {
                throw new ResponseException(403, "Error: already taken");
            } else {
                var modGame = new GameData(currGame.gameID(), username, currGame.blackUsername(), currGame.gameName(), currGame.game());
                try {
                    gameDAO.updateGame(modGame);
                } catch (dataaccess.DataAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (Objects.equals(request.playerColor(), "BLACK")) {
            if (currGame.blackUsername() != null && !currGame.blackUsername().equals(username)) {
                throw new ResponseException(403, "Error: already taken");
            } else {
                var modGame = new GameData(currGame.gameID(), currGame.whiteUsername(), username, currGame.gameName(), currGame.game());
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

    public GameData getGame(int gameID) throws ResponseException {
        GameData currGame = null;
        try {
            currGame = gameDAO.getGame(gameID);
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }
        if (currGame == null) {
            throw new ResponseException(400, "Error: bad request");
        }
        return currGame;
    }

    public void updateGame(GameData gameData) throws ResponseException {
        try {
            gameDAO.updateGame(gameData);
        } catch (dataaccess.DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
