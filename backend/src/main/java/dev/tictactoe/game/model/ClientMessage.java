package dev.tictactoe.game.model;

import lombok.Getter;

@Getter
public class ClientMessage
{
    public ActionType actionType;
    public String gameId;
    public Integer position;
}
