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

    private String login(String[] params) throws Exception
    {
        if (params.length != 2) {
            return "Usage: login <username> <password>";
        }
        auth = server.login(params[0], params[1]);
        state = State.SIGNEDIN;
        return "Logged in as " + auth.username();
    }

    public String rescuePet(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length >= 2) {
            String name = params[0];
            PetType type = PetType.valueOf(params[1].toUpperCase());
            var pet = new Pet(0, name, type);
            pet = server.addPet(pet);
            return String.format("You rescued %s. Assigned ID: %d", pet.name(), pet.id());
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <name> <CAT|DOG|FROG>");
    }

    public String listPets() throws ResponseException {
        assertSignedIn();
        PetList pets = server.listPets();
        var result = new StringBuilder();
        var gson = new Gson();
        for (Pet pet : pets) {
            result.append(gson.toJson(pet)).append('\n');
        }
        return result.toString();
    }

    public String adoptPet(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length == 1) {
            try {
                int id = Integer.parseInt(params[0]);
                Pet pet = getPet(id);
                if (pet != null) {
                    server.deletePet(id);
                    return String.format("%s says %s", pet.name(), pet.sound());
                }
            } catch (NumberFormatException ignored) {
            }
        }
        throw new ResponseException(ResponseException.Code.ClientError, "Expected: <pet id>");
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

    public String signOut() throws ResponseException {
        assertSignedIn();
        ws.leavePetShop(visitorName);
        state = State.SIGNEDOUT;
        return String.format("%s left the shop", visitorName);
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