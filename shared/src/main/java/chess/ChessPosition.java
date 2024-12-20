package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        if (row < 1 || row > 8) {
            throw new RuntimeException("Position row is off of board");
        } else if (col < 1 || col > 8) {
            throw new RuntimeException("Position column is off of board");
        }
        this.row = row;
        this.col = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
        // throw new RuntimeException("Not implemented");
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return col;
        // throw new RuntimeException("Not implemented");
    }

    /**
     * Takes a two char String in chess notation (i.e. a2) and parses it into a ChessPosition
     * @param moveString
     * @return ChessPosition
     */
    public static ChessPosition parseStringToPosition(String moveString) {
        // IMPORTANT! The first letter is the COL and the second is the ROW (inverse)
        int[] posIndices = {0, 0};
        for (int i = 0; i < moveString.length(); i++) {
            char pos = moveString.charAt(i);
            posIndices[i] = switch (pos) {
                case 'a' -> 1;
                case 'b' -> 2;
                case 'c' -> 3;
                case 'd' -> 4;
                case 'e' -> 5;
                case 'f' -> 6;
                case 'g' -> 7;
                case 'h' -> 8;
                default -> (pos - '0');
            };
        }
        return new ChessPosition(posIndices[1], posIndices[0]);
    }

    /**
     * Takes a ChessPosition and transforms it into a String of chess notation
     * @param pos
     * @return String
     */
    public static String parsePositionToString(ChessPosition pos) {
        // IMPORTANT! The first letter is the COL and the second is the ROW (inverse)
        String positionString = "";
        var row = pos.getRow();
        var col = pos.getColumn();
        positionString = positionString + switch (col) {
                case 1 -> 'a';
                case 2 -> 'b';
                case 3 -> 'c';
                case 4 -> 'd';
                case 5 -> 'e';
                case 6 -> 'f';
                case 7 -> 'g';
                case 8 -> 'h';
            default -> 'a';
            };
        positionString = positionString + Integer.toString(row);
        return positionString;
    }

    @Override
    public String toString() {
        String strRow = Integer.toString(row);
        String strCol = Integer.toString(col);
        return String.format("[%s][%s]", strRow, strCol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        ChessPosition that = (ChessPosition) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
