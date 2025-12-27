package dev.tictactoe.game.model.server;

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
