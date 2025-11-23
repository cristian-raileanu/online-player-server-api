package dev.community.onlineplayerserverapi.repositories;

import dev.community.onlineplayerserverapi.entities.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByNickName(String nickName);

    Optional<Object> findByEmail(String email);
}
