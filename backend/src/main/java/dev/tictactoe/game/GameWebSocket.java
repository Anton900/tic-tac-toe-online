package dev.tictactoe.game;

import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;

@WebSocket(path = "/game")
public class GameWebSocket
{
    private GameService gameService;

    @OnTextMessage
    public void onMessage(String message, WebSocketConnection webSocketConnection )
    {
        // parse JSON
        // call GameService


        // broadcast state
    }
}