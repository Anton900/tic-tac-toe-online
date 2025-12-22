package dev.tictactoe.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GameStateDTO
{
    private Mark[] board;
    private Mark currentTurn;
    private GameStatus status;
}
