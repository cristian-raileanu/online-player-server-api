package dev.community.onlineplayerserverapi.services;

import dev.community.onlineplayerserverapi.entities.PlayerSession;

public interface SessionService {
    String createPlayerSession(Long playerId);
    void closePlayerSession(String token);
    void updateActivity(String sessionToken);
    PlayerSession getPlayerSession(String sessionToken);
    boolean isSessionValid(String sessionToken);
    long getTotalPlayTime(Long playerId);
}
