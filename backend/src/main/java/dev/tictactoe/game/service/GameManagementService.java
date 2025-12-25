package dev.tictactoe.game.service;

import dev.tictactoe.exception.GameException;
import dev.tictactoe.game.model.GameParticipant;
import dev.tictactoe.game.engine.TicTacToeGame;
import dev.tictactoe.game.dto.GameStateDTO;
import dev.tictactoe.game.model.GameRole;
import dev.tictactoe.game.model.GameSession;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class GameManagementService
{
    private final Map<String, GameSession> gameSessions = new ConcurrentHashMap<>();

    public void joinCurrentGame(String gameId, WebSocketConnection connection)
    {
        // Check that a game exists for the given gameId
        TicTacToeGame game = getGame(gameId);
        if (game != null)
        {
            Log.info(("Reusing existing game for gameId=%s").formatted(gameId));
            addParticipant(gameId, connection);
        }
        else
        {
            Log.info(("No active game to join for gameId=%s").formatted(gameId));
            throw new GameException(Response.Status.NOT_FOUND, "GAME_NOT_FOUND", "Game doesn't exist for gameId: " + gameId);
        }
    }

    public void createGame(String gameId, WebSocketConnection connection)
    {
        gameSessions.put(gameId, new GameSession());
        addParticipant(gameId, connection);
    }

    public void addParticipant(String gameId, WebSocketConnection connection)
    {
        Set<GameParticipant> participants = gameSessions.get(gameId).getGameParticipants();

        synchronized (participants)
        {
            // avoid registering the same connection multiple times
            boolean alreadyRegistered = participants.stream()
                    .anyMatch(p -> p.getConnection().equals(connection));
            if (alreadyRegistered)
            {
                Log.info("Connection already registered for gameId=" + gameId);
                return;
            }

            boolean playerXTaken = participants.stream()
                    .anyMatch(p -> p.getRole() == GameRole.PLAYER_X);
            boolean playerOTaken = participants.stream()
                    .anyMatch(p -> p.getRole() == GameRole.PLAYER_O);

            GameRole assignedRole;
            if (!playerXTaken)
            {
                assignedRole = GameRole.PLAYER_X;
            }
            else if (!playerOTaken)
            {
                assignedRole = GameRole.PLAYER_O;
            }
            else
            {
                assignedRole = GameRole.SPECTATOR;
            }

            participants.add(GameParticipant.builder()
                    .connection(connection)
                    .role(assignedRole)
                    .build());

            Log.info("Assigned " + assignedRole + " to connection for gameId=" + gameId + ". Participants: " + participants);
        }
    }

    public void makeMove(String gameId, int position, WebSocketConnection connection)
    {
        TicTacToeGame game = getGame(gameId);
        Set<GameParticipant> participants = gameSessions.get(gameId).getGameParticipants();
        if(participants == null)
        {
            throw new IllegalStateException("No participants found for gameId: " + gameId);
        }

        GameParticipant participant = participants.stream()
                .filter(p -> p.getConnection().equals(connection))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Participant not found for the given connection"));

        try
        {
            game.makeMove(position, participant.getRole().toMark());
        }
        catch (IllegalArgumentException | IllegalStateException e)
        {
            Log.warn("Invalid move attempt by " + participant.getRole() + ": " + e.getMessage());
        }
    }

    public TicTacToeGame getGame(String gameId)
    {
        if(!gameSessions.containsKey(gameId))
        {
            return null;
        }
        return gameSessions.get(gameId).getTicTacToeGame();
    }

    public GameStateDTO getGameState(String gameId)
    {
        TicTacToeGame game = getGame(gameId);
        return GameStateDTO.builder()
                .board(game.getBoard().clone())
                .status(game.getStatus())
                .currentTurn(game.getCurrentTurn())
                .build();
    }
}
