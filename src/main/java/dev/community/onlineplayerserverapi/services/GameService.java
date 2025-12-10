package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.entities.Game;

public interface GameService {
    Game createGame(String name, Long hostPlayerId);
    Game joinGame(String name, Long playerId);
    void leaveGame(String name, Long playerId);
}
