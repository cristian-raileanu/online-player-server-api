package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.entities.PlayerSession;
import dev.community.onlineplayerserverapi.repositories.SessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:application.properties")
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;

    @Value("${features.session.expirationTimeSeconds:10800}")
    private Integer sessionExpirationTime;


    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public String createPlayerSession(Long playerId) {
        closePlayerOpenedSession(playerId);

        PlayerSession playerSession = new PlayerSession();
        playerSession.setPlayerId(playerId);
        playerSession.setSessionToken(UUID.randomUUID().toString());
        playerSession.setLoginTime(LocalDateTime.now());
        playerSession.setLastActivityTime(LocalDateTime.now());
        playerSession.setIsClosed(false);

        PlayerSession createdPlayerSession = sessionRepository.save(playerSession);

        return createdPlayerSession.getSessionToken();
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void closePlayerSession(String token) {
        closePlayerSessionWithToken(token);
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateActivity(String sessionToken) {
        PlayerSession playerSession = sessionRepository.findOpenedSessionByToken(sessionToken);
        if (playerSession != null && !playerSession.getIsClosed()) {
            playerSession.setLastActivityTime(LocalDateTime.now());
            sessionRepository.save(playerSession);
        }
    }

    @Override
    public PlayerSession getPlayerSession(String sessionToken) {
        return sessionRepository.findOpenedSessionByToken(sessionToken);
    }

    @Override
    public boolean isSessionValid(String sessionToken) {
        PlayerSession playerSession = sessionRepository.findOpenedSessionByToken(sessionToken);
        return playerSession != null && !playerSession.getIsClosed();
    }

    @Override
    public long getTotalPlayTime(Long playerId) {
        List<PlayerSession> playerSessions = sessionRepository.findPlayerSessionsByPlayerId(playerId);
        return playerSessions.stream()
                .map(this::calculateSessionDurationSeconds)
                .reduce(0L, Long::sum);
    }

    private void closePlayerOpenedSession(Long playerId) {
        closeSession(sessionRepository.findOpenedSessionByPlayerId(playerId));
    }

    private void closePlayerSessionWithToken(String token) {
        closeSession(sessionRepository.findOpenedSessionByToken(token));
    }

    private void closeSession(PlayerSession playerSession) {
        if (playerSession != null && !playerSession.getIsClosed()) {
            playerSession.setIsClosed(true);
            if (playerSession.getLastActivityTime().plusSeconds(sessionExpirationTime).isAfter(LocalDateTime.now())) {
                playerSession.setLastActivityTime(LocalDateTime.now());
            }
            sessionRepository.save(playerSession);
        }
    }

    private long calculateSessionDurationSeconds(PlayerSession playerSession) {
        return playerSession.getLastActivityTime().toEpochSecond(ZoneOffset.UTC) -
                playerSession.getLoginTime().toEpochSecond(ZoneOffset.UTC);
    }
}
