package chess;

import java.util.ArrayList;

public class ChessRules {

    public static ArrayList<ChessMove> getMovesFromRules(String rules, ChessBoard board, ChessPosition startPos) {

        var validMoves = new ArrayList<ChessMove>();

        // first get rules for special characters
        if (rules.charAt(3) == 'k') {
            return validMoves;
        }
        if (rules.charAt(3) == 'p') {
            return validMoves;
        }
        if (rules.charAt(3) == 'n') {
            return validMoves;
        }

        // now check for more normal character movement
        // check front/back movement
        if (rules.charAt(0) == '1') {
            // put rules here
        }
        // Check left/right movement
        if (rules.charAt(1) == '1') {
            // put rules here
        }
        if (rules.charAt(2) == '1') {
            int row = startPos.getRow();
            int col = startPos.getColumn();
            // first check down-left
            while (row > 1 && col > 1) {
                row -= 1;
                col -= 1;
                var possiblePos = new ChessPosition(row, col);
                ChessPiece pieceOnSquare = board.getPiece(possiblePos);
                if (pieceOnSquare == null) {
                    validMoves.add(new ChessMove(startPos, possiblePos, null));
                } else {
                    // Only add possible move if the piece on square is an enemy piece
                    if (pieceOnSquare.getTeamColor() != board.getPiece(startPos).getTeamColor()) {
                        validMoves.add(new ChessMove(startPos, possiblePos, null));
                    }
                    break;
                }
            }
            row = startPos.getRow();
            col = startPos.getColumn();
            // then check down-right
            while (row > 1 && col < 8) {
                row -= 1;
                col += 1;
                var possiblePos = new ChessPosition(row, col);
                ChessPiece pieceOnSquare = board.getPiece(possiblePos);
                if (pieceOnSquare == null) {
                    validMoves.add(new ChessMove(startPos, possiblePos, null));
                } else {
                    // Only add possible move if the piece on square is an enemy piece
                    if (pieceOnSquare.getTeamColor() != board.getPiece(startPos).getTeamColor()) {
                        validMoves.add(new ChessMove(startPos, possiblePos, null));
                    }
                    break;
                }
            }
            row = startPos.getRow();
            col = startPos.getColumn();
            // then check up-left
            while (row < 8 && col > 1) {
                row += 1;
                col -= 1;
                var possiblePos = new ChessPosition(row, col);
                ChessPiece pieceOnSquare = board.getPiece(possiblePos);
                if (pieceOnSquare == null) {
                    validMoves.add(new ChessMove(startPos, possiblePos, null));
                } else {
                    // Only add possible move if the piece on square is an enemy piece
                    if (pieceOnSquare.getTeamColor() != board.getPiece(startPos).getTeamColor()) {
                        validMoves.add(new ChessMove(startPos, possiblePos, null));
                    }
                    break;
                }
            }
            row = startPos.getRow();
            col = startPos.getColumn();
            // then check up-right
            while (row < 8 && col < 8) {
                row += 1;
                col += 1;
                var possiblePos = new ChessPosition(row, col);
                ChessPiece pieceOnSquare = board.getPiece(possiblePos);
                if (pieceOnSquare == null) {
                    validMoves.add(new ChessMove(startPos, possiblePos, null));
                } else {
                    // Only add possible move if the piece on square is an enemy piece
                    if (pieceOnSquare.getTeamColor() != board.getPiece(startPos).getTeamColor()) {
                        validMoves.add(new ChessMove(startPos, possiblePos, null));
                    }
                    break;
                }
            }
        }

        return validMoves;
    }
}
