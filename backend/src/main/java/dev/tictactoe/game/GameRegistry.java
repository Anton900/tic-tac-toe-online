package dev.tictactoe.game;

import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class GameRegistry
{
    private final Map<String, Set<WebSocketConnection>> gamesList = new ConcurrentHashMap<>();

    public void removeConnection(WebSocketConnection connection)
    {
        gamesList.values().forEach(connections -> connections.remove(connection));
        gamesList.values().removeIf(Set::isEmpty);
    }

    public void addGame(String gameId, WebSocketConnection connection)
    {
        if(gameId == null)
        {
            throw new IllegalArgumentException("GameId cannot be null");
        }
        gamesList.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet()).add(connection);
    }

    public Set<WebSocketConnection> getActiveConnections(String gameId)
    {
        if(gameId == null)
        {
            return Set.of();
        }
        return gamesList.getOrDefault(gameId, Set.of());
    }
}
