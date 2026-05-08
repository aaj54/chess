package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookMoves implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition)
    {

        //Set an array of where the rook can move
        Collection<ChessMove> moves = new ArrayList<>();

        //get necessary piece values and locations
        ChessPiece piece = board.getPiece(myPosition);

        //Set directions that ROOK can move in array (HOR and VIR)
        int[][] straightMoves = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        BishopRookQueenMove.slidingMoves(
                board,
                myPosition,
                straightMoves,
                moves,
                piece.getTeamColor()
        );
        return moves;
    }
}