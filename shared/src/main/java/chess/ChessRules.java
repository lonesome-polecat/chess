package chess;

import java.util.*;

public class ChessRules {
    private HashMap<ChessGame.TeamColor, HashSet<ChessPosition>> teamAttackVectors = new HashMap<>();

    public ChessRules() {
        // This teamAttackVectors property is just for checking if the King is in check
        teamAttackVectors.put(ChessGame.TeamColor.WHITE, new HashSet<>());
        teamAttackVectors.put(ChessGame.TeamColor.BLACK, new HashSet<>());
    }

    private String getPieceRules(ChessPiece.PieceType type) {
        return switch (type) {
            case KING -> "111k";
            case QUEEN -> "111q";
            case BISHOP -> "001b";
            case KNIGHT -> "000n";
            case ROOK -> "110r";
            case PAWN -> "101p";
        };
    }

    public ArrayList<ChessMove> getTeamMoves(ChessBoard board, ChessGame.TeamColor color) {
        var teamMoves = new ArrayList<ChessMove>();
        for (int i = 1; i <= board.boardSize; i++) {
            for (int j = 1; j <= board.boardSize; j++) {
                var position = new ChessPosition(i, j);
                var piece = board.getPiece(position);
                if (piece != null) {
                    if (piece.getTeamColor() == color) {
                        var pieceMoves = getMoves(board, position);
                        teamMoves.addAll(pieceMoves);
                    }
                }
            }
        }
        return teamMoves;
    }

    public ArrayList<ChessMove> getMoves(ChessBoard board, ChessPosition startPos) {
        // First get typical movements without accounting for king in check
        var validMoves = getBasicMoves(board, startPos);
        var color = board.getPiece(startPos).getTeamColor();
        var iter = validMoves.iterator();
        // Now check each move to see if it is still valid when taking the king into consideration
        while (iter.hasNext()) {
            var move = iter.next();
            var whatIfBoard = new ChessBoard(board);
            whatIfBoard.movePiece(move);
            if (isKingInCheck(color, whatIfBoard)) {
                iter.remove();
            }
        }
        return validMoves;
    }

    public ArrayList<ChessMove> getBasicMoves(ChessBoard board, ChessPosition startPos) {
        // Get typical movements for a piece regardless of king in check or not
        var rules = this.getPieceRules(board.getPiece(startPos).getPieceType());
        var validMoves = new ArrayList<ChessMove>();

        // first get rules for special characters
        if (rules.charAt(3) == 'k') {
            getKingMoves(validMoves, board, startPos);
            return validMoves;
        }
        if (rules.charAt(3) == 'p') {
            getPawnMoves(validMoves, board, startPos);
            return validMoves;
        }
        if (rules.charAt(3) == 'n') {
            getKnightMoves(validMoves, board, startPos);
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

    public void getKingMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int startPosRow = startPos.getRow();
        int startPosColumn = startPos.getColumn();
        var color = board.getPiece(startPos).getTeamColor();
        var attackVectors =  new HashSet<ChessPosition>();
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
                    attackVectors.add(possiblePos);
                } else {
                    // Only add possible move if the piece on square is an enemy piece
                    if (pieceOnSquare.getTeamColor() != color) {
                        validMoves.add(new ChessMove(startPos, possiblePos, null));
                        attackVectors.add(possiblePos);
                    } else {
                        attackVectors.add(possiblePos);
                    }
                }
            }
        }
        teamAttackVectors.get(color).addAll(attackVectors);
    }

    public void getKnightMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();
        var color = board.getPiece(startPos).getTeamColor();

        var colSpace = 0;
        for (int i = -2; i <= 2; i++) {
            if (row + i < 1 || row + i > 8) {
                continue;
            }
            if (i == 0) {
                continue;
            }
            if (i % 2 == 0) {
                colSpace = 1;
            } else {
                colSpace = 2;
            }
            // first check right-side possible space
            if (col + colSpace < 9) {
                var possiblePos = new ChessPosition(row+i, col+colSpace);
                setValidKnightMoves(possiblePos, validMoves, color, board, startPos);
            }
            // then check left-side possible space
            if (col - colSpace > 0) {
                var possiblePos = new ChessPosition(row+i, col-colSpace);
                setValidKnightMoves(possiblePos, validMoves, color, board, startPos);
            }
        }
    }

    public void setValidKnightMoves(ChessPosition pos, ArrayList<ChessMove> moves, ChessGame.TeamColor clr, ChessBoard brd, ChessPosition start) {
        ChessPiece pieceOnSquare = brd.getPiece(pos);
        if (pieceOnSquare == null) {
            moves.add(new ChessMove(start, pos, null));
            teamAttackVectors.get(clr).add(pos);
        } else {
            // Only add possible move if the piece on square is an enemy piece
            if (pieceOnSquare.getTeamColor() != clr) {
                moves.add(new ChessMove(start, pos, null));
                teamAttackVectors.get(clr).add(pos);
            } else {
                teamAttackVectors.get(clr).add(pos);
            }
        }
    }

    public void getPawnMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();
        var color = board.getPiece(startPos).getTeamColor();
        int farSide;
        int homeRow;
        if (color == ChessGame.TeamColor.WHITE) {
            farSide = 8;
            homeRow = 1;
        } else {
            farSide = 1;
            homeRow = 8;
        }

        var promotionPieces = new ArrayList<ChessPiece.PieceType>();
        if (getDifference(row, 1, color) == farSide) {
            promotionPieces.add(ChessPiece.PieceType.QUEEN);
            promotionPieces.add(ChessPiece.PieceType.BISHOP);
            promotionPieces.add(ChessPiece.PieceType.ROOK);
            promotionPieces.add(ChessPiece.PieceType.KNIGHT);
        } else {
            promotionPieces.add(null);
        }
        // check forward and forward-diagonals one space
        // first check move forward (1 or 2 spaces)
        var possiblePos = new ChessPosition(getDifference(row, 1, color), col);
        ChessPiece pieceOnSquare = board.getPiece(possiblePos);
        if (pieceOnSquare == null) {
            for (var promotionPiece : promotionPieces) {
                validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
            }
            if (row == getDifference(homeRow, 1, color)) {
                possiblePos = new ChessPosition(getDifference(row, 2, color), col);
                pieceOnSquare = board.getPiece(possiblePos);
                if (pieceOnSquare == null) {
                    for (var promotionPiece : promotionPieces) {
                        validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
                    }
                }
            }
        }
        // then check left-diagonal attack
        if (col > 1) {
            possiblePos = new ChessPosition(getDifference(row, 1, color), col-1);
            setPawnMvs(possiblePos, validMoves, board, startPos, promotionPieces);
        }
        // then check right-diagonal
        if (col < 8) {
            possiblePos = new ChessPosition(getDifference(row, 1, color), col+1);
            setPawnMvs(possiblePos, validMoves, board, startPos, promotionPieces);
        }
    }

    public int getDifference(int row, int offset, ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE) {
            return row + offset;
        } else {
            return row - offset;
        }
    }

    public void setPawnMvs(ChessPosition pos, ArrayList<ChessMove> mvs, ChessBoard brd, ChessPosition start, ArrayList<ChessPiece.PieceType> promos) {
        var pieceOnSquare = brd.getPiece(pos);
        var color = brd.getPiece(start).getTeamColor();
        if (pieceOnSquare != null) {
            // Only add possible move if the piece on square is an enemy piece
            if (pieceOnSquare.getTeamColor() != color) {
                for (var promotionPiece : promos) {
                    mvs.add(new ChessMove(start, pos, promotionPiece));
                }
                teamAttackVectors.get(color).add(pos);
            } else {
                teamAttackVectors.get(color).add(pos);
            }
        }
    }

    public void getForwardBackMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();
        var color = board.getPiece(startPos).getTeamColor();
        var keepMovingThisDirection = true;

        // first check down
        while (row > 1 && keepMovingThisDirection) {
            row -= 1;
            keepMovingThisDirection = setValidMoves(row, col, validMoves, color, board, startPos);
        }
        row = startPos.getRow();
        keepMovingThisDirection = true;
        // then check up
        while (row < 8 && keepMovingThisDirection) {
            row += 1;
            keepMovingThisDirection = setValidMoves(row, col, validMoves, color, board, startPos);
        }
    }

    public void getRightLeftMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();
        var color = board.getPiece(startPos).getTeamColor();
        var keepMovingThisDirection = true;

        // first check left
        while (col > 1 && keepMovingThisDirection) {
            col -= 1;
            keepMovingThisDirection = setValidMoves(row, col, validMoves, color, board, startPos);
        }
        col = startPos.getColumn();
        keepMovingThisDirection = true;
        // then check right
        while (col < 8 && keepMovingThisDirection) {
            col += 1;
            keepMovingThisDirection = setValidMoves(row, col, validMoves, color, board, startPos);
        }
    }

    public void getDiagonalMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();
        var color = board.getPiece(startPos).getTeamColor();
        var keepMovingThisDirection = true;

        // first check down-left
        while (row > 1 && col > 1 && keepMovingThisDirection) {
            row -= 1;
            col -= 1;
            keepMovingThisDirection = setValidMoves(row, col, validMoves, color, board, startPos);
        }
        row = startPos.getRow();
        col = startPos.getColumn();
        keepMovingThisDirection = true;
        // then check down-right
        while (row > 1 && col < 8 && keepMovingThisDirection) {
            row -= 1;
            col += 1;
            keepMovingThisDirection = setValidMoves(row, col, validMoves, color, board, startPos);
        }
        row = startPos.getRow();
        col = startPos.getColumn();
        keepMovingThisDirection = true;
        // then check up-left
        while (row < 8 && col > 1 && keepMovingThisDirection) {
            row += 1;
            col -= 1;
            keepMovingThisDirection = setValidMoves(row, col, validMoves, color, board, startPos);
        }
        row = startPos.getRow();
        col = startPos.getColumn();
        keepMovingThisDirection = true;
        // then check up-right
        while (row < 8 && col < 8 && keepMovingThisDirection) {
            row += 1;
            col += 1;
            keepMovingThisDirection = setValidMoves(row, col, validMoves, color, board, startPos);
        }
    }

    private void updateTeamAttackVectors(ChessGame.TeamColor teamColor, ChessBoard board) {
        // Clear attackVectors and reset
        teamAttackVectors.get(teamColor).clear();
        for (int i = 1; i <= board.boardSize; i++) {
            for (int j = 1; j <= board.boardSize; j++) {
                var position = new ChessPosition(i, j);
                var piece = board.getPiece(position);
                if (piece != null) {
                    if (piece.getTeamColor() == teamColor) {
                        this.getBasicMoves(board, position);
                    }
                }
            }
        }
    }

    public boolean isKingInCheck(ChessGame.TeamColor teamColor, ChessBoard board) {
        ChessGame.TeamColor enemyColor;
        if (teamColor == ChessGame.TeamColor.WHITE) {
            enemyColor = ChessGame.TeamColor.BLACK;
        } else {
            enemyColor = ChessGame.TeamColor.WHITE;
        }

        updateTeamAttackVectors(enemyColor, board);

        ChessPosition kingPosition = board.getSpecificPiecePosition(ChessPiece.PieceType.KING, teamColor);
        return teamAttackVectors.get(enemyColor).contains(kingPosition);
    }

    public boolean setValidMoves(int row, int col, ArrayList<ChessMove> validMoves, ChessGame.TeamColor color, ChessBoard brd, ChessPosition start) {
        var possiblePos = new ChessPosition(row, col);
        ChessPiece pieceOnSquare = brd.getPiece(possiblePos);
        if (pieceOnSquare == null) {
            validMoves.add(new ChessMove(start, possiblePos, null));
            teamAttackVectors.get(color).add(possiblePos);
        } else {
            // Only add possible move if the piece on square is an enemy piece
            if (pieceOnSquare.getTeamColor() != color) {
                validMoves.add(new ChessMove(start, possiblePos, null));
                teamAttackVectors.get(color).add(possiblePos);
            } else {
                teamAttackVectors.get(color).add(possiblePos);
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessRules that = (ChessRules) o;
        return Objects.equals(teamAttackVectors, that.teamAttackVectors);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(teamAttackVectors);
    }
}
