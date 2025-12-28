package dev.tictactoe.game.model.client;

import dev.tictactoe.game.model.ActionType;
import lombok.Getter;

@Getter
public class ClientMessage
{
    public ActionType actionType;
    public String gameId;
    public String previousGameId;
    public Integer position;
    public String displayName;
    public String reconnectToken;
}
