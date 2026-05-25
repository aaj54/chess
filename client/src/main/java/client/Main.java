package client;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        new ChessClient(port).run();
    }
}
