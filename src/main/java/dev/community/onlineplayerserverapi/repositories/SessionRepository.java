package dev.community.onlineplayerserverapi.repositories;

import dev.community.onlineplayerserverapi.entities.PlayerSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<PlayerSession, Long> {

    @Query("select ps from PlayerSession ps where ps.playerId = ?1 and ps.isClosed != TRUE")
    PlayerSession findOpenedSessionByPlayerId(Long playerId);

    @Query("select ps from PlayerSession ps where ps.sessionToken = ?1 and ps.isClosed != TRUE")
    PlayerSession findOpenedSessionByToken(String token);

    List<PlayerSession> findPlayerSessionsByPlayerId(Long playerId);
}
