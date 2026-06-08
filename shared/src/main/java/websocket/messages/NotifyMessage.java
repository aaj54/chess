package websocket.messages;


public class NotifyMessage extends ServerMessage{
    private final String message;

    public NotifyMessage(String message) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
    }

    public String getMess() {
        return message;
    }
}
