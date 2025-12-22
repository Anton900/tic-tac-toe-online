package dev.tictactoe.game;

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

    public void makeMove(int position)
    {
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
            throw new IllegalStateException("Game is already finished");
        }
        if (position < 0 || position >= BOARD_SIZE)
        {
            throw new IllegalArgumentException("Invalid board position");
        }
        if (board[position] != null)
        {
            throw new IllegalArgumentException("Position already taken");
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
