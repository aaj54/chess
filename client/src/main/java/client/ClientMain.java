package client;

public class ClientMain {
    public static void main(String[] args) {
        int port = 2323;
        new ChessClient(port).run();
    }
}
