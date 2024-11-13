package client;

import chess.ChessBoard;
import ui.BoardUI;
import ui.BoardUI.*;
import org.junit.jupiter.api.*;

public class BoardUITests {

    @Test
    public void testPrintBoardInitial() {
        var board = new ChessBoard();
        BoardUI.drawBoard(board);

        Assertions.assertTrue(true);
    }

}
