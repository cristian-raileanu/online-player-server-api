package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.entities.Player;
import dev.community.onlineplayerserverapi.entities.PlayerSession;
import dev.community.onlineplayerserverapi.mappers.PlayerMapper;
import dev.community.onlineplayerserverapi.models.*;
import dev.community.onlineplayerserverapi.repositories.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceImplTest {

    public static final String TEST_TOKEN = "test_token";
    public static final String TEST_GAME = "Test Game";

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerMapper playerMapper;

    @Mock
    private SessionService sessionService;

    @Mock
    private GameService gameService;

    @InjectMocks
    private PlayerServiceImpl playerService;

    private PlayerDto playerDto;
    private Player player;
    private PlayerSession playerSession;
    private GameRequestDto gameRequestDto;

    @BeforeEach
    void setUp() {
        playerDto = new PlayerDto();
        playerDto.setNickName("testUser");
        playerDto.setEmail("test@example.com");
        playerDto.setPasswordHash("password");

        player = new Player();
        player.setId(1L);
        player.setNickName("testUser");
        player.setEmail("test@example.com");
        player.setPasswordHash("password");

        playerSession = new PlayerSession();
        playerSession.setPlayerId(1L);
        playerSession.setSessionToken(TEST_TOKEN);

        gameRequestDto = new GameRequestDto(TEST_GAME, TEST_TOKEN);
    }

    @Test
    void register_success() {
        when(playerRepository.findByNickName(playerDto.getNickName())).thenReturn(Optional.empty());
        when(playerRepository.findByEmail(playerDto.getEmail())).thenReturn(Optional.empty());
        when(playerMapper.toEntity(playerDto)).thenReturn(player);
        when(playerRepository.save(any(Player.class))).thenReturn(player);

        RegisterResponseDto response = playerService.register(playerDto);

        assertEquals(LoginStatus.SUCCESS, response.getLoginStatus());
    }

    @Test
    void register_nicknameExists() {
        when(playerRepository.findByNickName(playerDto.getNickName())).thenReturn(Optional.of(player));

        RegisterResponseDto response = playerService.register(playerDto);

        assertEquals(LoginStatus.REJECTED, response.getLoginStatus());
        assertEquals("Nickname already exists!", response.getMessage());
    }

    @Test
    void register_emailExists() {
        when(playerRepository.findByNickName(playerDto.getNickName())).thenReturn(Optional.empty());
        when(playerRepository.findByEmail(playerDto.getEmail())).thenReturn(Optional.of(player));

        RegisterResponseDto response = playerService.register(playerDto);

        assertEquals(LoginStatus.REJECTED, response.getLoginStatus());
        assertEquals("Email already used!", response.getMessage());
    }

    @Test
    void register_invalidEmail() {
        playerDto.setEmail("invalid-email");

        RegisterResponseDto response = playerService.register(playerDto);

        assertEquals(LoginStatus.REJECTED, response.getLoginStatus());
        assertEquals("Invalid email format!", response.getMessage());
    }

    @Test
    void login_success() {
        when(playerRepository.findByNickName(playerDto.getNickName())).thenReturn(Optional.of(player));
        when(sessionService.createPlayerSession(player.getId())).thenReturn(TEST_TOKEN);

        LoginResponseDto response = playerService.login(playerDto);

        assertEquals(LoginStatus.SUCCESS, response.getLoginStatus());
        assertEquals(TEST_TOKEN, response.getToken());
    }

    @Test
    void login_playerNotRegistered() {
        when(playerRepository.findByNickName(playerDto.getNickName())).thenReturn(Optional.empty());

        LoginResponseDto response = playerService.login(playerDto);

        assertEquals(LoginStatus.REJECTED, response.getLoginStatus());
        assertEquals("Player not registered!", response.getMessage());
    }

    @Test
    void login_wrongPassword() {
        player.setPasswordHash("wrong_password");
        when(playerRepository.findByNickName(playerDto.getNickName())).thenReturn(Optional.of(player));

        LoginResponseDto response = playerService.login(playerDto);

        assertEquals(LoginStatus.REJECTED, response.getLoginStatus());
        assertEquals("Wrong password!", response.getMessage());
    }

    @Test
    void logout_success() {
        LoginResponseDto response = playerService.logout(TEST_TOKEN);

        assertEquals(LoginStatus.SUCCESS, response.getLoginStatus());
    }

    @Test
    void createGame_success() {
        when(sessionService.getPlayerSession(TEST_TOKEN)).thenReturn(playerSession);

        LoginResponseDto response = playerService.createGame(gameRequestDto);

        assertEquals(LoginStatus.SUCCESS, response.getLoginStatus());
    }

    @Test
    void createGame_invalidSession() {
        when(sessionService.getPlayerSession(TEST_TOKEN)).thenReturn(null);

        LoginResponseDto response = playerService.createGame(gameRequestDto);

        assertEquals(LoginStatus.REJECTED, response.getLoginStatus());
        assertEquals("Invalid session token.", response.getMessage());
    }

    @Test
    void createGame_gameAlreadyExists() {
        when(sessionService.getPlayerSession(TEST_TOKEN)).thenReturn(playerSession);
        doThrow(new IllegalStateException("Game already exists.")).when(gameService).createGame(TEST_GAME, 1L);

        LoginResponseDto response = playerService.createGame(gameRequestDto);

        assertEquals(LoginStatus.REJECTED, response.getLoginStatus());
        assertEquals("Game already exists.", response.getMessage());
    }

    @Test
    void joinGame_success() {
        when(sessionService.getPlayerSession(TEST_TOKEN)).thenReturn(playerSession);

        LoginResponseDto response = playerService.joinGame(gameRequestDto);

        assertEquals(LoginStatus.SUCCESS, response.getLoginStatus());
    }

    @Test
    void joinGame_invalidSession() {
        when(sessionService.getPlayerSession(TEST_TOKEN)).thenReturn(null);

        LoginResponseDto response = playerService.joinGame(gameRequestDto);

        assertEquals(LoginStatus.REJECTED, response.getLoginStatus());
        assertEquals("Invalid session token.", response.getMessage());
    }

    @Test
    void joinGame_playerAlreadyInGame() {
        when(sessionService.getPlayerSession(TEST_TOKEN)).thenReturn(playerSession);
        doThrow(new IllegalStateException("Player already in game.")).when(gameService).joinGame(TEST_GAME, 1L);

        LoginResponseDto response = playerService.joinGame(gameRequestDto);

        assertEquals(LoginStatus.REJECTED, response.getLoginStatus());
        assertEquals("Player already in game.", response.getMessage());
    }

    @Test
    void leaveGame_success() {
        when(sessionService.getPlayerSession(TEST_TOKEN)).thenReturn(playerSession);

        LoginResponseDto response = playerService.leaveGame(gameRequestDto);

        assertEquals(LoginStatus.SUCCESS, response.getLoginStatus());
    }

    @Test
    void leaveGame_invalidSession() {
        when(sessionService.getPlayerSession(TEST_TOKEN)).thenReturn(null);

        LoginResponseDto response = playerService.leaveGame(gameRequestDto);

        assertEquals(LoginStatus.REJECTED, response.getLoginStatus());
        assertEquals("Invalid session token.", response.getMessage());
    }

    @Test
    void leaveGame_gameNotFound() {
        when(sessionService.getPlayerSession(TEST_TOKEN)).thenReturn(playerSession);
        doThrow(new IllegalStateException("Game not found.")).when(gameService).leaveGame(TEST_GAME, 1L);

        LoginResponseDto response = playerService.leaveGame(gameRequestDto);

        assertEquals(LoginStatus.REJECTED, response.getLoginStatus());
        assertEquals("Game not found.", response.getMessage());
    }
}
