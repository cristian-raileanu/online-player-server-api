package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.entities.Player;
import dev.community.onlineplayerserverapi.entities.PlayerSession;
import dev.community.onlineplayerserverapi.mappers.PlayerMapper;
import dev.community.onlineplayerserverapi.models.*;
import dev.community.onlineplayerserverapi.repositories.PlayerRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final SessionService sessionService;
    private final GameService gameService;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
    );

    private static final int MAX_SIZE_NICKNAME = 20;
    private static final int MAX_SIZE_EMAIL = 20;

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

        String sessionToken = sessionService.createPlayerSession(foundPlayer.get().getId());

        return LoginResponseDto.builder()
                .loginStatus(LoginStatus.SUCCESS)
                .token(sessionToken)
                .build();
    }

    @Override
    public LoginResponseDto logout(String token) {
        sessionService.closePlayerSession(token);
        return  LoginResponseDto.builder()
                .loginStatus(LoginStatus.SUCCESS)
                .build();
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public RegisterResponseDto register(PlayerDto playerDto) {
        Optional<RegisterResponseDto> validationResponse = validateRegistration(playerDto);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }

        Player createdPlayer = playerMapper.toEntity(playerDto);
        playerRepository.save(createdPlayer);

        return RegisterResponseDto.builder()
                .loginStatus(LoginStatus.SUCCESS)
                .build();
    }

    @Override
    public LoginResponseDto createGame(GameRequestDto gameRequestDto) {
        PlayerSession session = sessionService.getPlayerSession(gameRequestDto.getPlayerToken());
        if (session == null) {
            return LoginResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Invalid session token.")
                    .build();
        }

        try {
            gameService.createGame(gameRequestDto.getGameName(), session.getPlayerId());
            return LoginResponseDto.builder()
                    .loginStatus(LoginStatus.SUCCESS)
                    .build();
        } catch (IllegalStateException e) {
            return LoginResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public LoginResponseDto joinGame(GameRequestDto gameRequestDto) {
        PlayerSession session = sessionService.getPlayerSession(gameRequestDto.getPlayerToken());
        if (session == null) {
            return LoginResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Invalid session token.")
                    .build();
        }

        try {
            gameService.joinGame(gameRequestDto.getGameName(), session.getPlayerId());
            return LoginResponseDto.builder()
                    .loginStatus(LoginStatus.SUCCESS)
                    .build();
        } catch (IllegalStateException e) {
            return LoginResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public LoginResponseDto leaveGame(GameRequestDto gameRequestDto) {
        PlayerSession session = sessionService.getPlayerSession(gameRequestDto.getPlayerToken());
        if (session == null) {
            return LoginResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Invalid session token.")
                    .build();
        }

        try {
            gameService.leaveGame(gameRequestDto.getGameName(), session.getPlayerId());
            return LoginResponseDto.builder()
                    .loginStatus(LoginStatus.SUCCESS)
                    .build();
        } catch (IllegalStateException e) {
            return LoginResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public GameDetailsResponseDto getGameDetails(PlayerDetailsRequestDto playerDetailsRequestDto) {
        GameDetailsResponseDto gameDetails = gameService.getGameDetails(playerDetailsRequestDto);

        Set<String> includedFields = playerDetailsRequestDto.getIncludes() != null ?
                playerDetailsRequestDto.getIncludes() : Set.of();
        if (includedFields.contains("teamsDetails")) {
            gameDetails.getGameDetails().forEach(game -> {
                if (game.getTeamsDetails() != null) {
                    game.getTeamsDetails().forEach(team -> {
                        List<Long> playerIds = team.getPlayers().stream().map(Long::valueOf).collect(Collectors.toList());
                        List<Player> players = playerRepository.findAllById(playerIds);
                        team.setPlayers(players.stream().map(Player::getNickName).collect(Collectors.toList()));
                    });
                }
            });
        }

        return gameDetails;
    }

    private Optional<RegisterResponseDto> validateRegistration(PlayerDto playerDto) {
        if (playerDto.getNickName().length() > MAX_SIZE_NICKNAME) {
            return Optional.of(RegisterResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Nickname exceeds maximum length of " + MAX_SIZE_NICKNAME)
                    .build());
        }
        if (playerDto.getEmail().length() > MAX_SIZE_EMAIL) {
            return Optional.of(RegisterResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Email exceeds maximum length of " + MAX_SIZE_EMAIL)
                    .build());
        }
        if (playerDto.getPasswordHash().length() > MAX_SIZE_NICKNAME) {
            return Optional.of(RegisterResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Password exceeds maximum length of " + MAX_SIZE_NICKNAME)
                    .build());
        }
        if (doesPlayerWithNicknameExist(playerDto.getNickName())) {
            return Optional.of(RegisterResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Nickname already exists!")
                    .build());
        }
        if (doesPlayerWithEmailExist(playerDto.getEmail())) {
            return Optional.of(RegisterResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Email already used!")
                    .build());
        }
        if (!isEmailValid(playerDto.getEmail())) {
            return Optional.of(RegisterResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Invalid email format!")
                    .build());
        }
        if (playerDto.getPasswordHash().isEmpty()) {
            return Optional.of(RegisterResponseDto.builder()
                    .loginStatus(LoginStatus.REJECTED)
                    .message("Password empty!")
                    .build());
        }
        return Optional.empty();
    }

    @Override
    public boolean isPlayerExisting(String nickName) {
        return doesPlayerWithNicknameExist(nickName);
    }

    @Override
    public PlayerDetailsResponseDto getPlayerDetailsPage(PlayerDetailsRequestDto playerDetailsRequestDto) {
        if (playerDetailsRequestDto.getPlayerToken() != null) {
            sessionService.updateActivity(playerDetailsRequestDto.getPlayerToken());
        }
        boolean showAll = playerDetailsRequestDto.getPage() == null || playerDetailsRequestDto.getPageSize() == null;

        List<Player> selectedPlayers = showAll ? playerRepository.findAll() : List.of();
        Set<String> includedFields = playerDetailsRequestDto.getIncludes() != null ?
                playerDetailsRequestDto.getIncludes() : Set.of();

        PlayerDetailsResponseDto playerDetailsResponseDto = new PlayerDetailsResponseDto();
        playerDetailsResponseDto.setPlayerDetails(selectedPlayers.stream()
                .filter(player -> playerDetailsPartialFilter(player, playerDetailsRequestDto.getFilter()))
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
        if (includedFields.contains("totalPlayTime")) {
            playerDetailsDto.setTotalPlayTime(sessionService.getTotalPlayTime(player.getId()));
        }
        return playerDetailsDto;
    }

    private boolean playerDetailsPartialFilter(Player player, PlayerFilterDto playerFilterDto) {
        if (playerFilterDto != null && !CollectionUtils.isEmpty(playerFilterDto.getNickNames())) {
            return playerFilterDto.getNickNames().contains(player.getNickName());
        }
        return true;
    }

    private boolean doesPlayerWithNicknameExist(String nickName) {
        return playerRepository.findByNickName(nickName).isPresent();
    }
    private boolean doesPlayerWithEmailExist(String email) {
        return playerRepository.findByEmail(email).isPresent();
    }

    private boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

}
