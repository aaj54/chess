package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import model.*;

import com.google.gson.Gson;

public class ChessClient {
    private final ServerFacade server;
    private AuthData auth = null;
    private List<GameData> gameList = new ArrayList<>();
    private State state = State.SIGNEDOUT;

    public ChessClient(int port) {
        server = new ServerFacade(port);
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


    private void notify(Notification notification) {
        System.out.println(RED + notification.message());
        printPrompt();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET + ">>> " + GREEN);
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
            } else {
                return switch (cmd) {
                    case "create" -> create(params);
                    case "list" -> list();
                    case "join <ID> [WHITE|BLACK}" -> playGame();
                    case "observe <ID>" -> observeGame();
                    case "logout" -> logout();
                    case "quit" -> "quit";
                    default -> help();
                };
            }
        } catch (Exception e)
        {
            return "Error: " + e.getMessage();
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

    private String playGame(String[] params) throws Exception {
        assertSignedIn();
        if (params.length != 2)
        {
            return "Usage: play <game number> <WHITE|BLACK>";
        }
        int idx;
        try {
            idx = Integer.parseInt(params[0])-1;
        } catch (NumberFormatException e) {
            return "Invalid game number";
        }
        if (gameList.isEmpty())
        {
            return "Please create a game";
        }
        if (idx < 0 || idx >= gameList.size()) {
            return "Invalid game number";
        }
        String color = params[1].toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            return "Color must be WHITE or BLACK";
        }
        int gameID = gameList.get(idx).gameID();
        server.joinGame(auth.authToken(), color, gameID);
        //Draw board
        return "";

    }

    private String observeGame(String[] params) throws Exception {
        assertSignedIn();
        if (params.length != 1)
        {
            return "Usage: observe <game number>";
        }
        int idx;
        try {
            idx = Integer.parseInt(params[0])-1;
        } catch (NumberFormatException e) {
            return "Invalid game number";
        }
        if (gameList.isEmpty())
        {
            return "Please create a game";
        }
        if (idx < 0 || idx >= gameList.size()) {
            return "Invalid game number";
        }
        String color = params[1].toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            return "Color must be WHITE or BLACK";
        }
        //Draw board
        return "";

    }

    public String adoptAllPets() throws ResponseException {
        assertSignedIn();
        var buffer = new StringBuilder();
        for (Pet pet : server.listPets()) {
            buffer.append(String.format("%s says %s%n", pet.name(), pet.sound()));
        }

        server.deleteAllPets();
        return buffer.toString();
    }

    private String logout() throws Exception {
        assertSignedIn();
        server.logout(auth.authToken());
        auth = null;
        state = State.SIGNEDOUT;
        return "Logged out successfully";
    }

    private Pet getPet(int id) throws ResponseException {
        for (Pet pet : server.listPets()) {
            if (pet.id() == id) {
                return pet;
            }
        }
        return null;
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - signIn <yourname>
                    - quit
                    """;
        }
        return """
                - list
                - adopt <pet id>
                - rescue <name> <CAT|DOG|FROG|FISH>
                - adoptAll
                - signOut
                - quit
                """;
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(ResponseException.Code.ClientError, "You must sign in");
        }
    }
}