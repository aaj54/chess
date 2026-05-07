package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentPlayerTurn;

    public ChessGame() {

        //set up board and set turn
        board = new ChessBoard();
        board.resetBoard();
        currentPlayerTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn()
    {
        return  currentPlayerTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team)
    {
        currentPlayerTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        //Get the first piece
        ChessPiece piece = board.getPiece(startPosition);

        //if no piece selected return nothing
        if (piece == null)
        {
            return null;
        }

        //set collection of moves and of valid moves
        Collection<ChessMove> allMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove mov: allMoves)
        {
            //copy board
            ChessBoard copy = copyBoard(board);
            ChessPiece movingPiece = copy.getPiece(mov.getStartPosition());

            copy.addPiece(mov.getEndPosition(), movingPiece);
            copy.addPiece(mov.getStartPosition(), null);

            ChessGame tempGame = new ChessGame();
            tempGame.setBoard(copy);

            if (!tempGame.isInCheck(piece.getTeamColor())) {
                validMoves.add(mov);
            }
        }


        return validMoves;
    }

    public ChessBoard copyBoard(ChessBoard originalBoard) {
        ChessBoard copy = new ChessBoard();

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition currentPiecePos = new ChessPosition(row, col);
                ChessPiece piece = originalBoard.getPiece(currentPiecePos);

                if (piece != null) {
                    copy.addPiece(currentPiecePos, piece); // may need deep copy depending on your design
                }
            }
        }

        return copy;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */

    public void makeMove(ChessMove move) throws InvalidMoveException {
        //get start and stop position
        ChessPosition start = move.getStartPosition();
        ChessPosition stop = move.getEndPosition();

        //get the piece at the start location
        ChessPiece piece = board.getPiece(start);

        //if no piece was chosen throw exception
        if(piece == null)
        {
            throw new InvalidMoveException();
        }

        //if not your piece
        if (piece.getTeamColor() != currentPlayerTurn) {
            throw new InvalidMoveException();
        }

        //make collection on avaliable moves
        Collection<ChessMove> availableMoves = validMoves(start);

        //if no avaliable moves throw error
        if (availableMoves == null || !availableMoves.contains(move)) {
            throw new InvalidMoveException();
        }

        //enable move piece by clearing the start pos and puting the orig piece in stop pos
        board.addPiece(stop, piece);
        board.addPiece(start, null);

        // switch player turns
        currentPlayerTurn = (currentPlayerTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(getBoard(), chessGame.getBoard()) && currentPlayerTurn == chessGame.currentPlayerTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBoard(), currentPlayerTurn);
    }
}
