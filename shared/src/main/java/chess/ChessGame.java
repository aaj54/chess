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
    private ChessMove lastMove;

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
            return new ArrayList<>();
        }

        //set collection of moves and of valid moves
        Collection<ChessMove> allMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        //loop through all move options
        for (ChessMove mov: allMoves)
        {
            //copy board
            ChessBoard copy = copyBoard(board);
            ChessPiece movingPiece = copy.getPiece(mov.getStartPosition());

            if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                int getColumnDifference = mov.getEndPosition().getColumn() - mov.getStartPosition().getColumn();

                //if col difference set bool
                boolean movingDiagonally = getColumnDifference != 0;
                boolean targetEmpty = board.getPiece(mov.getEndPosition()) == null;

                //add en pessant
                if (movingDiagonally && targetEmpty) {
                    // Remove the captured pawn (same row as attacker, same col as destination)
                    copy.addPiece(new ChessPosition(mov.getStartPosition().getRow(),
                            mov.getEndPosition().getColumn()), null);
                }
            }

            //move piece and set old spot to null
            copy.addPiece(mov.getEndPosition(), movingPiece);
            copy.addPiece(mov.getStartPosition(), null);

            //set temp game and make copy
            ChessGame tempGame = new ChessGame();
            tempGame.setBoard(copy);

            if (!tempGame.isInCheck(piece.getTeamColor())) {
                validMoves.add(mov);
            }
        }


        return validMoves;
    }

    //copy the board
    public ChessBoard copyBoard(ChessBoard originalBoard) {
        ChessBoard copy = new ChessBoard();

        for (int row = 1; row < 9; row++) {
            for (int col = 1; col < 9; col++) {
                ChessPosition currentPiecePos = new ChessPosition(row, col);
                ChessPiece piece = originalBoard.getPiece(currentPiecePos);

                if (piece != null) {
                    ChessPiece copiedPiece = new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                    copiedPiece.setHasMoved(piece.hasMoved()); // ADD THIS
                    copy.addPiece(currentPiecePos, copiedPiece);
                }
            }
        }
        copy.setLastMove(originalBoard.getLastMove());

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

        //make collection on available moves
        Collection<ChessMove> availableMoves = validMoves(start);

        //if no available moves throw error
        if (!availableMoves.contains(move)) {
            throw new InvalidMoveException();
        }

        //look to see if king or rook has moved
        if (piece.getPieceType() == ChessPiece.PieceType.KING ||
                piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            piece.setHasMoved(true);
        }

        //Add castling
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            int getColDifference = stop.getColumn() - start.getColumn();
            //Get abs 2 because can be con left or right side
            if (Math.abs(getColDifference) == 2) {
                //set rook col
                int rookFromCol = (getColDifference > 0) ? 8 : 1;

                //put the rook in col
                int rookToCol   = (getColDifference > 0) ? stop.getColumn() - 1 : stop.getColumn() + 1;

                //move rook
                ChessPiece rook = board.getPiece(new ChessPosition(start.getRow(), rookFromCol));
                rook.setHasMoved(true);

                //move the rook
                board.addPiece(new ChessPosition(start.getRow(), rookToCol), rook);
                board.addPiece(new ChessPosition(start.getRow(), rookFromCol), null);
            }
        }


        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int getColumnDifference = stop.getColumn() - start.getColumn();

            //if col difference set bool
            boolean movingDiagonally = getColumnDifference != 0;
            boolean targetEmpty = board.getPiece(stop) == null;

            //add en pessant
            if (movingDiagonally && targetEmpty) {
                // Remove the captured pawn (same row as attacker, same col as destination)
                board.addPiece(new ChessPosition(start.getRow(), stop.getColumn()), null);
            }
        }

        //enable move piece by clearing the start pos and putting the orig piece in stop pos
        board.addPiece(stop, piece);

        // switch player turns
        currentPlayerTurn = (currentPlayerTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;


        //add pawn promotion logic
        if (move.getPromotionPiece() != null) {

            //get the promotion piece
            ChessPiece promotion = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            board.addPiece(stop, promotion);
        }
        else {
            board.addPiece(stop, piece);
        }

        board.addPiece(start, null);

        lastMove = move;
        board.setLastMove(move);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {

        //get king pos
        ChessPosition kingPos = null;
        for (int ii = 1; ii < 9; ii++) {
            for (int jj = 1; jj < 9; jj++) {

                ChessPosition pos = new ChessPosition(ii, jj);
                ChessPiece piece = board.getPiece(pos);

                //look for a king piece that is the right team color
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING &&
                        piece.getTeamColor() == teamColor) {

                    kingPos = pos;
                }
            }
        }

        //look to see if enemy moves put the king in check
        for (int ii = 1; ii < 9; ii++) {
            for (int jj = 1; jj < 9; jj++) {

                ChessPosition pos = new ChessPosition(ii, jj);
                ChessPiece piece = board.getPiece(pos);

                if (piece == null) {
                    continue;
                }

                if (piece.getTeamColor() == teamColor) {
                    continue;
                }

                Collection<ChessMove> enemyMoves = piece.pieceMoves(board, pos);

                for (ChessMove move : enemyMoves) {

                    if (move.getEndPosition().equals(kingPos)) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {

        //if not already in check cant be
        if (!isInCheck(teamColor))
        {
            return false;
        }

        //go through all pieces
        for (int ii = 1; ii < 9; ii++) {
            for (int jj = 1; jj < 9; jj++) {

                ChessPosition pos = new ChessPosition(ii, jj);
                ChessPiece piece = board.getPiece(pos);

                // only check this team's pieces
                if (piece != null && piece.getTeamColor() == teamColor) {

                    Collection<ChessMove> moves = validMoves(pos);

                    // if ANY legal move exists,not checkmate
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }

        // no legal moves while in check
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {

        // cannot be stalemate if currently in check
        if (isInCheck(teamColor)) {
            return false;
        }

        //go through all pieces
        for (int ii = 1; ii < 9; ii++) {
            for (int jj = 1; jj < 9; jj++) {

                ChessPosition pos = new ChessPosition(ii, jj);
                ChessPiece piece = board.getPiece(pos);

                // only check this team's pieces
                if (piece != null && piece.getTeamColor() == teamColor) {

                    Collection<ChessMove> moves = validMoves(pos);

                    // if ANY legal move exists,not stalemate
                    if (!moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }

        //if in stalemate return true
        return true;
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
