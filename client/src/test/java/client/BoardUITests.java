package client;

import chess.ChessBoard;
import chess.ChessGame;
import ui.BoardUI;
import org.junit.jupiter.api.*;

public class BoardUITests {

    @Test
    public void testPrintBoardWhitePerspective() {
        var board = new ChessBoard();
        board.resetBoard();
        BoardUI.drawBoard(board, ChessGame.TeamColor.WHITE);

        Assertions.assertTrue(true);
    }

    @Test
    public void testPrintBoardBlackPerspective() {
        var board = new ChessBoard();
        board.resetBoard();
        BoardUI.drawBoard(board, ChessGame.TeamColor.BLACK);

        Assertions.assertTrue(true);
    }

}
