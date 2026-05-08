package chess;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMoves implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        //Set an array of where the rook can move
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(myPosition);


        //Set directions that BISHOP can move in array (diagonal)
        int[][] diagMoves = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        BishopRookQueenMove.slidingMoves(
                board,
                myPosition,
                diagMoves,
                moves,
                piece.getTeamColor()
        );

        return moves;
    }
}
