package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.models.LoginResponseDto;
import dev.community.onlineplayerserverapi.models.PlayerDto;
import dev.community.onlineplayerserverapi.models.RegisterResponseDto;

public interface PlayerService {

    LoginResponseDto login(PlayerDto playerDto);

    RegisterResponseDto register(PlayerDto playerDto);

    boolean isPlayerExisting(String nickName);
}
