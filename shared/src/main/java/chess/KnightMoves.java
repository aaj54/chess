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
        ChessPiece piece = board.getPiece(myPosition);

        //Set directions that KNIGHT can move in array (2 on dir 1 other direct)
        int[][] knightDir = {{-1,2}, {1,2}, {-2,1}, {2,1}, {-2,-1}, {2,-1}, {-1,-2}, {1,-2}};

        //For loop to loop though movement options
        KingKnightMove.slidingMoves(
                board,
                myPosition,
                knightDir,
                moves,
                piece.getTeamColor()
        );

        return moves;
    }
}
