package dev.tictactoe.game.model.server;

public record RematchCreatedMessage(
        String type,
        String rematchGameId
) {
    public RematchCreatedMessage(String gameId) {
        this("REMATCH_CREATED", gameId);
    }
}