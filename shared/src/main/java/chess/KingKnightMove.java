package chess;

import java.util.Collection;

public class KingKnightMove {
    public static void slidingMoves(
            ChessBoard board,
            ChessPosition start,
            int[][] directions,
            Collection<ChessMove> moves,
            ChessGame.TeamColor color
    ) {
        //For loop to loop though movement options
        for (int[] dir : directions)
        {
            int r = start.getRow() + dir[0];
            int c = start.getColumn() + dir[1];

            // bounds check
            if (r < 1 || r > 8 || c < 1 || c > 8) {
                continue;
            }

            //set new position
            ChessPosition newPosition = new ChessPosition(r,c);
            ChessPiece newSpot = board.getPiece(newPosition);

            //check for empty spot or opposite pawn
            if(newSpot == null)
            {
                //add new location and piece to moves
                moves.add(new ChessMove(start, newPosition, null));
            }
            //Enemy in the way
            else
            {
                if(newSpot.getTeamColor() != color)
                {
                    moves.add(new ChessMove(start, newPosition, null));
                }
            }
        }
    }
}
