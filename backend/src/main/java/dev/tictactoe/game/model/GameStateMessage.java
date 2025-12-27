package dev.tictactoe.game.model;

import dev.tictactoe.game.dto.GameStateDTO;

public record GameStateMessage(
        String type,
        GameStateDTO gameState
) {
    public GameStateMessage(GameStateDTO gameState) {
        this("GAME_STATE", gameState);
    }
}