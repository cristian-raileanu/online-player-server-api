package dev.community.onlineplayerserverapi.models;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GameDetailsDto {
    private String gameName;
    private Long duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<GameTeamDetailsDto> teamsDetails;
}
