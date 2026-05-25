package client;

import chess.*;
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
    private static void drawBoard(ChessBoard board, boolean flip)
    {
        String[] cols = {"a", "b", "c", "d", "e", "f", "g", "h"};
        printColHeaders(cols, flip);

        for(int r = 0; r < 8; r++)
        {
            int currentRow = flip ? r + 1 : 8 -r;
            System.out.print(SET_BG_COLOR_BLUE + " " + currentRow + " " + RESET_BG_COLOR);
            for (int c = 0; c < 8; c++) {
                int currentCol = flip ? 7 - c + 1 : c + 1;
                boolean isLight = (currentRow + currentCol) % 2 == 0;
                String bg = isLight ? SET_BG_COLOR_WHITE : SET_BG_COLOR_DARK_GREY;

                ChessPosition pos = new ChessPosition(currentRow, currentCol);
                ChessPiece piece = board.getPiece(pos);
                System.out.print(bg + getPieceStr(piece));
            }
        }
    }
    private static void printColHeaders(String[] cols, boolean flip) {
        System.out.print(SET_BG_COLOR_BLUE + "   ");
        for (int ii = 0; ii < 8; ii++)
        {
            int idx = flip ? 7 - ii : ii;
            System.out.print(" " + cols[idx] + " ");
        }
        System.out.println("   " + RESET_BG_COLOR);
    }

    private static String getPieceStr(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        String color = isWhite ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE;
        String symbol = switch (piece.getPieceType())
        {
            case KING -> isWhite ? WHITE_KING : BLACK_KING;
            case QUEEN -> isWhite ? WHITE_QUEEN : BLACK_QUEEN;
            case ROOK -> isWhite ? WHITE_ROOK: BLACK_ROOK;
            case BISHOP -> isWhite ? WHITE_BISHOP: BLACK_BISHOP;
            case KNIGHT -> isWhite ? WHITE_KNIGHT : BLACK_KNIGHT;
            case PAWN -> isWhite ? WHITE_PAWN : BLACK_PAWN;
        };
        return color + symbol + RESET_TEXT_COLOR;
    }
}

