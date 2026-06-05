package websocket.messages;

import model.GameData;

public class NotifyMessage extends ServerMessage{
    private final GameData game;

    public NotifyMessage(GameData game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }

    public GameData getGame() {
        return game;
    }
}
