package dev.community.onlineplayerserverapi.controllers;

import dev.community.onlineplayerserverapi.models.LoginResponseDto;
import dev.community.onlineplayerserverapi.models.PlayerDto;
import dev.community.onlineplayerserverapi.models.RegisterResponseDto;
import dev.community.onlineplayerserverapi.services.PlayerService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players")
@AllArgsConstructor
public class PlayerController {

    private PlayerService playerService;

    @GetMapping("/login")
    public LoginResponseDto login(@RequestBody PlayerDto playerDto) {
        return playerService.login(playerDto);
    }

    @GetMapping("/player-exists")
    public Boolean playerExists(@RequestBody PlayerDto playerDto) {
        return playerService.isPlayerExisting(playerDto.getNickName());
    }

    @PutMapping("/register")
    public RegisterResponseDto register(@RequestBody PlayerDto playerDto) {
        return playerService.register(playerDto);
    }
}
