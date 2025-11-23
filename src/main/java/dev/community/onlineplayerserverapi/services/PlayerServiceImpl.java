package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.entities.Player;
import dev.community.onlineplayerserverapi.mappers.PlayerMapper;
import dev.community.onlineplayerserverapi.models.LoginResponseDto;
import dev.community.onlineplayerserverapi.models.LoginStatus;
import dev.community.onlineplayerserverapi.models.PlayerDto;
import dev.community.onlineplayerserverapi.models.RegisterResponseDto;
import dev.community.onlineplayerserverapi.repositories.PlayerRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private PlayerRepository playerRepository;

    private PlayerMapper playerMapper;

    @Override
    public LoginResponseDto login(PlayerDto playerDto) {

        Optional<Player> foundPlayer = playerRepository.findByNickName(playerDto.getNickName());

        if (foundPlayer.isEmpty()) {
            return LoginResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Player not registered!")
                    .build();
        } else if (!foundPlayer.get().getPasswordHash().equals(playerDto.getPasswordHash())) {
            return LoginResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Wrong password!")
                    .build();
        }

        return LoginResponseDto.builder()
                .loginStatus(LoginStatus.SUCCESS)
                .token(getToken(foundPlayer.get()))
                .build();
    }

    private String getToken(Player player) {
        return player.getNickName() + player.hashCode();
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public RegisterResponseDto register(PlayerDto playerDto) {

        if (doesPlayerWithNicknameExist(playerDto.getNickName())) {
            return RegisterResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Nickname already exists!")
                    .build();
        }
        if (doesPlayerWithEmailExist(playerDto.getEmail())) {
            return RegisterResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Email already used!")
                    .build();
        }
        if (playerDto.getPasswordHash().isEmpty()) {
            return RegisterResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Password empty!")
                    .build();
        }

        Player createdPlayer = playerMapper.toEntity(playerDto);

        Player savedPlayer = playerRepository.save(createdPlayer);

        return RegisterResponseDto.builder()
                .loginStatus(LoginStatus.SUCCESS)
                .token(getToken(savedPlayer))
                .build();
    }

    @Override
    public boolean isPlayerExisting(String nickName) {
        return doesPlayerWithNicknameExist(nickName);
    }

    private boolean doesPlayerWithNicknameExist(String nickName) {
        return playerRepository.findByNickName(nickName).isPresent();
    }
    private boolean doesPlayerWithEmailExist(String email) {
        return playerRepository.findByEmail(email).isPresent();
    }

}
