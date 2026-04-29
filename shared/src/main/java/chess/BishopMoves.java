package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMoves implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        //Set an array of where the bishop can move
        Collection<ChessMove> moves = new ArrayList<>();

        //get necessary piece values and locations
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPiece piece = board.getPiece(myPosition);
        ChessGame.TeamColor currentPlayerColor = piece.getTeamColor();

        //Set directions that BISHOP can move in array (diagonal)
        int [][] diagMoves = {{-1,-1}, {-1,1}, {1,-1}, {1,1}};

        //For loop to loop though movement options
        for (int[] dir: diagMoves) {
            int r = row + dir[0];
            int c = col + dir[1];

            // see if allowed continuous movement in this direction within the board
            while (!(r<1) && !(r>8) && !(c<1) && !(c>8)){

                //set new position
                ChessPosition newPosition = new ChessPosition(r, c);
                ChessPiece newSpot = board.getPiece(newPosition);

                //check for empty spot or opposite pawn
                if (newSpot == null){
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    //cont
                    r += dir[0];
                    c += dir[1];
                }
                else {
                    if (newSpot.getTeamColor() != currentPlayerColor) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }
        return moves;
    }
}
