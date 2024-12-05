package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board = new ChessBoard();
    private TeamColor currTurn = TeamColor.WHITE;
    private ChessRules rules = new ChessRules();
    private enum GameState {
        IN_PLAY,
        GAME_OVER
    }
    private GameState gameState = GameState.IN_PLAY;
    private TeamColor winner = null;

    public ChessGame() {
        board.resetBoard();
    }

    public GameState getGameState() {
        return gameState;
    }

    public void gameOver() {
        gameState = GameState.GAME_OVER;
    }

    public void setWinner(TeamColor color) {
        winner = color;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currTurn = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && currTurn == chessGame.currTurn && Objects.equals(rules, chessGame.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currTurn, rules);
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        return rules.getMoves(board, startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        var validMoves = rules.getTeamMoves(board, currTurn);
        if (move == null || !validMoves.contains(move)) {
            throw new InvalidMoveException(String.format("%s is not a valid move", move));
        }
        board.movePiece(move);
        if (move.getPromotionPiece() != null) {
            var newPiece = new ChessPiece(currTurn, move.getPromotionPiece());
            board.addPiece(move.getEndPosition(), newPiece);
        }
        TeamColor nextTurn;
        if (currTurn == TeamColor.WHITE) {
            nextTurn = TeamColor.BLACK;
        } else {
            nextTurn = TeamColor.WHITE;
        }
        setTeamTurn(nextTurn);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return rules.isKingInCheck(teamColor, board);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        var validMoves = rules.getTeamMoves(board, teamColor);
        return validMoves.isEmpty();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        var validMoves = rules.getTeamMoves(board, teamColor);
        return !isInCheck(teamColor) && validMoves.isEmpty();
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
