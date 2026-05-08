package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KingMoves implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        //Set an array of where the queen can move
        Collection<ChessMove> moves = new ArrayList<>();

        //get necessary piece values and locations
        ChessPiece piece = board.getPiece(myPosition);

        //Set directions that KING can move in array (diagonal and in straight line 1 space)
        int[][] kingDir = {{-1, 1}, {0, 1}, {1, 1}, {-1, 0}, {1, 0}, {-1, -1}, {0, -1}, {1, -1}};

        KingKnightMove.slidingMoves(
                board,
                myPosition,
                kingDir,
                moves,
                piece.getTeamColor()
        );
        return moves;
    }
}