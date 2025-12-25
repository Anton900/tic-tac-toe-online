package dev.tictactoe.exception;

public record ApiError(
        String code,
        String message
) {}