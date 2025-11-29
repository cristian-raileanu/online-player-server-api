package dev.community.onlineplayerserverapi.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@EqualsAndHashCode
@Table(name = "T_PLAYER_SESSION")
@NoArgsConstructor
public class PlayerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "player_id")
    private Long playerId;

    @Column(name = "session_token")
    private String sessionToken;

    @Column(name = "login_time")
    private LocalDateTime loginTime;

    @Column(name = "last_activity_time")
    private LocalDateTime lastActivityTime;

    @Column(name = "is_closed")
    private Boolean isClosed;
}
