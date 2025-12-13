package dev.community.onlineplayerserverapi.models;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GameTeamDetailsDto {
    private List<String> players;
    private LocalDateTime exitTime;
    private GameResult result;
}
