package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.MakeMove;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadMessage;
import websocket.messages.NotifyMessage;
import io.javalin.websocket.*;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final DataAccess dataAccess;
    private final Gson gson = new Gson();

    public WebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            websocket.commands.UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(command, ctx.session);
                case MAKE_MOVE -> makeMove(ctx.session, gson.fromJson(ctx.message(), MakeMove.class));
                case LEAVE -> leave(ctx.session, command);
                case RESIGN -> resign(ctx.session, command);
            }
        } catch (Exception ex) {
            try {
                connections.sendToSession(ctx.session, new ErrorMessage("Error: " + ex.getMessage()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(UserGameCommand command, Session session) throws IOException {
        try {
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                connections.sendToSession(session, new ErrorMessage("Error: unauthorized"));
                return;
            }
            GameData game = dataAccess.getGame(command.getGameID());
            if (game == null) {
                connections.sendToSession(session, new ErrorMessage("Error: game not found"));
                return;
            }
            connections.add(command.getGameID(), session);
            connections.sendToSession(session, new LoadMessage(game));
            String username = auth.username();
            String message;
            if (username.equals(game.whiteUsername())) {
                message = username + " joined as WHITE";
            } else if (username.equals(game.blackUsername())) {
                message = username + " joined as BLACK";
            } else {
                message = username + " joined as observer";
            }
            connections.broadcast(command.getGameID(), session, new NotifyMessage(message));
        } catch (DataAccessException e) {
            connections.sendToSession(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private void leave(String visitorName, Session session) throws IOException {
        var message = String.format("%s left the shop", visitorName);
        var notification = new Notification(Notification.Type.DEPARTURE, message);
        connections.broadcast(session, notification);
        connections.remove(session);
    }

    public void makeMove(String petName, String sound) throws ResponseException {
        try {
            var message = String.format("%s says %s", petName, sound);
            var notification = new Notification(Notification.Type.NOISE, message);
            connections.broadcast(null, notification);
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private void resign(String visitorName, Session session) throws IOException {
        var message = String.format("%s left the shop", visitorName);
        var notification = new Notification(Notification.Type.DEPARTURE, message);
        connections.broadcast(session, notification);
        connections.remove(session);
    }
}