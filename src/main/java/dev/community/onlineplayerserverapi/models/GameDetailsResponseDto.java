package dev.community.onlineplayerserverapi.models;

import lombok.Data;

import java.util.List;

@Data
public class GameDetailsResponseDto {
    private List<GameDetailsDto> gameDetails;
    private int totalNumber;
}
