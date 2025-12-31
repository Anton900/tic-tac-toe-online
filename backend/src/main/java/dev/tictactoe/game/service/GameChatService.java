package dev.tictactoe.game.service;

import dev.tictactoe.game.model.GameSession;
import dev.tictactoe.game.model.User;
import dev.tictactoe.game.model.client.ClientMessage;
import dev.tictactoe.game.model.server.ChatMessage;
import dev.tictactoe.game.registry.GameSessionRegistry;
import dev.tictactoe.game.registry.UserRegistry;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Set;

@ApplicationScoped
public class GameChatService
{
    @Inject
    UserRegistry userRegistry;

    @Inject
    GameSessionRegistry gameSessionRegistry;

    public void handleMessage(ClientMessage clientMessage, WebSocketConnection connection)
    {
        User user = userRegistry.getUserByConnection(connection.id());
        if (user == null)
        {
            return;
        }
        String gameId = clientMessage.getGameId();
        String message = clientMessage.getChatMessage();
        broadcastMessage(gameId, user.displayName(), message);

    }

    public void broadcastMessage(String gameId, String displayName, String message)
    {
        GameSession gameSession = gameSessionRegistry.getGameSession(gameId);
        Set<User> gameSessionUsers = gameSession.getAllUsers();
        for (User user : gameSessionUsers)
        {
            try
            {
                ChatMessage chatMessage = new ChatMessage(displayName, message);
                Log.infof("Sending chat message to client for gameId=%s", gameId);
                Set<WebSocketConnection> connections = userRegistry.getUserConnections(user.id());
                for (WebSocketConnection connection : connections)
                {
                    Log.infof("Sending chat message to userId=%s on connectionId=%s for gameId=%s", user.id(), connection.id(), gameId);
                    connection.sendText(chatMessage)
                            .subscribe()
                            .with(
                                    ignored -> Log.infof("IGNORED: Chat message sent successfully to client for gameId=%s", gameId),
                                    failure -> Log.errorf("FAILURE: Failed to send chat message to client for gameId=%s: %s", gameId,
                                            failure.getMessage())
                            );
                }
            } catch (Exception e)
            {
                Log.error("Error sending game state to client", e);
            }
        }
    }
}
