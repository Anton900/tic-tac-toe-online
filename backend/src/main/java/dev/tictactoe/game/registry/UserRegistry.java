package dev.tictactoe.game.registry;

import dev.tictactoe.game.model.User;
import io.quarkus.websockets.next.WebSocketConnection;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class UserRegistry
{

    private final Map<String, User> usersByToken = new ConcurrentHashMap<>();
    private final Map<String, Set<WebSocketConnection>> connectionsByUserId = new ConcurrentHashMap<>();
    private final Map<String, User> userByConnectionId = new ConcurrentHashMap<>();

    public User connectUser(WebSocketConnection connection, String reconnectToken, String displayName)
    {
        User user = null;
        if(reconnectToken != null && usersByToken.containsKey(reconnectToken))
        {
            user = getUserWithToken(reconnectToken);
        }
        else
        {
            user = new User(
                    UUID.randomUUID().toString(), // creates a unique user ID
                    displayName, // uses the provided display name
                    UUID.randomUUID().toString() // creates a unique reconnect token
            );
            usersByToken.put(user.reconnectToken(), user);
        }
        connectionsByUserId
                .computeIfAbsent(user.id(), id -> ConcurrentHashMap.newKeySet())
                .add(connection);
        userByConnectionId.put(connection.id(), user);
        return user;

    }

    public Set<WebSocketConnection> getUserConnections(String userId)
    {
        return connectionsByUserId.getOrDefault(userId, Set.of());
    }

    public User getUserWithToken(String reconnectToken)
    {
        return usersByToken.get(reconnectToken);
    }

    public void removeConnection(WebSocketConnection connection)
    {
        User user = getUserByConnection(connection.id());
        if (user == null)
        {
            return;
        }
        String userId = user.id();
        userByConnectionId.remove(connection.id());

        Set<WebSocketConnection> connections = connectionsByUserId.get(userId);
        if (connections != null)
        {
            connections.remove(connection);
            if (connections.isEmpty())
            {
                connectionsByUserId.remove(userId);
            }
        }
    }

    public User getUserByConnection(String connectionId)
    {
        return userByConnectionId.get(connectionId);
    }
}

