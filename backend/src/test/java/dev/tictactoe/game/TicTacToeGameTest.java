package dev.tictactoe.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TicTacToeGameTest
{
/*
    @Test
    public void createNewGame()
    {
        TicTacToeGame game = new TicTacToeGame();
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals(Mark.X, game.getCurrentTurn());
        Mark[] board = game.getBoard();
        for (Mark m : board)
        {
            assertNull(m);
        }
    }

    @Test
    public void makeMoveAndSwitchTurn()
    {
        TicTacToeGame game = new TicTacToeGame();
        game.makeMove(0, );
        Mark[] board = game.getBoard();
        assertEquals(Mark.X, board[0]);
        assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        assertEquals(Mark.O, game.getCurrentTurn());
    }

    @Test
    public void xWinsAndTurnNotSwitchedAfterWin()
    {
        TicTacToeGame game = new TicTacToeGame();
        // sequence: X(0), O(1), X(4), O(2), X(8) -> diagonal 0-4-8 wins for X
        game.makeMove(0, ); // X
        game.makeMove(1, ); // O
        game.makeMove(4, ); // X
        game.makeMove(2, ); // O
        game.makeMove(8, ); // X wins
        assertEquals(GameStatus.X_WON, game.getStatus());
        // currentTurn should remain the winning player (X)
        assertEquals(Mark.X, game.getCurrentTurn());
    }

    @Test
    public void drawDetectedWhenBoardFullNoWinner()
    {
        TicTacToeGame game = new TicTacToeGame();
        // moves lead to a draw
        int[] moves = {0, 1, 2, 4, 3, 5, 7, 6, 8};
        for (int pos : moves)
        {
            game.makeMove(pos, );
        }
        assertEquals(GameStatus.DRAW, game.getStatus());
        // last move was by X (since X starts), and turn should not flip after finished
        assertEquals(Mark.X, game.getCurrentTurn());
    }

    @Test
    public void invalidPositionThrows()
    {
        TicTacToeGame game = new TicTacToeGame();
        assertThrows(IllegalArgumentException.class, () -> game.makeMove(-1, ));
        assertThrows(IllegalArgumentException.class, () -> game.makeMove(9, ));
    }

    @Test
    public void positionAlreadyTakenThrows()
    {
        TicTacToeGame game = new TicTacToeGame();
        game.makeMove(0, );
        assertThrows(IllegalArgumentException.class, () -> game.makeMove(0, ));
    }

    @Test
    public void moveAfterFinishedThrows()
    {
        TicTacToeGame game = new TicTacToeGame();
        // X wins with top row: 0,1,2
        game.makeMove(0, ); // X
        game.makeMove(3, ); // O
        game.makeMove(1, ); // X
        game.makeMove(4, ); // O
        game.makeMove(2, ); // X wins
        assertEquals(GameStatus.X_WON, game.getStatus());
        assertThrows(IllegalStateException.class, () -> game.makeMove(5, ));
    }
 */
}
