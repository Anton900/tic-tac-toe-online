package dev.tictactoe.game.service;

import dev.tictactoe.exception.GameException;
import dev.tictactoe.exception.GameNotFoundException;
import dev.tictactoe.game.model.*;
import dev.tictactoe.game.engine.TicTacToeGame;
import dev.tictactoe.game.dto.GameStateDTO;
import dev.tictactoe.game.model.client.ClientMessage;
import dev.tictactoe.game.model.server.ConnectedMessage;
import dev.tictactoe.game.model.server.GameExistMessage;
import dev.tictactoe.game.model.server.GameStateMessage;
import dev.tictactoe.game.model.server.RematchCreatedMessage;
import dev.tictactoe.game.registry.GameSessionRegistry;
import dev.tictactoe.game.registry.UserRegistry;
import io.quarkus.logging.Log;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class GameManagementService
{
    @Inject
    GameSessionRegistry gameSessionRegistry;

    @Inject
    UserRegistry userRegistry;

    public void handleMessage(ClientMessage clientMessage, WebSocketConnection connection)
    {

        if (clientMessage.actionType == ActionType.CONNECT)
        {
            String displayName = clientMessage.getDisplayName();
            String reconnectToken = clientMessage.getReconnectToken();
            Log.infof("Connecting user with displayName=%s", displayName);
            User user = userRegistry.connectUser(connection, reconnectToken, displayName);
            connection.sendText(new ConnectedMessage(
                    user.displayName(),
                    user.reconnectToken()))
                    .subscribe()
                    .with(
                            ignored -> Log.infof("IGNORED: Game state sent successfully to client for userId=%s", user.id()),
                            failure -> Log.errorf("FAILURE: Failed to send game state to client for userId=%s: %s", user.id(),
                                    failure.getMessage())
                    );
            return;
        }

        String gameId = clientMessage.getGameId();
        validGameId(gameId);

        if (clientMessage.actionType == ActionType.CREATE_GAME)
        {
            Log.infof("Creating new game with gameId=%s", gameId);
            User user = userRegistry.getUserByConnection(connection.id());
            createGame(gameId, connection, user);
            sendGameState(gameId);
        }
        if (clientMessage.actionType == ActionType.JOIN_GAME)
        {
            User user = userRegistry.getUserByConnection(connection.id());
            joinGame(gameId, user);
            sendGameState(gameId);
        }
        if (clientMessage.actionType == ActionType.CREATE_REMATCH)
        {
            String previousGameId = clientMessage.getPreviousGameId();
            validGameId(previousGameId);
            Log.infof("Creating rematch game with gameId=%s and rematchGameId=%s", previousGameId, gameId);
            User user = userRegistry.getUserByConnection(connection.id());
            createRematch(previousGameId, gameId, user);
            sendGameState(gameId); // Send game state for player that clicked on rematch
            sendRematch(previousGameId, gameId); // Send rematch notification to other player and spectators
        }
        if (clientMessage.actionType == ActionType.MAKE_MOVE)
        {
            Log.infof("Making move with gameId=%s at position=%d", gameId, clientMessage.position);
            User user = userRegistry.getUserByConnection(connection.id());
            makeMove(gameId, clientMessage.position, user);
            sendGameState(gameId);
        }
        if (clientMessage.actionType == ActionType.CHECK_GAME_EXISTS)
        {
            Log.infof("Checking if game exists with gameId=%s", gameId);
            boolean doesGameExist = doesGameExist(gameId);
            if (doesGameExist)
            {
                Log.infof("Game exists with gameId=%s", gameId);
                connection.sendText(new GameExistMessage())
                        .subscribe()
                        .with(
                                ignored -> Log.infof("IGNORED: Game state sent successfully to client for gameId=%s", gameId),
                                failure -> Log.errorf("FAILURE: Failed to send game state to client for gameId=%s: %s", gameId,
                                        failure.getMessage())
                        );

            }
        }
    }

    public void validGameId(String gameId)
    {
        if (gameId == null || gameId.isBlank())
        {
            throw new GameException(Response.Status.BAD_REQUEST, "INVALID_GAME_ID", "Game ID is missing or empty");
        }
    }

    public void joinGame(String gameId, User user)
    {
        Log.infof("Joining game with gameId=%s", gameId);
        GameSession gameSession = gameSessionRegistry.getGameSession(gameId);
        TicTacToeGame game = gameSession.getTicTacToeGame();

        if(game == null)
        {
            Log.warnf("No active game to join for gameId=%s", gameId);
            throw new GameNotFoundException(gameId);
        }

        boolean alreadyInGameAsPlayer = gameSession.isUserAPlayer(user);

        if(alreadyInGameAsPlayer)
        {
            Log.infof("User already assigned as player in gameId=%s", gameId);
            return;
        }
        if(gameSession.playersAssigned())
        {
            Log.infof("Both players already assigned for gameId=%s, adding as spectator", gameId);
            gameSession.addSpectator(user);
        }
        else
        {
            Log.infof("Assigning player to gameId=%s", gameId);
            gameSession.assignPlayer(user);
        }

    }

    public boolean joinedCurrentGame(String gameId, WebSocketConnection connection)
    {
        // Check that a game exists for the given gameId
        TicTacToeGame game = getGameFromGameSessions(gameId);
        if (game != null)
        {
            Log.info(("Reusing existing game for gameId=%s").formatted(gameId));

            return true;
        }
        else
        {
            Log.info(("No active game to join for gameId=%s").formatted(gameId));
          //  throw new GameException(Response.Status.NOT_FOUND, "GAME_NOT_FOUND", "Game doesn't exist for gameId: " + gameId);
            return false;
        }
    }

    public void createGame(String gameId, WebSocketConnection connection, User user)
    {
        GameSession newGameSession = new GameSession();
        newGameSession.setGameId(gameId);
        newGameSession.assignPlayer(user);
        gameSessionRegistry.getGameSessions().put(gameId, newGameSession);
    }

    public void createRematch(String previousGameId, String newGameId, User user)
    {
        GameSession oldGameSession = gameSessionRegistry.getGameSession(previousGameId);
        GameSession newGameSession = new GameSession();

        if(oldGameSession.markRematchCreated())
        {
            newGameSession.setGameId(newGameId);
            newGameSession.assignPlayerX(oldGameSession.getPlayerO());
            newGameSession.assignPlayerO(oldGameSession.getPlayerX());
            newGameSession.setSpectators(oldGameSession.getSpectators());
            newGameSession.updateStatus();
            gameSessionRegistry.addGameSession(newGameId, newGameSession);
        }
    }

    public void makeMove(String gameId, int position, User user)
    {
        GameSession gameSession = gameSessionRegistry.getGameSession(gameId);
        TicTacToeGame game = gameSession.getTicTacToeGame();
        if(!gameSession.playersAssigned())
        {
            throw new GameException(Response.Status.NOT_FOUND, "NOT_ALL_PLATYERS_OFUND", "Game is missing players");
        }

        Mark currentPlayersMark = gameSession.getPlayerMark(user);
        game.makeMove(position, currentPlayersMark);
    }

    public GameSession getGameSession(String gameId)
    {
        Map<String, GameSession> gameSessions = gameSessionRegistry.getGameSessions();
        if(!gameSessions.containsKey(gameId))
        {
            return null;
        }
        return gameSessions.get(gameId);
    }

    public TicTacToeGame getGameFromGameSessions(String gameId)
    {
        Map<String, GameSession> gameSessions = gameSessionRegistry.getGameSessions();
        if(!gameSessions.containsKey(gameId))
        {
            return null;
        }
        return gameSessions.get(gameId).getTicTacToeGame();
    }

    public GameStateDTO getGameState(String gameId)
    {
        GameSession gameSession = getGameSession(gameId);

        if (gameSession == null || gameSession.getTicTacToeGame() == null)
        {
            throw new GameException(Response.Status.NOT_FOUND, "GAME_NOT_FOUND", "Game session doesn't exist for gameId: " + gameId);
        }

        User playerX = gameSession.getPlayerX();
        User playerO = gameSession.getPlayerO();
        String playerXName = (playerX != null) ? playerX.displayName() : null;
        String playerOName = (playerO != null) ? playerO.displayName() : null;

        TicTacToeGame game = gameSession.getTicTacToeGame();

        return GameStateDTO.builder()
                .board(game.getBoard().clone())
                .status(game.getStatus())
                .currentTurn(game.getCurrentTurn())
                .playerX(playerXName)
                .playerO(playerOName)
                .build();
    }

    public void removeUser(WebSocketConnection connection)
    {
        userRegistry.removeConnection(connection);
    }

    public boolean doesGameExist(String gameId)
    {
        return gameSessionRegistry.getGameSessions().containsKey(gameId);
    }

    public void sendGameState(String gameId)
    {
        GameStateDTO gameState = getGameState(gameId);
        GameSession gameSession = getGameSession(gameId);
        Set<User> gameSessionUsers = gameSession.getAllUsers();
        for (User user : gameSessionUsers)
        {
            try
            {
                GameStateMessage gameStateMessage = new GameStateMessage(gameState);
                Log.infof("Sending updated game state to client for gameId=%s", gameId);
                Set<WebSocketConnection> connections = userRegistry.getUserConnections(user.id());
                for (WebSocketConnection connection : connections)
                {
                    Log.infof("Sending game state to userId=%s on connectionId=%s for gameId=%s", user.id(), connection.id(), gameId);
                    connection.sendText(gameStateMessage)
                            .subscribe()
                            .with(
                                    ignored -> Log.infof("IGNORED: Game state sent successfully to client for gameId=%s", gameId),
                                    failure -> Log.errorf("FAILURE: Failed to send game state to client for gameId=%s: %s", gameId,
                                            failure.getMessage())
                            );
                }
            } catch (Exception e)
            {
                Log.error("Error sending game state to client", e);
            }
        }
    }

    public void sendRematch(String oldGameId, String newGameId)
    {
        GameSession gameSession = getGameSession(oldGameId);
        Set<User> gameSessionUsers = gameSession.getAllUsers();

        for (User user : gameSessionUsers)
        {
            try
            {
                RematchCreatedMessage rematchCreatedMessage = new RematchCreatedMessage(newGameId);
                Log.infof("Sending rematch created message to client for oldGameId=%s and newGameId=%s", oldGameId, newGameId);
                Set<WebSocketConnection> connections = userRegistry.getUserConnections(user.id());
                for (WebSocketConnection connection : connections)
                {
                    Log.infof("Sending game state to userId=%s on connectionId=%s for gameId=%s", user.id(), connection.id(), newGameId);
                    connection.sendText(rematchCreatedMessage)
                            .subscribe()
                            .with(
                                    ignored -> Log.infof("IGNORED: message sent successfully to client for gameId=%s", newGameId),
                                    failure -> Log.errorf("FAILURE: Failed to send message to client for gameId=%s: %s", newGameId,
                                            failure.getMessage())
                            );
                }
            } catch (Exception e)
            {
                Log.error("Error sending game state to client", e);
            }
        }
    }

}
