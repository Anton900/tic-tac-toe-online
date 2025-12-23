package service;

import jakarta.enterprise.context.ApplicationScoped;

import java.security.SecureRandom;

@ApplicationScoped
public class GameService
{
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ID_LENGTH = 5;
    private static final SecureRandom random = new SecureRandom();

    public static String generateGameId()
    {
        StringBuilder id = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++)
        {
            id.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return id.toString();
    }

    public String createGameId()
    {
        return generateGameId();
    }
}
