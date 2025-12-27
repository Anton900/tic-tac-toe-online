package dev.tictactoe.game;

import dev.tictactoe.exception.ApiError;
import dev.tictactoe.exception.GameException;
import dev.tictactoe.exception.GameNotFoundException;
import dev.tictactoe.game.dto.GameStateDTO;
import dev.tictactoe.game.model.*;
import dev.tictactoe.game.registry.GameRegistry;
import dev.tictactoe.game.service.GameManagementService;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.*;
import io.vertx.core.json.Json;
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
        if (joinedGame)
        {
            gameManagementService.sendGameState(gameId);
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

        gameManagementService.removeParticipant(connection);
        connection.close();
        Log.info("Connection closed and removed from registry");
    }

    @OnTextMessage
    public void onMessage(ClientMessage clientMessage, WebSocketConnection connection)
    {
        Log.info("================================= ON MESSAGE ===========================================");
        Log.info("Received message from client in onMessage: " + clientMessage);

        if (!validClientMessage(clientMessage))
        {
            return;
        }
        Log.info("Received message: " + clientMessage + "");

        try
        {
            gameManagementService.handleMessage(clientMessage, connection);
        }
        catch (GameException gameException)
        {
            Log.errorf("GameException occurred: %s", gameException.getMessage());
            sendError(
                    connection,
                    gameException.getCode(),
                    gameException.getMessage()
            );
        }
        catch (Exception e)
        {
            Log.errorf("Unexpected exception occurred: %s", e.getMessage());
            sendError(
                    connection,
                    "INTERNAL_SERVER_ERROR",
                    e.getMessage()
            );

        }
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

    private void sendError(
            WebSocketConnection connection,
            String code,
            String message
    )
    {
        try
        {
            ErrorMessage error = new ErrorMessage(code, message);
            connection.sendText(Json.encode(error))
                    .subscribe()
                    .with(
                            ignored -> Log.infof("IGNORED: Error sent successfully to client"),
                            failure -> Log.errorf("FAILURE: Failed to send error to client. Failure message: %s",
                                    failure.getMessage())
                    );
        }
        catch (Exception e)
        {
            Log.error("Failed to send error to client", e);
        }
    }
}