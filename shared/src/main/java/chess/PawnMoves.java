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
                if ((newSpot == 8 && currentPlayerColor == ChessGame.TeamColor.WHITE) ||
                        (newSpot == 1 && currentPlayerColor == ChessGame.TeamColor.BLACK))
                {
                    addPromotionMoves(moves, myPosition, forward);
                }
                else {
                    moves.add(new ChessMove(myPosition, forward, null));
                }
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
        int capture[] = {col-1, col+1};
        for (int c : capture)
        {
            int r  = row + direction;
            if (r < 1 || r > 8 || c < 1 || c > 8) continue;

            ChessPosition captDiag = new ChessPosition(r, c);
            ChessPiece moveDiag = board.getPiece(captDiag);

            if (moveDiag != null && moveDiag.getTeamColor() != currentPlayerColor)
            {
                if ((r == 8 && currentPlayerColor == ChessGame.TeamColor.WHITE) ||
                        (r == 1 && currentPlayerColor == ChessGame.TeamColor.BLACK))
                {
                    addPromotionMoves(moves, myPosition, captDiag);
                }
                else
                {
                    moves.add(new ChessMove(myPosition, captDiag, null));
                }
            }
        }
        return moves;
    }
    private void addPromotionMoves(Collection<ChessMove> moves, ChessPosition from, ChessPosition to) {
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.QUEEN));
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(from, to, ChessPiece.PieceType.KNIGHT));
    }
}