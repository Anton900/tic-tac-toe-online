package dev.tictactoe.game.model;

import io.quarkus.websockets.next.WebSocketConnection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GameParticipant
{
    WebSocketConnection connection;
    GameRole role;
}
