package chess;

import java.util.ArrayList;
import java.util.Collection;

public class QueenMoves implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        //Set an array of where the queen can move
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(myPosition);

        //Set directions that QUEEN can move in array (diagonal and in straight lines )
        int[][] straDiagMoves = {{1, -1}, {1, 0}, {1, 1}, {0, -1}, {0, 1}, {-1, -1}, {-1, 0}, {-1, 1}};

        BishopRookQueenMove.slidingMoves(
                board,
                myPosition,
                straDiagMoves,
                moves,
                piece.getTeamColor()
        );
        return moves;
    }
}
