package dev.tictactoe.game.model.server;

public record GameExistMessage(
        String type
) {
    public GameExistMessage() {
        this("GAME_EXIST");
    }
}