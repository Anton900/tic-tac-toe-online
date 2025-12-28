package dev.tictactoe.game.model.server;

public record ConnectedMessage(
        String type,
        String displayName,
        String reconnectToken
) {
    public ConnectedMessage(String displayName, String reconnectToken) {
        this("CONNECTED", displayName, reconnectToken);
    }
}