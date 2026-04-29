package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMoves implements PieceMovesCalculator {
    @Override
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        //Set an array of where the bishop can move
        Collection<ChessMove> moves = new ArrayList<>();

        //get necessary piece values and locations
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPiece piece = board.getPiece(myPosition);
        ChessGame.TeamColor myColor = piece.getTeamColor();

        //Set directions that BISHOP can move in array (diagonal)
        int [][] dir = {{-1,-1}, {-1,1}, {1,-1}, {1,1}};

        //For loop to loop though movement options
        for (int[] dir: dir) {
            int r = row + dir[0];
            int c = col + dir[1];
        }

    }

}
