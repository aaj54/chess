package websocket.messages;


public class NotifyMessage extends ServerMessage{
    private final String mess;

    public NotifyMessage(String mess) {
        super(ServerMessageType.NOTIFICATION);
        this.mess = mess;
    }

    public String getMess() {
        return mess;
    }
}
