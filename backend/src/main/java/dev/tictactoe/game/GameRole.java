package dev.tictactoe.game;

public enum GameRole
{
    PLAYER_X,
    PLAYER_O,
    SPECTATOR;

    public boolean isPlayer()
    {
        return this == PLAYER_X || this == PLAYER_O;
    }

    public Mark toMark()
    {
        if (this == PLAYER_X) return Mark.X;
        if (this == PLAYER_O) return Mark.O;
        throw new IllegalStateException("Spectators do not have a mark");
    }
}