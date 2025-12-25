package dev.tictactoe.resources;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import dev.tictactoe.service.GameService;

@Path("/game")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameResource
{
    @Inject
    GameService gameService;

    @GET
    @Path("/createGameId")
    public String createGameId()
    {
        Log.info("Creating new game ID");
        return gameService.createGameId();
    }
}