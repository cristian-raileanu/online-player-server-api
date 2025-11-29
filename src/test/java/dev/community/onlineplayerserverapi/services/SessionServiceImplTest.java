package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.entities.PlayerSession;
import dev.community.onlineplayerserverapi.repositories.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    public static final String TEST_TOKEN = "test-token";
    public static final String NON_EXISTENT_TOKEN = "non-existent-token";
    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private SessionServiceImpl sessionService;

    private PlayerSession playerSession;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sessionService, "sessionExpirationTime", 3600);

        playerSession = new PlayerSession();
        playerSession.setId(1L);
        playerSession.setPlayerId(100L);
        playerSession.setSessionToken(TEST_TOKEN);
        playerSession.setLoginTime(LocalDateTime.now());
        playerSession.setLastActivityTime(LocalDateTime.now());
        playerSession.setIsClosed(false);
    }

    @Test
    void createPlayerSession_newSession() {
        when(sessionRepository.findOpenedSessionByPlayerId(anyLong())).thenReturn(null);
        when(sessionRepository.save(any(PlayerSession.class))).thenAnswer(invocation -> {
            PlayerSession session = invocation.getArgument(0);
            session.setId(2L); // Simulate saving and getting an ID
            return session;
        });

        String token = sessionService.createPlayerSession(100L);

        assertNotNull(token);
        verify(sessionRepository, times(1)).findOpenedSessionByPlayerId(100L);
        verify(sessionRepository, times(1)).save(any(PlayerSession.class));
    }

    @Test
    void createPlayerSession_existingSessionClosed() {
        when(sessionRepository.findOpenedSessionByPlayerId(anyLong())).thenReturn(playerSession);
        when(sessionRepository.save(any(PlayerSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String token = sessionService.createPlayerSession(100L);

        assertNotNull(token);
        assertTrue(playerSession.getIsClosed());
        verify(sessionRepository, times(1)).findOpenedSessionByPlayerId(100L);
        verify(sessionRepository, times(2)).save(any(PlayerSession.class)); // Once for closing, once for creating
    }

    @Test
    void closePlayerSession_sessionFound() {
        when(sessionRepository.findOpenedSessionByToken(TEST_TOKEN)).thenReturn(playerSession);

        sessionService.closePlayerSession(TEST_TOKEN);

        assertTrue(playerSession.getIsClosed());
        verify(sessionRepository, times(1)).save(playerSession);
    }

    @Test
    void closePlayerSession_sessionNotFound() {
        when(sessionRepository.findOpenedSessionByToken(NON_EXISTENT_TOKEN)).thenReturn(null);

        sessionService.closePlayerSession(NON_EXISTENT_TOKEN);

        verify(sessionRepository, never()).save(any(PlayerSession.class));
    }

    @Test
    void getPlayerSession_sessionFound() {
        when(sessionRepository.findOpenedSessionByToken(TEST_TOKEN)).thenReturn(playerSession);

        PlayerSession foundSession = sessionService.getPlayerSession(TEST_TOKEN);

        assertNotNull(foundSession);
        assertEquals(TEST_TOKEN, foundSession.getSessionToken());
    }

    @Test
    void getPlayerSession_sessionNotFound() {
        when(sessionRepository.findOpenedSessionByToken(NON_EXISTENT_TOKEN)).thenReturn(null);

        PlayerSession foundSession = sessionService.getPlayerSession(NON_EXISTENT_TOKEN);

        assertNull(foundSession);
    }

    @Test
    void isSessionValid_validSession() {
        when(sessionRepository.findOpenedSessionByToken(TEST_TOKEN)).thenReturn(playerSession);

        assertTrue(sessionService.isSessionValid(TEST_TOKEN));
    }

    @Test
    void isSessionValid_invalidSession() {
        when(sessionRepository.findOpenedSessionByToken(NON_EXISTENT_TOKEN)).thenReturn(null);

        assertFalse(sessionService.isSessionValid(NON_EXISTENT_TOKEN));
    }
    
    @Test
    void isSessionValid_closedSession() {
        playerSession.setIsClosed(true);
        when(sessionRepository.findOpenedSessionByToken(TEST_TOKEN)).thenReturn(null);

        assertFalse(sessionService.isSessionValid(TEST_TOKEN));
    }
}
