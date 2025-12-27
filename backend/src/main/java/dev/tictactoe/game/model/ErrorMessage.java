package dev.tictactoe.game.model;

import dev.tictactoe.game.dto.GameStateDTO;

public record ErrorMessage(
        String type,
        String errorCode,
        String errorMessage
)
{
    public ErrorMessage(String errorCode, String errorMessage) {
        this("ERROR", errorCode, errorMessage);
    }
}
