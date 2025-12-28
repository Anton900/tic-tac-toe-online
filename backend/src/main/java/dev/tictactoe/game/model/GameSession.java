package dev.tictactoe.game.model;

import dev.tictactoe.exception.GameException;
import dev.tictactoe.game.engine.TicTacToeGame;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class GameSession
{
    private String gameId;
    private TicTacToeGame ticTacToeGame = new TicTacToeGame();
    private User playerX;
    private User playerO;
    private Set<User> spectators = new HashSet<>();

    private boolean rematchCreated = false;

    public synchronized boolean markRematchCreated()
    {
        if (rematchCreated)
        {
            return false;
        }
        rematchCreated = true;
        return true;
    }

    public void assignPlayer(User user)
    {
        if (playerX == null)
        {
            playerX = user;
        }
        if( playerO == null && !user.equals(playerX))
        {
            playerO = user;
        }
        updateStatus();
    }

    public void assignPlayerByMark(Mark mark, User user)
    {
        if (mark == Mark.O)
        {
            playerO = user;
        }
        else if (mark == Mark.X)
        {
            playerX = user;
        }
        updateStatus();
    }

    public boolean playersAssigned()
    {
        return playerX != null && playerO != null;
    }

    public void addSpectator(User user)
    {
        spectators.add(user);
    }

    private void updateStatus()
    {
        if (playerX != null && playerO != null)
        {
            ticTacToeGame.setStatus(GameStatus.IN_PROGRESS);
        }
    }

    public Mark getPlayerMark(User user)
    {
        if (user.equals(playerX))
        {
            return Mark.X;
        }
        else if (user.equals(playerO))
        {
            return Mark.O;
        }
        throw new GameException(
                Response.Status.BAD_REQUEST,
                "NOT_A_PLAYER",
                "User is not a player in this game session"
        );
    }

    public Set<User> getAllUsers()
    {
        Set<User> allUsers = new HashSet<>();
        if (playerX != null) allUsers.add(playerX);
        if (playerO != null) allUsers.add(playerO);
        allUsers.addAll(spectators);
        return allUsers;
    }

    public boolean isUserAPlayer(User user)
    {
        if (user == null || user.id() == null) {
            return false;
        }
        return (playerX != null && user.id().equals(playerX.id()))
                || (playerO != null && user.id().equals(playerO.id()));
    }
}
