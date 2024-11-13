package ui;

import chess.ChessBoard;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class BoardUI {

    public static void drawBoard(ChessBoard board) {

        for (int i = 1; i <= board.boardSize; i++) {
            for (int j = 1; j <= board.boardSize; j++) {
                System.out.printf(SET_BG_COLOR_WHITE);
//                var pos = new ChessPosition(i, j);
//                var piece = board.getPiece(pos);
            }
        }

    }
}
