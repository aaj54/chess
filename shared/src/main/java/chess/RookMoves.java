package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookMoves implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        //Set an array of where the bishop can move
        Collection<ChessMove> move = new ArrayList<>();

        //get necessary piece values and locations
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPiece piece = board.getPiece(myPosition);
        ChessGame.TeamColor currentPlayer = piece.getTeamColor();

        //Set directions that ROOK can move in array (HOR and VIR)
        int[][] dirVirHor = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        //For loop to loop though movement options

        // see if allowed continuous movement in this direction within the board


        //set new position


        //check for empty spot or opposite pawn

    }
}