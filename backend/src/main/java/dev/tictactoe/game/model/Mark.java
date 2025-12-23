package dev.tictactoe.game.model;

public enum Mark
{
    X, O;

    public Mark opposite()
    {
        return (this == X) ? O : X;
    }
}
