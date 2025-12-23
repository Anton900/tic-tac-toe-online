package dev.tictactoe.game;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class GameLogicService
{
    private final Map<String, TicTacToeGame> gameList = new ConcurrentHashMap<>();

    public String createGame(String gameId)
    {
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
        return game;
    }

    public void removeGame(String gameId)
    {
        gameList.remove(gameId);
    }

    public GameStateDTO getGameState(String gameId)
    {
        TicTacToeGame game = getGame(gameId);
        return GameStateDTO.builder()
                .board(game.getBoard().clone())
                .status(game.getStatus())
                .currentTurn(game.getCurrentTurn())
                .build();
    }
}
