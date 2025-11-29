package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.entities.Player;
import dev.community.onlineplayerserverapi.mappers.PlayerMapper;
import dev.community.onlineplayerserverapi.models.LoginResponseDto;
import dev.community.onlineplayerserverapi.models.LoginStatus;
import dev.community.onlineplayerserverapi.models.PlayerDto;
import dev.community.onlineplayerserverapi.models.RegisterResponseDto;
import dev.community.onlineplayerserverapi.repositories.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceImplTest {

    public static final String TEST_TOKEN = "test_token";
    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerMapper playerMapper;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private PlayerServiceImpl playerService;

    private PlayerDto playerDto;
    private Player player;

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
    }

    @Test
    void register_success() {
        when(playerRepository.findByNickName(playerDto.getNickName())).thenReturn(Optional.empty());
        when(playerRepository.findByEmail(playerDto.getEmail())).thenReturn(Optional.empty());
        when(playerMapper.toEntity(playerDto)).thenReturn(player);
        when(playerRepository.save(any(Player.class))).thenReturn(player);
        when(sessionService.createPlayerSession(player.getId())).thenReturn(TEST_TOKEN);

        RegisterResponseDto response = playerService.register(playerDto);

        assertEquals(LoginStatus.SUCCESS, response.getLoginStatus());
        assertEquals(TEST_TOKEN, response.getToken());
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
}
