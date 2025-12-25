package dev.tictactoe.exception.mapper;

import dev.tictactoe.exception.ApiError;
import dev.tictactoe.exception.GameException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GameExceptionMapper implements ExceptionMapper<GameException>
{

    @Override
    public Response toResponse(GameException ex) {
        return Response
                .status(ex.getStatus())
                .entity(new ApiError(
                        ex.getCode(),
                        ex.getMessage()
                ))
                .build();
    }
}