package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;

import static ui.EscapeSequences.*;

public class BoardUI {

    private static final PrintStream OUT = System.out;
    private static final String LIGHT_BG_COLOR = SET_BG_COLOR_ORANGE;
    private static final String DARK_BG_COLOR = SET_BG_COLOR_DARK_GREEN;
    private static final String BORDER_BG_COLOR = SET_BG_COLOR_DARK_GREY;

    private static final String BORDER_TEXT_COLOR = SET_TEXT_COLOR_LIGHT_GREY;
    private static final String WHITE_TEAM_COLOR = SET_TEXT_COLOR_WHITE;
    private static final String BLACK_TEAM_COLOR = SET_TEXT_COLOR_BLACK;

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
            OUT.println(RESET_BG_COLOR);
        }
        OUT.println(RESET_BG_COLOR);
    }

    private static void printHeaders(ChessGame.TeamColor teamColor) {
        OUT.print(BORDER_BG_COLOR + BORDER_TEXT_COLOR);
        OUT.print("   ");
        if (teamColor == ChessGame.TeamColor.WHITE) {
            for (int i = 0; i < COL_LETTERS.length; i++) {
                OUT.printf(" %s ", COL_LETTERS[i]);
            }
        } else {
            for (int i = COL_LETTERS.length-1; i >= 0; i--) {
                OUT.printf(" %s ", COL_LETTERS[i]);
            }
        }
        OUT.print("   ");
        OUT.println(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private static void printSideBorder(int index, ChessGame.TeamColor teamColor) {
        // Index will come in as 2-9, need to adjust for that
        OUT.print(BORDER_BG_COLOR + BORDER_TEXT_COLOR);
        if (teamColor == ChessGame.TeamColor.WHITE) {
            OUT.printf(" %s ", ROW_NUMBERS[index-2]);
        } else {
            OUT.printf(" %s ", ROW_NUMBERS[9-index]);
        }
    }

    private static void printPiece(int iIndex, int jIndex, ChessGame.TeamColor teamColor, ChessBoard board) {
        // i_index comes in as 2-9
        // j_index comes in as 1-8
        // Compensate as need be
        ChessPosition pos;
        if (teamColor == ChessGame.TeamColor.WHITE) {
            pos = new ChessPosition(10-iIndex, 9-jIndex);
        } else {
            pos = new ChessPosition(iIndex-1, jIndex);
        }
        var piece = board.getPiece(pos);
        String icon = " ";
        if (piece != null) {
            var pieceTeamColor = piece.getTeamColor();
            if (pieceTeamColor == ChessGame.TeamColor.WHITE) {
                OUT.print(WHITE_TEAM_COLOR);
            } else {
                OUT.print(BLACK_TEAM_COLOR);
            }
            icon = getIconFromPieceType(piece.getPieceType());
        }
        OUT.printf(" %s ", icon);
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
            OUT.printf(LIGHT_BG_COLOR);
        } else if (tileColor == TileColor.BLACK) {
            OUT.printf(DARK_BG_COLOR);
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
