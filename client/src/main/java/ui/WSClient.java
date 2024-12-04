package ui;

import com.google.gson.Gson;
import server.Server;
import websocket.commands.UserGameCommand;

import javax.websocket.*;
import java.net.URI;

public class WSClient extends Endpoint {

    public Session session;

    public WSClient(ChessClient client, String url) throws Exception {
        URI uri = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                client.onMessage(message);
            }
        });
    }

    public void sendUserGameCommand(UserGameCommand command) throws Exception {
        var msg = new Gson().toJson(command);
        this.session.getBasicRemote().sendText(msg);
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
