package dev.tictactoe.exception;

import jakarta.ws.rs.core.Response;

public class InvalidMoveException extends GameException {

    public InvalidMoveException(String message) {
        super(
                Response.Status.CONFLICT,
                "INVALID_MOVE",
                "Illegal move - " + message
        );
    }
}