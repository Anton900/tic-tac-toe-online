package dev.tictactoe.game.engine;

import dev.tictactoe.exception.GameException;
import dev.tictactoe.exception.InvalidMoveException;
import dev.tictactoe.game.model.GameStatus;
import dev.tictactoe.game.model.Mark;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

public class TicTacToeGame
{
    private static final int BOARD_SIZE = 9;

    private final Mark[] board;
    @Getter
    private Mark currentTurn;
    @Getter
    private GameStatus status;

    public TicTacToeGame()
    {
        this.board = new Mark[BOARD_SIZE];
        this.currentTurn = Mark.X;
        this.status = GameStatus.IN_PROGRESS;
    }

    public void makeMove(int position, Mark role)
    {
        if (role != currentTurn)
        {
            Log.info("It's not " + role + "'s turn. Current turn: " + currentTurn);
            return;
        }
        validateMove(position);
        board[position] = currentTurn;
        updateGameStatus();
        if(status == GameStatus.IN_PROGRESS)
        {
            switchTurn();
        }
    }

    public Mark[] getBoard()
    {
        return board.clone();
    }

    private void validateMove(int position)
    {
        if (status != GameStatus.IN_PROGRESS)
        {
            throw new InvalidMoveException("Game is already finished");
        }
        if (position < 0 || position >= BOARD_SIZE)
        {
            throw new InvalidMoveException("Invalid board position");
        }
        if (board[position] != null)
        {
            throw new InvalidMoveException("Position already taken");
        }
    }

    private void validateRole(Mark role, Mark expectedRole)
    {
        if (role != expectedRole)
        {
            throw new GameException(
                    Response.Status.BAD_REQUEST,
                    "NOT_YOUR_TURN",
                    "It's not " + role + "'s turn."
            );
        }
    }

    private static final int[][] WINNING_COMBINATIONS = {
            {0,1,2},{3,4,5},{6,7,8},
            {0,3,6},{1,4,7},{2,5,8},
            {0,4,8},{2,4,6}
    };

    private void updateGameStatus()
    {
        for (int[] combo : WINNING_COMBINATIONS)
        {
            Mark m = board[combo[0]];
            if (m != null &&
                    m == board[combo[1]] &&
                    m == board[combo[2]])
            {
                status = m == Mark.X ? GameStatus.X_WON : GameStatus.O_WON;
                return;
            }
        }

        // Check for draw, if all positions are filled, no winner
        if (Arrays.stream(board).noneMatch(Objects::isNull))
        {
            status = GameStatus.DRAW;
        }
    }

    private void switchTurn()
    {
        currentTurn = currentTurn.opposite();
    }

}
