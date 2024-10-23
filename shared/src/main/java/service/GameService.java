package service;

import dataaccess.DataAccess;
import model.GameData;
import server.ResponseException;

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

}
