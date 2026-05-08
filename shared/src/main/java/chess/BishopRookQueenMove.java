package chess;

import java.util.Collection;

public class BishopRookQueenMove {
    public static void slidingMoves(
            ChessBoard board,
            ChessPosition start,
            int[][] directions,
            Collection<ChessMove> moves,
            ChessGame.TeamColor color
    ) {
        for (int[] dir : directions) {
            int r = start.getRow() + dir[0];
            int c = start.getColumn() + dir[1];

            while (r >= 1 && r <= 8 && c >= 1 && c <= 8) {

                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece piece = board.getPiece(pos);

                if (piece == null) {
                    moves.add(new ChessMove(start, pos, null));
                } else {
                    if (piece.getTeamColor() != color) {
                        moves.add(new ChessMove(start, pos, null));
                    }
                    break;
                }

                r += dir[0];
                c += dir[1];
            }
        }
    }
}
