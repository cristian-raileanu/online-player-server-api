package dev.community.onlineplayerserverapi.models;

import lombok.Data;

import java.util.List;

@Data
public class PlayerFilterDto {
    private List<String> nickNames;
}
