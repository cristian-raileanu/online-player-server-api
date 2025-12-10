package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.entities.Game;
import dev.community.onlineplayerserverapi.entities.GameTeam;
import dev.community.onlineplayerserverapi.repositories.GameRepository;
import dev.community.onlineplayerserverapi.repositories.GameTeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameTeamRepository gameTeamRepository;

    @InjectMocks
    private GameServiceImpl gameService;

    private Game game;
    private GameTeam team1;
    private GameTeam team2;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.setId(1L);
        game.setName("Test Game");
        game.setHostPlayerId(100L);
        game.setStartTime(LocalDateTime.now());
        game.setGameTeams(new ArrayList<>());

        team1 = new GameTeam();
        team1.setId(1L);
        team1.setGame(game);
        team1.setPlayersIds(new HashSet<>(Set.of(100L)));
        team1.setRemainingPlayers(new HashSet<>(Set.of(100L)));

        team2 = new GameTeam();
        team2.setId(2L);
        team2.setGame(game);
        team2.setPlayersIds(new HashSet<>(Set.of(200L)));
        team2.setRemainingPlayers(new HashSet<>(Set.of(200L)));
    }

    @Test
    void createGame_success() {
        when(gameRepository.findFirstByNameOrderByStartTimeDesc(anyString())).thenReturn(Optional.empty());
        when(gameRepository.save(any(Game.class))).thenReturn(game);
        when(gameTeamRepository.save(any(GameTeam.class))).thenReturn(team1);

        Game createdGame = gameService.createGame("Test Game", 100L);

        assertNotNull(createdGame);
        assertEquals("Test Game", createdGame.getName());
        assertEquals(1, createdGame.getGameTeams().size());
        verify(gameRepository, times(2)).save(any(Game.class));
    }

    @Test
    void createGame_alreadyExists() {
        when(gameRepository.findFirstByNameOrderByStartTimeDesc(anyString())).thenReturn(Optional.of(game));

        assertThrows(IllegalStateException.class, () -> gameService.createGame("Test Game", 100L));
    }

    @Test
    void joinGame_success() {
        when(gameRepository.findFirstByNameOrderByStartTimeDesc(anyString())).thenReturn(Optional.of(game));
        when(gameTeamRepository.save(any(GameTeam.class))).thenReturn(team2);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        Game joinedGame = gameService.joinGame("Test Game", 200L);

        assertNotNull(joinedGame);
        assertEquals(1, joinedGame.getGameTeams().size());
    }

    @Test
    void joinGame_notFound() {
        when(gameRepository.findFirstByNameOrderByStartTimeDesc(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> gameService.joinGame("Test Game", 200L));
    }

    @Test
    void joinGame_playerAlreadyInGame() {
        game.getGameTeams().add(team1);
        when(gameRepository.findFirstByNameOrderByStartTimeDesc(anyString())).thenReturn(Optional.of(game));

        assertThrows(IllegalStateException.class, () -> gameService.joinGame("Test Game", 100L));
    }

    @Test
    void leaveGame_success() {
        game.getGameTeams().add(team1);
        game.getGameTeams().add(team2);
        when(gameRepository.findFirstByNameOrderByStartTimeDesc(anyString())).thenReturn(Optional.of(game));

        gameService.leaveGame("Test Game", 100L);

        assertFalse(team1.getRemainingPlayers().contains(100L));
        verify(gameTeamRepository, times(1)).save(team1);
        assertNull(game.getEndTime());
    }

    @Test
    void leaveGame_lastTeamEndsGame() {
        game.getGameTeams().add(team1);
        team2.getRemainingPlayers().clear();
        game.getGameTeams().add(team2);
        when(gameRepository.findFirstByNameOrderByStartTimeDesc(anyString())).thenReturn(Optional.of(game));

        gameService.leaveGame("Test Game", 100L);

        assertNotNull(game.getEndTime());
        verify(gameRepository, times(1)).save(game);
        verify(gameTeamRepository, never()).save(any(GameTeam.class));
    }

    @Test
    void leaveGame_notFound() {
        when(gameRepository.findFirstByNameOrderByStartTimeDesc(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> gameService.leaveGame("Test Game", 100L));
    }
}
