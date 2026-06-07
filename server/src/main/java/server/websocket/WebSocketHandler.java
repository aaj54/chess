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
                case LEAVE -> leave(command, ctx.session);
                case RESIGN -> resign(command, ctx.session);
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

    private void leave(UserGameCommand command, Session session) throws IOException {
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
            String username = auth.username();
            if (username.equals(game.whiteUsername())) {
                dataAccess.updateGame(new GameData(game.gameID(), null,
                        game.blackUsername(), game.gameName(), game.game()));
            } else if (username.equals(game.blackUsername())) {
                dataAccess.updateGame(new GameData(game.gameID(), game.whiteUsername(),
                        null, game.gameName(), game.game()));
            }
            connections.remove(command.getGameID(), session);
            connections.broadcast(command.getGameID(), session,
                    new NotifyMessage(username + " left the game"));
        } catch (DataAccessException e) {
            connections.sendToSession(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    public void makeMove(Session session, MakeMove command) throws IOException {
        //copy from connect
        try {
            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                connections.sendToSession(session, new ErrorMessage("Error: unauthorized"));
                return;
            }
            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                connections.sendToSession(session, new ErrorMessage("Error: game not found"));
                return;
            }
            ChessGame game = gameData.game();
            String username = auth.username();

            if (game.isGameOver()) {
                connections.sendToSession(session, new ErrorMessage("Error: game is over"));
                return;
            }

            ChessGame.TeamColor color = null;
            if (username.equals(gameData.whiteUsername())) {
                color = ChessGame.TeamColor.WHITE;
            }
            else if (username.equals(gameData.blackUsername())) {
                color = ChessGame.TeamColor.BLACK;
            }
            if (color == null) {
                connections.sendToSession(session, new ErrorMessage("Error: observers cannot make moves"));
                return;
            }
            if (game.getTeamTurn() != color) {
                connections.sendToSession(session, new ErrorMessage("Error: not your turn"));
                return;
            }
            try {
                game.makeMove(command.getMove());
            } catch (Exception e) {
                connections.sendToSession(session, new ErrorMessage("Error: invalid move"));
                return;
            }

            GameData updGame = new GameData(gameData.gameID(), gameData.whiteUsername(),
                    gameData.blackUsername(), gameData.gameName(), game);
            dataAccess.updateGame(updGame);

            connections.broadcast(command.getGameID(), null, new LoadMessage(updGame));
            connections.broadcast(command.getGameID(), session,
                    new NotifyMessage(username + " made move: " + command.getMove()));

            ChessGame.TeamColor opponent = color == ChessGame.TeamColor.WHITE ?
                    ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
            String opponentName = color == ChessGame.TeamColor.WHITE ?
                    gameData.blackUsername() : gameData.whiteUsername();

            if (game.isInCheckmate(opponent)) {
                game.setGameOver(true);
                dataAccess.updateGame(updGame);
                connections.broadcast(command.getGameID(), null,
                        new NotifyMessage(opponentName + " is in checkmate! Game over."));
            }
            else if (game.isInStalemate(opponent)) {
                game.setGameOver(true);
                dataAccess.updateGame(updGame);
                connections.broadcast(command.getGameID(), null,
                        new NotifyMessage("Stalemate! Game over."));
            } else if (game.isInCheck(opponent)) {
                connections.broadcast(command.getGameID(), null,
                        new NotifyMessage(opponentName + " is in check!"));
            }
        } catch (DataAccessException e) {
            connections.sendToSession(session, new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private void resign(String visitorName, Session session) throws IOException {
        var message = String.format("%s left the shop", visitorName);
        var notification = new Notification(Notification.Type.DEPARTURE, message);
        connections.broadcast(session, notification);
        connections.remove(session);
    }
}