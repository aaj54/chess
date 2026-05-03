package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoves implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        //Set an array of where the queen can move
        Collection<ChessMove> moves = new ArrayList<>();

        //get necessary piece values and locations
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPiece piece = board.getPiece(myPosition);
        ChessGame.TeamColor currentPlayerColor = piece.getTeamColor();

        //Look to see if pawn need to move forward or backwards
        int direction = (currentPlayerColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int newSpot = row + direction;

        //Check to see if piece can capture
        if (!((newSpot) < 1) && (!(newSpot > 8)))
        {
            ChessPosition forward = new ChessPosition(newSpot, col);

            if (board.getPiece(forward) == null) {
                moves.add(new ChessMove(myPosition, forward, null));
                //Check to see if first move pawn on row 2 or 7
                if ((row == 2 && currentPlayerColor == ChessGame.TeamColor.WHITE) ||
                        (row == 7 && currentPlayerColor == ChessGame.TeamColor.BLACK)) {
                    int moveTwice = row + 2 * direction;
                    ChessPosition doubleForward = new ChessPosition(moveTwice, col);
                    if (board.getPiece(doubleForward) == null) {
                        moves.add(new ChessMove(myPosition, doubleForward, null));
                    }
                }
            }
        }
        return moves;
    }
}