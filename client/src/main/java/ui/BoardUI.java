package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;

import static ui.EscapeSequences.*;

public class BoardUI {

    private static final PrintStream out = System.out;
    private static final String lightBGColor = SET_BG_COLOR_ORANGE;
    private static final String darkBGColor = SET_BG_COLOR_DARK_GREEN;
    private static final String borderBGColor = SET_BG_COLOR_DARK_GREY;

    private static final String borderTextColor = SET_TEXT_COLOR_LIGHT_GREY;
    private static final String whiteTeamColor = SET_TEXT_COLOR_WHITE;
    private static final String blackTeamColor = SET_TEXT_COLOR_BLACK;

    private static final String[] ROW_NUMBERS = {"1", "2", "3", "4", "5", "6", "7", "8"};
    private static final String[] COL_LETTERS = {"h", "g", "f", "e", "d", "c", "b", "a"};

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
                printPiece(i, j, teamColor, board);
            }
            printSideBorder(i, teamColor);
            out.println(RESET_BG_COLOR);
        }
        out.println(RESET_BG_COLOR);
    }

    private static void printHeaders(ChessGame.TeamColor teamColor) {
        out.print(borderBGColor + borderTextColor);
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
        out.println(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private static void printSideBorder(int index, ChessGame.TeamColor teamColor) {
        // Index will come in as 2-9, need to adjust for that
        out.print(borderBGColor + borderTextColor);
        if (teamColor == ChessGame.TeamColor.WHITE) {
            out.printf(" %s ", ROW_NUMBERS[index-2]);
        } else {
            out.printf(" %s ", ROW_NUMBERS[9-index]);
        }
    }

    private static void printPiece(int i_index, int j_index, ChessGame.TeamColor teamColor, ChessBoard board) {
        // i_index comes in as 2-9
        // j_index comes in as 1-8
        // Compensate as need be
        ChessPosition pos;
        if (teamColor == ChessGame.TeamColor.WHITE) {
            pos = new ChessPosition(10-i_index, 9-j_index);
        } else {
            pos = new ChessPosition(i_index-1, j_index);
        }
        var piece = board.getPiece(pos);
        String icon = " ";
        if (piece != null) {
            var pieceTeamColor = piece.getTeamColor();
            if (pieceTeamColor == ChessGame.TeamColor.WHITE) {
                out.print(whiteTeamColor);
            } else {
                out.print(blackTeamColor);
            }
            icon = getIconFromPieceType(piece.getPieceType());
        }
        out.printf(" %s ", icon);
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
