package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.entities.Game;
import dev.community.onlineplayerserverapi.models.GameDetailsResponseDto;
import dev.community.onlineplayerserverapi.models.PlayerDetailsRequestDto;

public interface GameService {
    Game createGame(String name, Long hostPlayerId);
    Game joinGame(String name, Long playerId);
    void leaveGame(String name, Long playerId);
    GameDetailsResponseDto getGameDetails(PlayerDetailsRequestDto playerDetailsRequestDto);
}
