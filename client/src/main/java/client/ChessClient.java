package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import model.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadMessage;
import websocket.messages.NotifyMessage;
import websocket.messages.ServerMessage;

import javax.swing.*;


public class ChessClient implements WebSocketFacade.NotificationHandler {
    private final ServerFacade server;
    private WebSocketFacade ws;
    private AuthData auth = null;
    private List<GameData> gameList = new ArrayList<>();
    private State state = State.SIGNEDOUT;
    private GameData currentGame = null;
    private ChessGame.TeamColor playerColor = null;
    private final int port;
    private final Gson gson = new Gson();

    public ChessClient(int port) {
        this.port = port;
        server = new ServerFacade(port);
    }

    @Override
    public void handleMessage(String message) {
        ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
        switch (serverMessage.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadMessage loadGame = gson.fromJson(message, LoadMessage.class);
                currentGame = loadGame.getGame();
                DrawBoard.draw(playerColor == ChessGame.TeamColor.BLACK);
            }
            case NOTIFICATION -> {
                NotifyMessage notification = gson.fromJson(message, NotifyMessage.class);
                System.out.println("\n*** " + notification.getMess() + " ***");
                printPrompt();
            }
            case ERROR -> {
                ErrorMessage error = gson.fromJson(message, ErrorMessage.class);
                System.out.println("\nError: " + error.getErrMess());
                printPrompt();
            }
        }
    }

    public void run() {
        System.out.println(" \uD83D\uDC51 Welcome to 240 chess. Type Help to get started.");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.println(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println("Bye!");
    }

    private void printPrompt() {
        System.out.print(state == State.SIGNEDOUT ? "\n[LOGGED_OUT] >>> " : "\n[LOGGED_IN] >>> ");
    }


    public String eval(String input) {
        if (input == null || input.isBlank())
        {
            return help();
        }
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

            if (state == State.SIGNEDOUT) {
                return switch (cmd) {
                    case "register" -> register(params);
                    case "login" -> login(params);
                    case "quit" -> "quit";
                    default -> help();
                };
            } else if (state == State.SIGNEDIN) {
                return switch (cmd) {
                    case "create" -> create(params);
                    case "list" -> list();
                    case "play" -> playGame(params);
                    case "observe" -> observeGame(params);
                    case "logout" -> logout();
                    case "quit" -> "quit";
                    default -> help();
                };
            } else {
                return switch (cmd) {
                    case "redraw" -> redraw();
                    case "leave" -> leave();
                    case "move" -> makeMove(params);
                    case "resign" -> resign();
                    case "highlight" -> highlight(params);
                    case "quit" -> "quit";
                    default -> help();
                };
            }
        } catch (Exception e)
        {
            return e.getMessage();
        }
    }
    private String register(String[] params) throws Exception
    {
        if (params.length != 3) {
            return "Usage: register <username> <password> <email>";
        }
        auth = server.register(params[0], params[1], params[2]);
        state = State.SIGNEDIN;
        return "Registered as " + auth.username();
    }

    private String login(String[] params) throws Exception
    {
        if (params.length != 2) {
            return "Usage: login <username> <password>";
        }
        auth = server.login(params[0], params[1]);
        state = State.SIGNEDIN;
        return "Logged in as " + auth.username();
    }

    private String create(String[] params) throws Exception {
        assertSignedIn();
        if (params.length < 1) {
            return "Usage: create <game name>";
        }
        String gameName = String.join(" ", params);
        server.createGame(auth.authToken(), gameName);
        gameList = new ArrayList<>(server.listGames(auth.authToken()));
        return "Created game: " + gameName;
    }

    private String list() throws Exception {
        assertSignedIn();
        var games = server.listGames(auth.authToken());
        gameList = new ArrayList<>(games);
        if (gameList.isEmpty()) {
            return "No games available";
        }
        java.lang.StringBuilder gameInfo = new StringBuilder();
        for (int i = 0; i < gameList.size(); i++) {
            GameData g = gameList.get(i);
            gameInfo.append(String.format("%d. %s | White: %s | Black: %s%n",
                    i + 1,
                    g.gameName(),
                    g.whiteUsername() != null ? g.whiteUsername() : "open",
                    g.blackUsername() != null ? g.blackUsername() : "open"));
        }
        return gameInfo.toString();
    }

    private GameData getGameFromParams(String[] params, int expectedLength) throws Exception {
        assertSignedIn();

        if (params.length != expectedLength) {
            throw new Exception(expectedLength == 2
                    ? "Usage: play <game number> <WHITE|BLACK>"
                    : "Usage: observe <game number>");
        }

        int idx;
        try {
            idx = Integer.parseInt(params[0]) - 1;
        } catch (NumberFormatException e) {
            throw new Exception("Invalid game number");
        }

        gameList = new ArrayList<>(server.listGames(auth.authToken()));

        if (gameList.isEmpty()) {
            throw new Exception("No games available");
        }
        if (idx < 0 || idx >= gameList.size()) {
            throw new Exception("Invalid game number");
        }

        return gameList.get(idx);
    }

    private String playGame(String[] params) throws Exception {
        GameData game = getGameFromParams(params, 2);

        String color = params[1].toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            return "Color must be WHITE or BLACK";
        }

        server.joinGame(auth.authToken(), color, game.gameID());
        playerColor = color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        ws = new WebSocketFacade(port, this);
        ws.connect(auth.authToken(), game.gameID());
        state = State.GAMEPLAY;
        return "";
    }

    private String observeGame(String[] params) throws Exception {
        GameData game = getGameFromParams(params, 1);
        playerColor = null;
        ws = new WebSocketFacade(port, this);
        ws.connect(auth.authToken(), game.gameID());
        state = State.GAMEPLAY;
        return "";
    }

    private String logout() throws Exception {
        assertSignedIn();
        server.logout(auth.authToken());
        auth = null;
        state = State.SIGNEDOUT;
        return "Logged out successfully";
    }

    private String redraw() {
        if (currentGame == null) {
            return "No game to redraw";
        }
        DrawBoard.draw(playerColor == ChessGame.TeamColor.BLACK);
        return "";
    }

    private String leave() throws Exception {
        ws.leave(auth.authToken(), currentGame.gameID());
        ws.close();
        ws = null;
        currentGame = null;
        playerColor = null;
        state = State.SIGNEDIN;
        return "Left the game";
    }

    private String makeMove(String[] params) throws Exception {
        if (params.length != 2) {
            return "Usage: move <from> <to> (e.g. move e2 e4)";
        }
        ChessPosition from = parsePosition(params[0]);
        ChessPosition to = parsePosition(params[1]);
        if (from == null || to == null) {
            return "Invalid position. Use format like e2, a1, h8";
        }
        ChessPiece.PieceType promotion = null;
        if (params.length >= 3) {
            promotion = parsePromotion(params[2]);
        }
        ChessMove move = new ChessMove(from, to, promotion);
        ws.makeMove(auth.authToken(), currentGame.gameID(), move);
        return "";
    }

    private String resign() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("yes")) {
            ws.resign(auth.authToken(), currentGame.gameID());
            return "You resigned";
        }
        return "Resignation cancelled";
    }

    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) {
            return null;
        }
        int col = pos.charAt(0) - 'a' + 1;
        int row = pos.charAt(1) - '0';
        if (col < 1 || col > 8 || row < 1 || row > 8) {
            return null;
        }
        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType parsePromotion(String piece) {
        return switch (piece.toUpperCase()) {
            case "Q" -> ChessPiece.PieceType.QUEEN;
            case "R" -> ChessPiece.PieceType.ROOK;
            case "B" -> ChessPiece.PieceType.BISHOP;
            case "N" -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
    }


    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    Commands:
                        register <username> <password> <email>
                        login <username> <password>
                        quit
                        help
                    """;
        }
        return """
                Commands:
                    create <game name>
                    list
                    play <game number> <WHITE|BLACK>
                    observe <game number>
                    logout
                    quit
                    help
                """;
    }

    private void assertSignedIn() throws Exception {
        if (state == State.SIGNEDOUT) {
            throw new Exception("You must sign in");
        }
    }
}