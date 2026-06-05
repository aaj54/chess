package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<Integer, ArrayList<Session>> gameSessions = new ConcurrentHashMap<>();

    public void add(int gameID, Session session) {
        gameSessions.computeIfAbsent(gameID, k -> new ArrayList<>()).add(session);
    }

    public void remove(int gameID, Session session) {
        var sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(session);
        }
    }

    public void broadcast(int gameID, Session excludeSession, ServerMessage notification) throws IOException {
        String msg = new Gson().toJson(notification);
        var sessions = gameSessions.get(gameID);
        if (sessions == null) {
            return;
        }
        for (Session c : new ArrayList<>(sessions)) {
            if (c.isOpen()) {
                if (!c.equals(excludeSession)) {
                    c.getRemote().sendString(msg);
                }
            }
        }
    }

    public void sendToSession(Session session, ServerMessage message) throws IOException {
        String json = new Gson().toJson(message);
        if (session.isOpen()) {
            session.getRemote().sendString(json);
        }
    }
}