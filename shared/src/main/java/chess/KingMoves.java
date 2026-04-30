package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KingMoves implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition)
    {
        //Set an array of where the queen can move
        Collection<ChessMove> moves = new ArrayList<>();

        //get necessary piece values and locations
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPiece piece = board.getPiece(myPosition);
        ChessGame.TeamColor currentPlayerColor = piece.getTeamColor();

        //Set directions that KING can move in array (diagonal and in straight line 1 space)
        int[][] kingDir = {{-1,1}, {0,1}, {1,1}, {-1,0}, {1,0}, {-1,-1}, {0,-1}, {1,-1}};

        //For loop to loop though movement options
        for (int[] dir : kingDir)
        {
            int r = row + dir[0];
            int c = col + dir[1];

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
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
            //Enemy in the way
            else
            {
                if(newSpot.getTeamColor() != currentPlayerColor)
                {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
        return moves;
    }
}