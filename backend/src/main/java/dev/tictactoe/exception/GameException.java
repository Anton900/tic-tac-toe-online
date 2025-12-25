package dev.tictactoe.exception;

import jakarta.ws.rs.core.Response;

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

    public Response.Status getStatus()
    {
        return status;
    }

    public String getCode()
    {
        return code;
    }
}