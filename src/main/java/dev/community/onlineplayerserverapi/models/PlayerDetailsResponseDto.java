package dev.community.onlineplayerserverapi.models;

import lombok.Data;

import java.util.List;

@Data
public class PlayerDetailsResponseDto {

    private List<PlayerDetailsDto> playerDetails;

    int totalPlayers;
}
