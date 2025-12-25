package dev.tictactoe.game;

import dev.tictactoe.game.dto.GameStateDTO;
import dev.tictactoe.game.model.ActionType;
import dev.tictactoe.game.model.ClientMessage;
import dev.tictactoe.game.registry.GameRegistry;
import dev.tictactoe.game.service.GameManagementService;
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
        Log.info("================================= ON OPEN ===========================================");
        String gameId = connection.pathParam("gameId");
        if (gameId == null || gameId.isBlank())
        {
            Log.warn("Missing or empty gameId on open, closing connection");
            connection.close();
            return;
        }

        boolean joinedGame = gameManagementService.joinedCurrentGame(gameId, connection);
        if(joinedGame)
        {
            gameRegistry.addGameConnection(gameId, connection);
            sendGameState(gameId);
            Log.infof("Connection opened for gameId=%s", gameId);
        }
        else
        {
            Log.infof("No existing game to join for gameId=%s", gameId);
        }
    }

    @OnClose
    void onClose(WebSocketConnection connection)
    {
        Log.info("================================= ON CLOSE ===========================================");

        gameRegistry.removeConnection(connection);
        connection.close();
        Log.info("Connection closed and removed from registry");
    }

    @OnTextMessage
    public void onMessage(ClientMessage clientMessage, WebSocketConnection connection)
    {
        Log.info("================================= ON MESSAGE ===========================================");
        Log.info("Received message from client in onMessage: " + clientMessage);

        String gameId = connection.pathParam("gameId");
        if(!validClientMessage(clientMessage) || !validGameId(connection))
        {
            return;
        }
        Log.info("Received message: " + clientMessage + "");

        if (clientMessage.actionType == ActionType.CREATE_GAME)
        {
            Log.infof("Creating new game for gameId=%s", gameId);
            gameManagementService.createGame(gameId, connection);
            gameRegistry.addGameConnection(gameId, connection);
            sendGameState(gameId);
        }
        if (clientMessage.actionType == ActionType.MAKE_MOVE)
        {
            Log.infof("Making move for gameId=%s at position=%d", gameId, clientMessage.position);
            gameManagementService.makeMove(gameId, clientMessage.position, connection);
            sendGameState(gameId);
        }
    }

    private boolean validGameId(WebSocketConnection connection)
    {
        String gameId = connection.pathParam("gameId");
        if (gameId == null || gameId.isBlank())
        {
            Log.warn("Missing or empty gameId in connection");
            return false;
        }
        return true;
    }

    private boolean validClientMessage(ClientMessage clientMessage)
    {
        if (clientMessage == null || clientMessage.actionType == null)
        {
            Log.warn("Received null or invalid client message");
            return false;
        }
        return true;
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