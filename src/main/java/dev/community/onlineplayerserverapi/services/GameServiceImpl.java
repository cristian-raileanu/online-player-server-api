package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.entities.Game;
import dev.community.onlineplayerserverapi.entities.GameTeam;
import dev.community.onlineplayerserverapi.repositories.GameRepository;
import dev.community.onlineplayerserverapi.repositories.GameTeamRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
