package dev.tictactoe.game.service;

import dev.tictactoe.game.model.GameParticipant;
import dev.tictactoe.game.engine.TicTacToeGame;
import dev.tictactoe.game.dto.GameStateDTO;
import dev.tictactoe.game.model.GameRole;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class GameManagementService
{
    private final Map<String, TicTacToeGame> gameList = new ConcurrentHashMap<>();
    private final Map<String, Set<GameParticipant>> participantsList = new ConcurrentHashMap<>();


    public void manageGameOnOpen(String gameId, WebSocketConnection connection)
    {
        TicTacToeGame game = getGame(gameId);
        if (game == null)
        {
            Log.info(("Creating new game for gameId=%s").formatted(gameId));
            createGame(gameId, connection);
        }
        else
        {
            Log.info(("Reusing existing game for gameId=%s").formatted(gameId));
        }
        addParticipant(gameId, connection);
    }

    public void createGame(String gameId, WebSocketConnection connection)
    {
        Log.info("CREATE Participants List: " + participantsList);
        gameList.put(gameId, new TicTacToeGame());
        participantsList.put(gameId, ConcurrentHashMap.newKeySet());
        participantsList.get(gameId).add(GameParticipant.builder()
                .connection(connection)
                .role(GameRole.PLAYER_X)
                .build());
        Log.info("CREATE AFTER Participants List: " + participantsList);
    }

    public void addParticipant(String gameId, WebSocketConnection connection)
    {
        Log.info("ADD Participants List: " + participantsList);
        Set<GameParticipant> participants = participantsList.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet());

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
        Log.info("Participants List: " + participantsList);
        TicTacToeGame game = getGame(gameId);
        Set<GameParticipant> gameParticipants = participantsList.get(gameId);
        if(gameParticipants == null)
        {
            throw new IllegalStateException("No participants found for gameId: " + gameId);
        }

        GameParticipant participant = gameParticipants.stream()
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
        TicTacToeGame game = gameList.get(gameId);
        return game;
    }

    public Set<GameParticipant> getParticpants(String gameId)
    {
        return participantsList.get(gameId);
    }

    public void removeGame(String gameId)
    {
        gameList.remove(gameId);
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
