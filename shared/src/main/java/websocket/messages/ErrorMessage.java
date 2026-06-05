package websocket.messages;

import model.GameData;

public class ErrorMessage extends ServerMessage {
    private final String errMess;

    public ErrorMessage(String errMess) {
        super(ServerMessageType.ERROR);
        this.errMess = errMess;
    }

    public String getErrMess() {
        return errMess;
    }
}
