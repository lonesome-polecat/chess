package chess;

import java.util.*;

public class ChessRules {
    private HashMap<ChessGame.TeamColor, HashSet<ChessPosition>> teamAttackVectors = new HashMap<>();

    public ChessRules() {
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

    public ArrayList<ChessMove> getMovesFromRules(ChessBoard board, ChessPosition startPos) {

        var rules = this.getPieceRules(board.getPiece(startPos).getPieceType());
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
                    break;
                }
            }
        }
        teamAttackVectors.get(color).addAll(attackVectors);
    }

    public void getKnightMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();
        var color = board.getPiece(startPos).getTeamColor();
        var attackVectors =  new HashSet<ChessPosition>();

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
            // then check left-side possible space
            if (col - colSpace > 0) {
                var possiblePos = new ChessPosition(row+i, col-colSpace);
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

    public void getWhitePawnMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();
        var color = board.getPiece(startPos).getTeamColor();
        var attackVectors =  new HashSet<ChessPosition>();

        var promotionPieces = new ArrayList<ChessPiece.PieceType>();
        if (row + 1 == 8) {
            promotionPieces.add(ChessPiece.PieceType.QUEEN);
            promotionPieces.add(ChessPiece.PieceType.BISHOP);
            promotionPieces.add(ChessPiece.PieceType.ROOK);
            promotionPieces.add(ChessPiece.PieceType.KNIGHT);
        } else {
            promotionPieces.add(null);
        }
        // check forward and forward-diagonals one space
        // first check move forward (1 or 2 spaces)
        var possiblePos = new ChessPosition(row+1, col);
        ChessPiece pieceOnSquare = board.getPiece(possiblePos);
        // System.out.printf("possiblePos = %s%n", possiblePos);
        // System.out.printf("pieceOnSquare = %s%n", pieceOnSquare.toString());
        if (pieceOnSquare == null) {
            for (var promotionPiece : promotionPieces) {
                validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
            }
            if (row == 2) {
                possiblePos = new ChessPosition(row+2, col);
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
            possiblePos = new ChessPosition(row+1, col-1);
            pieceOnSquare = board.getPiece(possiblePos);
            if (pieceOnSquare != null) {
                // Only add possible move if the piece on square is an enemy piece
                if (pieceOnSquare.getTeamColor() != color) {
                    for (var promotionPiece : promotionPieces) {
                        validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
                    }
                    attackVectors.add(possiblePos);
                } else {
                    attackVectors.add(possiblePos);
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
                    for (var promotionPiece : promotionPieces) {
                        validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
                    }
                    attackVectors.add(possiblePos);
                } else {
                    attackVectors.add(possiblePos);
                }
            }
        }
        teamAttackVectors.get(color).addAll(attackVectors);
    }

    public void getBlackPawnMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();
        var color = board.getPiece(startPos).getTeamColor();
        var attackVectors =  new HashSet<ChessPosition>();

        var promotionPieces = new ArrayList<ChessPiece.PieceType>();
        if (row - 1 == 1) {
            promotionPieces.add(ChessPiece.PieceType.QUEEN);
            promotionPieces.add(ChessPiece.PieceType.BISHOP);
            promotionPieces.add(ChessPiece.PieceType.ROOK);
            promotionPieces.add(ChessPiece.PieceType.KNIGHT);
        } else {
            promotionPieces.add(null);
        }
        // check forward and forward-diagonals one space
        // first check move forward (1 or 2 spaces)
        var possiblePos = new ChessPosition(row-1, col);
        ChessPiece pieceOnSquare = board.getPiece(possiblePos);
        // System.out.printf("possiblePos = %s%n", possiblePos);
        // System.out.printf("pieceOnSquare = %s%n", pieceOnSquare.toString());
        if (pieceOnSquare == null) {
            for (var promotionPiece : promotionPieces) {
                validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
            }
            if (row == 7) {
                possiblePos = new ChessPosition(row-2, col);
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
            possiblePos = new ChessPosition(row-1, col-1);
            pieceOnSquare = board.getPiece(possiblePos);
            if (pieceOnSquare != null) {
                // Only add possible move if the piece on square is an enemy piece
                if (pieceOnSquare.getTeamColor() != board.getPiece(startPos).getTeamColor()) {
                    for (var promotionPiece : promotionPieces) {
                        validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
                    }
                    attackVectors.add(possiblePos);
                } else {
                    attackVectors.add(possiblePos);
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
                    for (var promotionPiece : promotionPieces) {
                        validMoves.add(new ChessMove(startPos, possiblePos, promotionPiece));
                    }
                    attackVectors.add(possiblePos);
                } else {
                    attackVectors.add(possiblePos);
                }
            }
        }
        teamAttackVectors.get(color).addAll(attackVectors);
    }

    public void getForwardBackMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();
        var color = board.getPiece(startPos).getTeamColor();
        var attackVectors =  new HashSet<ChessPosition>();

        // first check down
        while (row > 1) {
            row -= 1;
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
                attackVectors.add(possiblePos);
            } else {
                // Only add possible move if the piece on square is an enemy piece
                if (pieceOnSquare.getTeamColor() != color) {
                    validMoves.add(new ChessMove(startPos, possiblePos, null));
                    attackVectors.add(possiblePos);
                } else {
                    attackVectors.add(possiblePos);
                }
                break;
            }
        }
        teamAttackVectors.get(color).addAll(attackVectors);
    }

    public void getRightLeftMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();
        var color = board.getPiece(startPos).getTeamColor();
        var attackVectors =  new HashSet<ChessPosition>();

        // first check left
        while (col > 1) {
            col -= 1;
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
                attackVectors.add(possiblePos);
            } else {
                // Only add possible move if the piece on square is an enemy piece
                if (pieceOnSquare.getTeamColor() != color) {
                    validMoves.add(new ChessMove(startPos, possiblePos, null));
                    attackVectors.add(possiblePos);
                } else {
                    attackVectors.add(possiblePos);
                }
                break;
            }
        }
        teamAttackVectors.get(color).addAll(attackVectors);
    }

    public void getDiagonalMoves(ArrayList<ChessMove> validMoves, ChessBoard board, ChessPosition startPos) {

        int row = startPos.getRow();
        int col = startPos.getColumn();
        var color = board.getPiece(startPos).getTeamColor();
        var attackVectors =  new HashSet<ChessPosition>();

        // first check down-left
        while (row > 1 && col > 1) {
            row -= 1;
            col -= 1;
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
                attackVectors.add(possiblePos);
            } else {
                // Only add possible move if the piece on square is an enemy piece
                if (pieceOnSquare.getTeamColor() != color) {
                    validMoves.add(new ChessMove(startPos, possiblePos, null));
                    attackVectors.add(possiblePos);
                } else {
                    attackVectors.add(possiblePos);
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
                attackVectors.add(possiblePos);
            } else {
                // Only add possible move if the piece on square is an enemy piece
                if (pieceOnSquare.getTeamColor() != color) {
                    validMoves.add(new ChessMove(startPos, possiblePos, null));
                    attackVectors.add(possiblePos);
                } else {
                    attackVectors.add(possiblePos);
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
                attackVectors.add(possiblePos);
            } else {
                // Only add possible move if the piece on square is an enemy piece
                if (pieceOnSquare.getTeamColor() != color) {
                    validMoves.add(new ChessMove(startPos, possiblePos, null));
                    attackVectors.add(possiblePos);
                } else {
                    attackVectors.add(possiblePos);
                }
                break;
            }
        }
        teamAttackVectors.get(color).addAll(attackVectors);
    }

    private void updateTeamAttackVectors(ChessGame.TeamColor teamColor, ChessBoard board) {
        // Clear attackVectors and reset
        teamAttackVectors.get(teamColor).clear();
        for (int i = 1; i <= board.BOARD_SIZE; i++) {
            for (int j = 1; j <= board.BOARD_SIZE; j++) {
                var position = new ChessPosition(i, j);
                var piece = board.getPiece(position);
                if (piece != null) {
                    if (piece.getTeamColor() == teamColor) {
                        this.getMovesFromRules(board, position);
                    }
                }
            }
        }
        System.out.println(teamAttackVectors.get(teamColor));
    }

    public Collection<ChessMove> getKingMovesAllChecks(ChessBoard board, ChessPosition kingPos) {
        var kingColor = board.getPiece(kingPos).getTeamColor();
        var kingMoves = getMovesFromRules(board, kingPos);

        ChessGame.TeamColor enemyColor = null;

        if (kingColor == ChessGame.TeamColor.WHITE) {
            enemyColor = ChessGame.TeamColor.BLACK;
        } else {
            enemyColor = ChessGame.TeamColor.WHITE;
        }

        updateTeamAttackVectors(enemyColor, board);

        for (var move : kingMoves) {
            var pos = move.getEndPosition();
            if (teamAttackVectors.get(enemyColor).contains(pos)) {
                kingMoves.remove(move);
            }
        }
        return kingMoves;
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
}
