package client;

import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import websocket.commands.MakeMove;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.net.URI;

public class WebSocketFacade extends Endpoint {
    private final Gson gson = new Gson();
    private final NotificationHandler notificationHandler;
    private Session session;

    public WebSocketFacade(int port, NotificationHandler notificationHandler) throws Exception {
        this.notificationHandler = notificationHandler;
        URI uri = new URI("ws://localhost:" + port + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, uri);
        // wait for onOpen to set session
        Thread.sleep(100);
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        this.session = session;
        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
                    public void onMessage(String message) {
            notificationHandler.handleMessage(message);
        }
        });
    }

    public void connect(String authToken, int gameID) throws IOException {
        var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        String json = gson.toJson(command);
        session.getBasicRemote().sendText(json);
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws IOException {
        var command = new MakeMove(authToken, gameID, move);
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void leave(String authToken, int gameID) throws IOException {
        var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void resign(String authToken, int gameID) throws IOException {
        var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        session.getBasicRemote().sendText(gson.toJson(command));
    }

    public void close() throws IOException {
        try {
            session.close();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public interface NotificationHandler {
        void handleMessage(String message);
    }


}
