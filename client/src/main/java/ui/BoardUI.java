package ui;

import chess.ChessBoard;
import chess.ChessPosition;

import java.io.PrintStream;

import static ui.EscapeSequences.*;

public class BoardUI {

    private static PrintStream out = System.out;

    private enum TileColor {
        WHITE,
        BLACK
    }

    public static void drawBoard(ChessBoard board) {
        TileColor tileColor = TileColor.BLACK;
        tileColor = setTileColor(tileColor);
        for (int i = 1; i <= board.boardSize+2; i++) {
            for (int j = 1; j <= board.boardSize+2; j++) {
                if (j < 2 || j > 9) {
                    out.printf(SET_BG_COLOR_LIGHT_GREY);
                    out.printf(" B ");
                    continue;
                } else if (j != 2) {
                    tileColor = setTileColor(tileColor);
                }
                out.printf(" %d ", j);
//                var pos = new ChessPosition(i, j);
//                var piece = board.getPiece(pos);
            }
            out.println(RESET_BG_COLOR);
        }

    }

    private static TileColor setTileColor(TileColor tileColor) {
        if (tileColor == TileColor.WHITE) {
            tileColor = TileColor.BLACK;
            out.printf(SET_BG_COLOR_BLACK);
        } else if (tileColor == TileColor.BLACK) {
            tileColor = TileColor.WHITE;
            out.printf(SET_BG_COLOR_WHITE);
        }
        return tileColor;
    }
}
