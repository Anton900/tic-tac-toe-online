package dev.tictactoe.game;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class GameService
{
    private final Map<String, TicTacToeGame> gameList = new ConcurrentHashMap<>();

    public String createGame()
    {
        String gameId = UUID.randomUUID().toString();
        gameList.put(gameId, new TicTacToeGame());
        return gameId;
    }

    public void makeMove(String gameId, int position)
    {
        TicTacToeGame game = getGame(gameId);
        game.makeMove(position);
    }

    public TicTacToeGame getGame(String gameId)
    {
        TicTacToeGame game = gameList.get(gameId);
        if (game == null)
        {
            throw new IllegalArgumentException("Game not found");
        }
        return game;
    }
}
