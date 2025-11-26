package dev.community.onlineplayerserverapi.models;

import lombok.Data;

import java.util.Set;

@Data
public class PlayerDetailsRequestDto {
    private Set<String> includes;
    private Set<String> excludes;
    private Integer page;
    private Integer pageSize;
}
