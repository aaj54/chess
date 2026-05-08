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
                piece.getTeamColor());

        // look to see their can be castling
        if (!piece.hasMoved()) {
            castling(board, myPosition, piece.getTeamColor(), moves, true);  // kingside
            castling(board, myPosition, piece.getTeamColor(), moves, false); // queenside
        }

        return moves;
    }

    private void castling(ChessBoard board, ChessPosition kingPos, ChessGame.TeamColor color,
                          Collection<ChessMove> moves, boolean kingside) {

        int row = kingPos.getRow();

        //look to see if on the kingside and then set to 8 or 1 for col
        int rookCol = kingside ? 8 : 1;
        ChessPiece rook = board.getPiece(new ChessPosition(row, rookCol));

        // Rook must exist be of the same team and not have moved
        if (rook == null || rook.getPieceType() != ChessPiece.PieceType.ROOK || rook.getTeamColor() != color ||
                rook.hasMoved())
        {
            return;
        }

        // See if squares between king and rook are empty
        int kingCol = kingPos.getColumn();
        int start;
        int end;

        //look for start and end spots
        if (rookCol > kingCol) {
            // kingside
            start = kingCol + 1;
            end = rookCol - 1;
        } else {
            // queenside
            start = rookCol + 1;
            end = kingCol - 1;
        }

        for (int c = start; c <= end; c++) {
            if (board.getPiece(new ChessPosition(row, c)) != null)
            {
                return;
            }
        }

        // King cannot pass through or land on a checked square
        int direction = kingside ? 1 : -1;
        for (int step = 0; step <= 2; step++) {
            if (isSquareAttacked(board, new ChessPosition(row, kingCol + step * direction), color))
            {
                return;
            }
        }

        moves.add(new ChessMove(kingPos, new ChessPosition(row, kingCol + 2 * direction), null));
    }

    private boolean isSquareAttacked(ChessBoard board, ChessPosition square, ChessGame.TeamColor color) {
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece piece = board.getPiece(pos);

                //look to see if matching color
                if (piece == null || piece.getTeamColor() == color)
                {
                    continue;
                }

                for (ChessMove move : piece.pieceMoves(board, pos)) {
                    if (move.getEndPosition().equals(square)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}