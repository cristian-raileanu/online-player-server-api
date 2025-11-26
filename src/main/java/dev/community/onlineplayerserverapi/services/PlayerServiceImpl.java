package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.entities.Player;
import dev.community.onlineplayerserverapi.mappers.PlayerMapper;
import dev.community.onlineplayerserverapi.models.*;
import dev.community.onlineplayerserverapi.repositories.PlayerRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @Override
    public PlayerDetailsResponseDto getPlayerDetailsPage(PlayerDetailsRequestDto playerDetailsRequestDto) {
        boolean showAll = playerDetailsRequestDto.getPage() == null || playerDetailsRequestDto.getPageSize() == null;

        List<Player> selectedPlayers = showAll ? playerRepository.findAll() : List.of();
        Set<String> includedFields = playerDetailsRequestDto.getIncludes() != null ?
                playerDetailsRequestDto.getIncludes() : Set.of();

        PlayerDetailsResponseDto playerDetailsResponseDto = new PlayerDetailsResponseDto();
        playerDetailsResponseDto.setPlayerDetails(selectedPlayers.stream()
                .map(player -> playerDetailsPartialMap(player, includedFields))
                .toList());
        playerDetailsResponseDto.setTotalPlayers(showAll ? playerDetailsResponseDto.getPlayerDetails().size() :
                (int) playerRepository.count());
        return playerDetailsResponseDto;
    }

    private PlayerDetailsDto playerDetailsPartialMap(Player player, Set<String> includedFields) {
        PlayerDetailsDto playerDetailsDto = new PlayerDetailsDto();
        if (includedFields.contains("nickName")) {
            playerDetailsDto.setNickName(player.getNickName());
        }
        return playerDetailsDto;
    }

    private boolean doesPlayerWithNicknameExist(String nickName) {
        return playerRepository.findByNickName(nickName).isPresent();
    }
    private boolean doesPlayerWithEmailExist(String email) {
        return playerRepository.findByEmail(email).isPresent();
    }

}
