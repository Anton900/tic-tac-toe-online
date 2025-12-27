package dev.tictactoe.exception;

import jakarta.ws.rs.core.Response;

public class GameNotFoundException extends GameException {

    public GameNotFoundException(String gameId) {
        super(
                Response.Status.NOT_FOUND,
                "GAME_NOT_FOUND",
                "Game with id " + gameId + " does not exist"
        );
    }
}