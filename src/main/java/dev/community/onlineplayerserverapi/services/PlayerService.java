package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.models.*;

public interface PlayerService {

    LoginResponseDto login(PlayerDto playerDto);

    LoginResponseDto logout(String token);

    RegisterResponseDto register(PlayerDto playerDto);

    boolean isPlayerExisting(String nickName);

    PlayerDetailsResponseDto getPlayerDetailsPage(PlayerDetailsRequestDto playerDetailsRequestDto);

    LoginResponseDto createGame(GameRequestDto gameRequestDto);

    LoginResponseDto joinGame(GameRequestDto gameRequestDto);

    LoginResponseDto leaveGame(GameRequestDto gameRequestDto);
}
