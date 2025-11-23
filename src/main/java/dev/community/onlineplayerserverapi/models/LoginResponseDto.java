package dev.community.onlineplayerserverapi.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private LoginStatus loginStatus;
    private String token;
    private String message;
}
