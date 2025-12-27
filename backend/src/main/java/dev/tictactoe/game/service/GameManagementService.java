package dev.tictactoe.game.service;

import dev.tictactoe.exception.GameException;
import dev.tictactoe.exception.GameNotFoundException;
import dev.tictactoe.game.model.*;
import dev.tictactoe.game.engine.TicTacToeGame;
import dev.tictactoe.game.dto.GameStateDTO;
import dev.tictactoe.game.model.client.ClientMessage;
import dev.tictactoe.game.model.server.GameStateMessage;
import dev.tictactoe.game.model.server.RematchCreatedMessage;
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

    public void handleMessage(ClientMessage clientMessage, WebSocketConnection connection)
    {
        String gameId = clientMessage.getGameId();
        if (gameId == null || gameId.isBlank())
        {
            Log.warn("Received message with missing or empty gameId");
            return;
        }

        if (clientMessage.actionType == ActionType.CREATE_GAME)
        {
            Log.infof("Creating new game with gameId=%s", gameId);
            createGame(gameId, connection);
            sendGameState(gameId);
        }
        if (clientMessage.actionType == ActionType.JOIN_GAME)
        {
            joinGame(gameId, connection);
            sendGameState(gameId);
        }
        if (clientMessage.actionType == ActionType.CREATE_REMATCH)
        {
            String previousGameId = clientMessage.getPreviousGameId();
            Log.infof("Creating rematch game with gameId=%s and rematchGameId=%s", previousGameId, gameId);
            createRematch(gameId, connection);
            sendGameState(gameId); // Send game state for player that clicked on rematch
            sendRematch(previousGameId, gameId); // Send rematch notification to other player and spectators
        }
        if (clientMessage.actionType == ActionType.MAKE_MOVE)
        {
            Log.infof("Making move with gameId=%s at position=%d", gameId, clientMessage.position);
            makeMove(gameId, clientMessage.position, connection);
            sendGameState(gameId);
        }
    }

    public void joinGame(String gameId, WebSocketConnection connection)
    {
        Log.infof("Joining game with gameId=%s", gameId);
        TicTacToeGame game = getGame(gameId);
        if(game == null)
        {
            Log.warnf("No active game to join for gameId=%s", gameId);
            throw new GameNotFoundException(gameId);
        }
        addParticipant(gameId, connection);
    }

    public boolean joinedCurrentGame(String gameId, WebSocketConnection connection)
    {
        // Check that a game exists for the given gameId
        TicTacToeGame game = getGame(gameId);
        if (game != null)
        {
            Log.info(("Reusing existing game for gameId=%s").formatted(gameId));
            addParticipant(gameId, connection);
            return true;
        }
        else
        {
            Log.info(("No active game to join for gameId=%s").formatted(gameId));
          //  throw new GameException(Response.Status.NOT_FOUND, "GAME_NOT_FOUND", "Game doesn't exist for gameId: " + gameId);
            return false;
        }
    }

    public void createGame(String gameId, WebSocketConnection connection)
    {
        gameSessions.put(gameId, new GameSession());
        addParticipant(gameId, connection);
    }

    public void createRematch(String newGameId, WebSocketConnection connection)
    {
        GameSession gameSesson = new GameSession();
        gameSesson.markRematchCreated();
        gameSessions.put(newGameId, gameSesson);
        addParticipant(newGameId, connection);
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
            throw new GameException(Response.Status.NOT_FOUND, "NO_PARTICAPNTS_FOUND", "No participants found for gameId: " + gameId);
        }

        GameParticipant participant = participants.stream()
                .filter(p -> p.getConnection().equals(connection))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Participant not found for the given connection"));

        game.makeMove(position, participant.getRole().toMark());
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
        if (game == null)
        {
            throw new GameException(Response.Status.NOT_FOUND, "GAME_NOT_FOUND", "Game doesn't exist for gameId: " + gameId);
        }
        return GameStateDTO.builder()
                .board(game.getBoard().clone())
                .status(game.getStatus())
                .currentTurn(game.getCurrentTurn())
                .build();
    }

    public void removeParticipant(WebSocketConnection connection)
    {
        for (Map.Entry<String, GameSession> entry : gameSessions.entrySet())
        {
            String gameId = entry.getKey();
            GameSession session = entry.getValue();
            Set<GameParticipant> participants = session.getGameParticipants();

            synchronized (participants)
            {
                boolean removed = participants.removeIf(p -> p.getConnection().equals(connection));
                if (removed)
                {
                    Log.info("Removed participant from gameId=" + gameId);
                }
            }
        }
    }

    public Set<GameParticipant> getGameParticpants(String gameId)
    {
        GameSession session = gameSessions.get(gameId);
        if (session == null)
        {
            throw new GameException(Response.Status.NOT_FOUND, "GAME_NOT_FOUND", "Game doesn't exist for gameId: " + gameId);
        }
        return session.getGameParticipants();
    }

    public void sendGameState(String gameId)
    {
        GameStateDTO gameState = getGameState(gameId);
        Set<GameParticipant> gameParticipants = getGameParticpants(gameId);
        for (GameParticipant gameParticipant : gameParticipants)
        {
            try
            {
                GameStateMessage gameStateMessage = new GameStateMessage(gameState);
                Log.infof("Sending updated game state to client for gameId=%s", gameId);
                gameParticipant.getConnection().sendText(gameStateMessage)
                        .subscribe()
                        .with(
                                ignored -> Log.infof("IGNORED: Game state sent successfully to client for gameId=%s", gameId),
                                failure -> Log.errorf("FAILURE: Failed to send game state to client for gameId=%s: %s", gameId,
                                        failure.getMessage())
                        );
            } catch (Exception e)
            {
                Log.error("Error sending game state to client", e);
            }
        }
    }

    public void sendRematch(String oldGameId, String newGameId)
    {
        Set<GameParticipant> gameParticipants = getGameParticpants(oldGameId);
        for (GameParticipant gameParticipant : gameParticipants)
        {
            try
            {
                RematchCreatedMessage rematchCreatedMessage = new RematchCreatedMessage(newGameId);
                Log.infof("Sending rematch created message to client for oldGameId=%s and newGameId=%s", oldGameId, newGameId);
                gameParticipant.getConnection().sendText(rematchCreatedMessage)
                        .subscribe()
                        .with(
                                ignored -> Log.infof("IGNORED: message sent successfully to client for gameId=%s", newGameId),
                                failure -> Log.errorf("FAILURE: Failed to send message to client for gameId=%s: %s", newGameId,
                                        failure.getMessage())
                        );
            } catch (Exception e)
            {
                Log.error("Error sending message to client", e);
            }
        }
    }

}
