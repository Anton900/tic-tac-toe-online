package dev.tictactoe.game.registry;

import dev.tictactoe.game.model.GameSession;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class GameSessionRegistry
{
    private final Map<String, GameSession> gameSessions = new ConcurrentHashMap<>();

    public void addGameSession(GameSession gameSession)
    {
        gameSessions.put(gameSession.getGameId(), gameSession);
    }

    public GameSession getGameSession(String gameId)
    {
        return gameSessions.get(gameId);
    }

    public void addGameSession(String gameId, GameSession gameSession)
    {
        gameSessions.put(gameId, gameSession);
    }

    public void removeGameSession(String gameId)
    {
        gameSessions.remove(gameId);
    }

    public Map<String, GameSession> getGameSessions()
    {
        return gameSessions;
    }
}
