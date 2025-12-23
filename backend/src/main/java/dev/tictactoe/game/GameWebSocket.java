package dev.tictactoe.game;

import io.quarkus.logging.Log;
import io.quarkus.websockets.next.*;
import jakarta.inject.Inject;

import java.util.Set;

@WebSocket(path = "/game/{gameId}")
public class GameWebSocket
{
    @Inject
    GameManagementService gameManagementService;

    @Inject
    GameRegistry gameRegistry;

    @OnOpen
    void onOpen(WebSocketConnection connection)
    {
        Log.info("================================= ONOPEN ===========================================");
        String gameId = connection.pathParam("gameId");
        if (gameId == null || gameId.isBlank())
        {
            Log.warn("Missing or empty gameId on open, closing connection");
            connection.close();
            return;
        }

        gameManagementService.manageGameOnOpen(gameId, connection);
        gameRegistry.addGameConnection(gameId, connection);
        sendGameState(gameId);
        Log.infof("Connection opened for gameId=%s", gameId);
    }

    @OnClose
    void onClose(WebSocketConnection connection)
    {
        Log.info("================================= ONCLOSE ===========================================");

        gameRegistry.removeConnection(connection);
        connection.close();
        Log.info("Connection closed and removed from registry");
    }

    @OnTextMessage
    public void onMessage(ClientMessage clientMessage, WebSocketConnection connection)
    {
        Log.info("================================= ONMESSAGE ===========================================");
        Log.info("Received message from client in onMessage: " + clientMessage);
        if (clientMessage == null || clientMessage.actionType == null)
        {
            Log.warn("Received null or invalid client message");
            return;
        }
        Log.info("Received message: " + clientMessage + "");

        String gameId = connection.pathParam("gameId");

        if (gameId == null || gameId.isBlank())
        {
            Log.warn("Missing gameId for incoming message; ignoring");
            return;
        }

        if (clientMessage.actionType == ActionType.MAKE_MOVE)
        {
            gameManagementService.makeMove(gameId, clientMessage.position, connection);
            sendGameState(gameId);
        }
    }

    public void sendGameState(String gameId)
    {
        GameStateDTO gameState = gameManagementService.getGameState(gameId);
        Set<WebSocketConnection> connections = gameRegistry.getActiveConnections(gameId);
        for (WebSocketConnection conn : connections)
        {
            try
            {
                Log.infof("Sending updated game state to client for gameId=%s", gameId);
                conn.sendText(gameState)
                        .subscribe()
                        .with(
                                ignored -> Log.infof("IGNORED: Game state sent successfully to client for gameId=%s", gameId),
                                failure -> Log.errorf("FAILURE: Failed to send game state to client for gameId=%s: %s", gameId,
                                        failure.getMessage())
                        );
            } catch (Exception e)
            {
                Log.error("Error sending game state to client", e);
            }
        }
    }
}