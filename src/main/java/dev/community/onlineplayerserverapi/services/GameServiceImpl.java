package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.entities.Game;
import dev.community.onlineplayerserverapi.entities.GameTeam;
import dev.community.onlineplayerserverapi.models.*;
import dev.community.onlineplayerserverapi.repositories.GameRepository;
import dev.community.onlineplayerserverapi.repositories.GameTeamRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final GameTeamRepository gameTeamRepository;

    @Override
    @Transactional
    public Game createGame(String name, Long hostPlayerId) {
        Optional<Game> foundGame = gameRepository.findFirstByNameOrderByStartTimeDesc(name)
                .filter(game -> game.getEndTime() == null);
        if (foundGame.isPresent()) {
            throw new IllegalStateException("A game with this name already exists.");
        }

        Game newGame = new Game();
        newGame.setName(name);
        newGame.setHostPlayerId(hostPlayerId);
        newGame.setStartTime(LocalDateTime.now());
        newGame.setGameTeams(new ArrayList<>());

        Game savedGame = gameRepository.save(newGame);

        GameTeam hostTeam = createTeamForPlayer(savedGame, hostPlayerId);
        savedGame.getGameTeams().add(hostTeam);

        return gameRepository.save(savedGame);
    }

    @Override
    @Transactional
    public Game joinGame(String name, Long playerId) {
        Game game = gameRepository.findFirstByNameOrderByStartTimeDesc(name)
                .filter(foundGame -> foundGame.getEndTime() == null)
                .orElseThrow(() -> new IllegalStateException("Game not found."));

        boolean playerAlreadyInGame = game.getGameTeams().stream()
                .anyMatch(team -> team.getPlayersIds().contains(playerId));

        if (playerAlreadyInGame) {
            throw new IllegalStateException("Player is already in this game.");
        }

        GameTeam newTeam = createTeamForPlayer(game, playerId);
        game.getGameTeams().add(newTeam);

        return gameRepository.save(game);
    }

    @Override
    @Transactional
    public void leaveGame(String name, Long playerId) {
        Game game = gameRepository.findFirstByNameOrderByStartTimeDesc(name)
                .filter(foundGame -> foundGame.getEndTime() == null)
                .orElseThrow(() -> new IllegalStateException("Game not found."));

        Optional<GameTeam> activeTeamOpt = game.getGameTeams().stream()
                .filter(team -> team.getRemainingPlayers().contains(playerId))
                .findFirst();

        if (activeTeamOpt.isPresent()) {
            GameTeam activeTeam = activeTeamOpt.get();
            long teamsWithPlayersCount = game.getGameTeams().stream()
                    .filter(team -> !team.getRemainingPlayers().isEmpty())
                    .count();

            if (teamsWithPlayersCount == 1) {
                game.setEndTime(LocalDateTime.now());
                gameRepository.save(game);
            } else {
                activeTeam.getRemainingPlayers().remove(playerId);
                gameTeamRepository.save(activeTeam);
            }
        }
    }

    @Override
    public GameDetailsResponseDto getGameDetails(PlayerDetailsRequestDto playerDetailsRequestDto) {
        List<Game> games = gameRepository.findAll();
        Set<String> includedFields = playerDetailsRequestDto.getIncludes() != null ?
                playerDetailsRequestDto.getIncludes() : Set.of();

        List<GameDetailsDto> gameDetailsDtos = games.stream()
                .map(game -> mapGameToDetailsDto(game, includedFields))
                .collect(Collectors.toList());

        GameDetailsResponseDto responseDto = new GameDetailsResponseDto();
        responseDto.setGameDetails(gameDetailsDtos);
        responseDto.setTotalNumber(gameDetailsDtos.size());

        return responseDto;
    }

    private GameDetailsDto mapGameToDetailsDto(Game game, Set<String> includedFields) {
        GameDetailsDto dto = new GameDetailsDto();
        if (includedFields.contains("gameName")) {
            dto.setGameName(game.getName());
        }
        if (includedFields.contains("duration") && game.getStartTime() != null && game.getEndTime() != null) {
            dto.setDuration(Duration.between(game.getStartTime(), game.getEndTime()).getSeconds());
        }
        if (includedFields.contains("startTime")) {
            dto.setStartTime(game.getStartTime());
        }
        if (includedFields.contains("endTime")) {
            dto.setEndTime(game.getEndTime());
        }
        if (includedFields.contains("teamsDetails")) {
            dto.setTeamsDetails(game.getGameTeams().stream()
                    .map(this::mapGameTeamToDetailsDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private GameTeamDetailsDto mapGameTeamToDetailsDto(GameTeam gameTeam) {
        GameTeamDetailsDto dto = new GameTeamDetailsDto();
        dto.setPlayers(gameTeam.getPlayersIds().stream().map(String::valueOf).collect(Collectors.toList()));

        if (gameTeam.getGame().getEndTime() != null) {
            dto.setExitTime(gameTeam.getGame().getEndTime());
            if (gameTeam.getRemainingPlayers().isEmpty()) {
                dto.setResult(GameResult.DEFEAT);
            } else {
                dto.setResult(GameResult.VICTORY);
            }
        }
        return dto;
    }

    private GameTeam createTeamForPlayer(Game game, Long playerId) {
        GameTeam newTeam = new GameTeam();
        newTeam.setGame(game);

        Set<Long> playerSet = new HashSet<>();
        playerSet.add(playerId);

        newTeam.setPlayersIds(playerSet);
        newTeam.setRemainingPlayers(new HashSet<>(playerSet));

        return gameTeamRepository.save(newTeam);
    }
}
