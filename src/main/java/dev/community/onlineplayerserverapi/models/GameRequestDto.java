package dev.community.onlineplayerserverapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRequestDto {
    private String gameName;
    private String playerToken;
}
