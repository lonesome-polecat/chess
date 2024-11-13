package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;

import static ui.EscapeSequences.*;

public class BoardUI {

    private static PrintStream out = System.out;
    private static String lightBGColor = SET_BG_COLOR_GREEN;
    private static String darkBGColor = SET_BG_COLOR_DARK_GREEN;
    private static String borderColor = SET_BG_COLOR_LIGHT_GREY;
    private static String[] ROW_NUMBERS = {"1", "2", "3", "4", "5", "6", "7", "8"};
    private static String[] COL_LETTERS = {"h", "g", "f", "e", "d", "c", "b", "a"};

    private enum TileColor {
        WHITE,
        BLACK
    }

    public static void drawBoard(ChessBoard board, ChessGame.TeamColor teamColor) {
        TileColor tileColor = TileColor.WHITE;
        for (int i = 1; i <= board.boardSize+2; i++) {
            if (i == 1) {
                // print headers
                printHeaders(teamColor);
                continue;
            }
            if (i == 10) {
                // print footers
                printHeaders(teamColor);
                continue;
            }
            printSideBorder(i, teamColor);
            for (int j = 1; j <= board.boardSize; j++) {
                if (j != 1) {
                    tileColor = switchTileColor(tileColor);
                }
                setTileColor(tileColor);
                var pos = new ChessPosition(i-1, j);
                var piece = board.getPiece(pos);
                String icon = " ";
                if (piece != null) {
                    icon = getIconFromPieceType(piece.getPieceType());
                }
                out.printf(" %s ", icon);
            }
            printSideBorder(i, teamColor);
            out.println(RESET_BG_COLOR);
        }
        out.println(RESET_BG_COLOR);
    }

    private static void printHeaders(ChessGame.TeamColor teamColor) {
        out.print(borderColor);
        out.print("   ");
        if (teamColor == ChessGame.TeamColor.WHITE) {
            for (int i = 0; i < COL_LETTERS.length; i++) {
                out.printf(" %s ", COL_LETTERS[i]);
            }
        } else {
            for (int i = COL_LETTERS.length-1; i >= 0; i--) {
                out.printf(" %s ", COL_LETTERS[i]);
            }
        }
        out.print("   ");
        out.println(RESET_BG_COLOR);
    }

    private static void printSideBorder(int index, ChessGame.TeamColor teamColor) {
        // Index will come in as 2-9, need to adjust for that
        out.print(borderColor);
        if (teamColor == ChessGame.TeamColor.WHITE) {
            out.printf(" %s ", ROW_NUMBERS[index-2]);
        } else {
            out.printf(" %s ", ROW_NUMBERS[9-index]);
        }
    }

    private static TileColor switchTileColor(TileColor tileColor) {
        if (tileColor == TileColor.WHITE) {
            tileColor = TileColor.BLACK;
        } else if (tileColor == TileColor.BLACK) {
            tileColor = TileColor.WHITE;
        }
        return tileColor;
    }

    private static void setTileColor(TileColor tileColor) {
        if (tileColor == TileColor.WHITE) {
            out.printf(lightBGColor);
        } else if (tileColor == TileColor.BLACK) {
            out.printf(darkBGColor);
        }
    }

    private static String getIconFromPieceType(ChessPiece.PieceType type) {
        return switch(type) {
            case KING -> "K";
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case ROOK -> "R";
            case PAWN -> "P";
        };
    }
}
