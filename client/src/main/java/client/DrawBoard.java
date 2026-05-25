package client;

import chess.*
import ui.EscapeSequences.*;
import static ui.EscapeSequences.*;

public class DrawBoard {
    public static void draw(boolean blackPerspective)
    {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        System.out.println();
        drawBoard(board, blackPerspective);
        System.out.println();
    }
}
