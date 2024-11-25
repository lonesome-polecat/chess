package ui;

import server.Server;

import javax.websocket.*;
import java.net.URI;

public class WSClient extends Endpoint {

    public Session session;

    public WSClient(ServerFacade client, String url) throws Exception {
        URI uri = new URI(url);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println(message);
                // client.onMessage(message); <- maybe I could just pass in the client's onMessage method instead of the whole client
            }
        });
    }

    public void sendUserGameCommand(String msg) throws Exception {
        this.session.getBasicRemote().sendText(msg);
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
