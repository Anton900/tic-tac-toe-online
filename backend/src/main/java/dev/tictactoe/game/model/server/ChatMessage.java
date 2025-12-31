package dev.tictactoe.game.model.server;

public record ChatMessage(
        String type,
        String displayName,
        String chatMessage
)
{
    public ChatMessage(String displayName, String chatMessage)
    {
        this("CHAT_MESSAGE", displayName, chatMessage);
    }
}