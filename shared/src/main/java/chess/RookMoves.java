package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookMoves implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition)
    {

        //Set an array of where the bishop can move
        Collection<ChessMove> moves = new ArrayList<>();

        //get necessary piece values and locations
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPiece piece = board.getPiece(myPosition);
        ChessGame.TeamColor currentPlayerColor = piece.getTeamColor();

        //Set directions that ROOK can move in array (HOR and VIR)
        int[][] straightMoves = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        //For loop to loop though movement options
        for (int[] straMov : straightMoves) {
            //Update direction movement
            int r = row + straMov[0];
            int c = col + straMov[1];

            // see if allowed continuous movement in this direction within the board
            while (!(r<1) && !(r>8) && !(c<1) && !(c>8))
            {
                //set new position
                ChessPosition newPosition = new ChessPosition(r,c);
                ChessPiece newSpot = board.getPiece(newPosition);

                //check for empty spot or opposite pawn
                if(newSpot == null)
                {
                    //add new location and piece to moves
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    //allow for continuous movement
                    r += straMov[0];
                    c += straMov[1];
                }
                //Enemy in the way
                else
                {
                    if(newSpot.getTeamColor() != currentPlayerColor)
                    {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break;
                }
            }
        }
        return moves;
    }
}