package dev.tictactoe.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public class GameException extends RuntimeException
{

    private final Response.Status status;
    private final String code;

    public GameException(
            Response.Status status,
            String code,
            String message
    )
    {
        super(message);
        this.status = status;
        this.code = code;
    }
}