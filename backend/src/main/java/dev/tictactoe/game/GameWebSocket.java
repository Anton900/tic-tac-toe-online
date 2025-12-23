package dev.tictactoe.game;

import io.quarkus.logging.Log;
import io.quarkus.websockets.next.*;
import jakarta.inject.Inject;

import java.util.Set;

@WebSocket(path = "/game/{gameId}")
public class GameWebSocket
{
    @Inject
    GameLogicService gameLogicService;

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
        TicTacToeGame game = gameLogicService.getGame(gameId);
        if(game == null)
        {
            Log.info(("Creating new game for gameId=%s").formatted(gameId));
            gameLogicService.createGame(gameId);
            sendGameState(gameId);
        }
        else
        {
            Log.info(("Reusing existing game for gameId=%s").formatted(gameId));
            sendGameState(gameId);
        }
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
            gameLogicService.makeMove(gameId, clientMessage.position);
            sendGameState(gameId);
        }
    }

    public void sendGameState(String gameId)
    {
        GameStateDTO gameState = gameLogicService.getGameState(gameId);
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