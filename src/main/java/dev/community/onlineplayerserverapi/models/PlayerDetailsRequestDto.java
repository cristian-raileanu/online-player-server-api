package dev.community.onlineplayerserverapi.models;

import lombok.Data;

import java.util.Set;

@Data
public class PlayerDetailsRequestDto {
    private Set<String> includes;
    private Set<String> excludes;
    private PlayerFilterDto filter;
    private Integer page;
    private Integer pageSize;
    private String playerToken;
}
