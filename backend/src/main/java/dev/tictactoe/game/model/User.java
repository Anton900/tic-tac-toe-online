package dev.tictactoe.game.model;

public record User(
        String id,
        String displayName,
        String reconnectToken
) {
}
