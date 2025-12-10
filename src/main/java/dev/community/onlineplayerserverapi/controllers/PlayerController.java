package dev.community.onlineplayerserverapi.controllers;

import dev.community.onlineplayerserverapi.models.*;
import dev.community.onlineplayerserverapi.services.PlayerService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/players")
@AllArgsConstructor
public class PlayerController {

    private PlayerService playerService;

    @GetMapping("/player-exists")
    public Boolean playerExists(@RequestParam String nickName) {
        return playerService.isPlayerExisting(nickName);
    }

    @GetMapping("/player-details")
    public PlayerDetailsResponseDto playerDetails(@RequestBody PlayerDetailsRequestDto playerDetailsRequestDto) {
        return playerService.getPlayerDetailsPage(playerDetailsRequestDto);
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody PlayerDto playerDto) {
        return playerService.login(playerDto);
    }

    @PostMapping("/logout")
    public LoginResponseDto logout(@RequestBody String token) {
        return playerService.logout(token);
    }

    @PutMapping("/register")
    public RegisterResponseDto register(@RequestBody PlayerDto playerDto) {
        return playerService.register(playerDto);
    }

    @PostMapping("/game-create")
    public LoginResponseDto createGame(@RequestBody GameRequestDto gameRequestDto) {
        return playerService.createGame(gameRequestDto);
    }

    @PostMapping("/game-join")
    public LoginResponseDto joinGame(@RequestBody GameRequestDto gameRequestDto) {
        return playerService.joinGame(gameRequestDto);
    }

    @PostMapping("/game-leave")
    public LoginResponseDto leaveGame(@RequestBody GameRequestDto gameRequestDto) {
        return playerService.leaveGame(gameRequestDto);
    }
}
