package client;

import chess.ChessBoard;
import chess.ChessGame;
import ui.BoardUI;
import org.junit.jupiter.api.*;

public class BoardUITests {

    @Test
    public void testPrintBoardInitial() {
        var board = new ChessBoard();
        board.resetBoard();
        BoardUI.drawBoard(board, ChessGame.TeamColor.WHITE);
        BoardUI.drawBoard(board, ChessGame.TeamColor.BLACK);

        Assertions.assertTrue(true);
    }

}
