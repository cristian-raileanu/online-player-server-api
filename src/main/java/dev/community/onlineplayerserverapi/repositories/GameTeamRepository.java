package dev.community.onlineplayerserverapi.repositories;

import dev.community.onlineplayerserverapi.entities.GameTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameTeamRepository extends JpaRepository<GameTeam, Long> {
}
