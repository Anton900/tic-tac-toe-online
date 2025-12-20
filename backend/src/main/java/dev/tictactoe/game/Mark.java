package dev.tictactoe.game;

public enum Mark
{
    X, O;

    public Mark opposite()
    {
        return (this == X) ? O : X;
    }
}
