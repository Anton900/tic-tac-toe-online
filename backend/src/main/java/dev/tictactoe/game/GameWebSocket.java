package dev.tictactoe.game;

import io.quarkus.logging.Log;
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
        if (gameId == null || gameId.isBlank())
        {
            Log.warn("Missing or empty gameId on open, closing connection");
            connection.close();
            return;
        }
        gameRegistry.addGame(gameId, connection);
        Log.infof("Connection opened for gameId=%s", gameId);
    }

    @OnClose
    void onClose(WebSocketConnection connection)
    {
        gameRegistry.removeConnection(connection);
        connection.close();
        Log.info("Connection closed and removed from registry");
    }

    @OnTextMessage
    public void onMessage(ClientMessage clientMessage, WebSocketConnection connection)
    {
        if (clientMessage == null || clientMessage.actionType == null)
        {
            Log.warn("Received null or invalid client message");
            return;
        }

        String gameId = connection.pathParam("gameId");

        if (gameId == null || gameId.isBlank())
        {
            Log.warn("Missing gameId for incoming message; ignoring");
            return;
        }

        if (clientMessage.actionType == ActionType.MAKE_MOVE)
        {
            gameService.makeMove(gameId, clientMessage.position);
            GameStateDTO gameState = gameService.getGameState(gameId);
            Set<WebSocketConnection> connections = gameRegistry.getActiveConnections(gameId);
            for (WebSocketConnection conn : connections)
            {
                try
                {
                    conn.sendText(gameState);
                }
                catch (Exception e)
                {
                    Log.error("Error sending game state to client", e);
                }
            }
        }
    }
}