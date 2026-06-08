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
    public Session session;
    private final Gson gson = new Gson();

    public WebSocketFacade(int port, NotificationHandler notificationHandler) throws Exception {
        URI uri = new URI("ws://localhost:" + port + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);
        this.session.addMessageHandler((MessageHandler.Whole<String>) notificationHandler::handleMessage);
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, int gameID) throws IOException {
        var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        session.getBasicRemote().sendText(gson.toJson(command));
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
