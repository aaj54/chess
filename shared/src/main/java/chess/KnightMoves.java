package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMoves implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition)
    {
        //Set an array of where the knight can move
        Collection<ChessMove> moves = new ArrayList<>();

        //get necessary piece values and locations
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPiece piece = board.getPiece(myPosition);
        ChessGame.TeamColor currentPlayerColor = piece.getTeamColor();

        //Set directions that KNIGHT can move in array (2 on dir 1 otehr direct)
        int[][] knightDir = {{-1,2}, {1,2}, {-2,1}, {2,1}, {-2,-1}, {2,-1}, {-1,-2}, {1,-2}};

        //For loop to loop though movement options
        for (int[] dir : knightDir)
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
