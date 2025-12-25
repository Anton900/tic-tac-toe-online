package dev.tictactoe.game.model;

import dev.tictactoe.game.engine.TicTacToeGame;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class GameSession
{
    TicTacToeGame ticTacToeGame = new TicTacToeGame();
    Set<GameParticipant> gameParticipants = new HashSet<>();
}
