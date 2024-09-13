package chess;

import java.util.ArrayList;

public class ChessRules {

    public static ArrayList<ChessMove> getMovesFromRules(String rules, ChessBoard board, ChessPosition startPos) {

        var validMoves = new ArrayList<ChessMove>();

        // first get rules for special characters
        if (rules.charAt(3) == 'k') {
            getKingMoves(validMoves, board, startPos);
            return validMoves;
        }
        if (rules.charAt(3) == 'p') {
            ChessGame.TeamColor pieceColor = board.getPiece(startPos).getTeamColor();
            if (pieceColor == ChessGame.TeamColor.WHITE) {
                getWhitePawnMoves(validMoves, board, startPos);
            } else if (pieceColor == ChessGame.TeamColor.BLACK) {
                getBlackPawnMoves(validMoves, board, startPos);
            }
            return validMoves;
        }
        if (rules.charAt(3) == 'n') {
            // getKnightRules(validMoves, board, startPos);
            return validMoves;
        }

        // now check for more normal character movement
        // check front/back movement
        if (rules.charAt(0) == '1') {
            getForwardBackMoves(validMoves, board, startPos);
        }
        // Check left/right movement
        if (rules.charAt(1) == '1') {
            getRightLeftMoves(validMoves, board, startPos);
        }
        if (rules.charAt(2) == '1') {
            getDiagonalMoves(validMoves, board, startPos);
        }

        return validMoves;
    }

    public static void getKingMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int startPosRow = startPos.getRow();
        int startPosColumn = startPos.getColumn();
        // check all adjacent squares
        for (int row = startPosRow-1; row < startPosRow+2; row++) {
            if (row < 1 || row > 8) {
                continue;
            }
            for (int col = startPosColumn-1; col < startPosColumn+2; col++) {
                if (col < 1 || col > 8) {
                    continue;
                }
                if (row == startPosRow && col == startPosColumn) {
                    continue;
                }
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
    }

    public static void getWhitePawnMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();

        ChessPiece.PieceType promotionPiece = null;
        if (row + 1 == 8) {
            // implement this later
            // promotionPiece = new ChessPiece();
        }
        // check forward and forward-diagonals one space
        // first check move forward (1 or 2 spaces)
        var possiblePos = new ChessPosition(row+1, col);
        ChessPiece pieceOnSquare = board.getPiece(possiblePos);
        // System.out.printf("possiblePos = %s%n", possiblePos);
        // System.out.printf("pieceOnSquare = %s%n", pieceOnSquare.toString());
        if (pieceOnSquare == null) {
            validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
            if (row == 2) {
                possiblePos = new ChessPosition(row+2, col);
                pieceOnSquare = board.getPiece(possiblePos);
                if (pieceOnSquare == null) {
                    validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
                }
            }
        }
        // then check left-diagonal attack
        if (col > 1) {
            possiblePos = new ChessPosition(row+1, col-1);
            pieceOnSquare = board.getPiece(possiblePos);
            if (pieceOnSquare != null) {
                // Only add possible move if the piece on square is an enemy piece
                if (pieceOnSquare.getTeamColor() != board.getPiece(startPos).getTeamColor()) {
                    validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
                }
            }
        }
        // then check right-diagonal
        if (col < 8) {
            possiblePos = new ChessPosition(row+1, col+1);
            pieceOnSquare = board.getPiece(possiblePos);
            if (pieceOnSquare != null) {
                // Only add possible move if the piece on square is an enemy piece
                if (pieceOnSquare.getTeamColor() != board.getPiece(startPos).getTeamColor()) {
                    validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
                }
            }
        }
    }


    public static void getBlackPawnMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();

        ChessPiece.PieceType promotionPiece = null;
        if (row - 1 == 1) {
            // implement this later
            // promotionPiece = new ChessPiece();
        }
        // check forward and forward-diagonals one space
        // first check move forward (1 or 2 spaces)
        var possiblePos = new ChessPosition(row-1, col);
        ChessPiece pieceOnSquare = board.getPiece(possiblePos);
        // System.out.printf("possiblePos = %s%n", possiblePos);
        // System.out.printf("pieceOnSquare = %s%n", pieceOnSquare.toString());
        if (pieceOnSquare == null) {
            validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
            if (row == 7) {
                possiblePos = new ChessPosition(row-2, col);
                pieceOnSquare = board.getPiece(possiblePos);
                if (pieceOnSquare == null) {
                    validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
                }
            }
        }
        // then check left-diagonal attack
        if (col > 1) {
            possiblePos = new ChessPosition(row-1, col-1);
            pieceOnSquare = board.getPiece(possiblePos);
            if (pieceOnSquare != null) {
                // Only add possible move if the piece on square is an enemy piece
                if (pieceOnSquare.getTeamColor() != board.getPiece(startPos).getTeamColor()) {
                    validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
                }
            }
        }
        // then check right-diagonal
        if (col < 8) {
            possiblePos = new ChessPosition(row-1, col+1);
            pieceOnSquare = board.getPiece(possiblePos);
            if (pieceOnSquare != null) {
                // Only add possible move if the piece on square is an enemy piece
                if (pieceOnSquare.getTeamColor() != board.getPiece(startPos).getTeamColor()) {
                    validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
                }
            }
        }
    }

    public static void getForwardBackMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();
        // first check down
        while (row > 1) {
            row -= 1;
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
        // then check up
        while (row < 8) {
            row += 1;
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

    public static void getRightLeftMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();
        // first check left
        while (col > 1) {
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
        col = startPos.getColumn();
        // then check right
        while (col < 8) {
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

    public static void getDiagonalMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

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
}
