package dev.tictactoe.game;

import io.quarkus.websockets.next.*;
import jakarta.inject.Inject;
import java.util.Set;

@WebSocket(path = "/game/{gameId}")
public class GameWebSocket
{
    @Inject
    GameService gameService;

    @Inject
    GameRegistry gameRegistry;

    @OnOpen
    void onOpen(WebSocketConnection connection)
    {
        String gameId = connection.pathParam("gameId");
        gameRegistry.addGame(gameId, connection);
    }

    @OnClose
    void onClose(WebSocketConnection connection)
    {
        gameRegistry.removeConnection(connection);
    }

    @OnTextMessage
    public void onMessage(ClientMessage clientMessage, WebSocketConnection connection)
    {
        String gameId = connection.pathParam("gameId");

        if (clientMessage.actionType == ActionType.MAKE_MOVE)
        {
            gameService.makeMove(gameId, clientMessage.position);
            TicTacToeGameStateDTO gameState = gameService.getGameState(gameId);
            Set<WebSocketConnection> connections = gameRegistry.getActiveConnections(gameId);
            for (WebSocketConnection conn : connections)
            {
                conn.sendText(gameState);
            }
        }
    }
}