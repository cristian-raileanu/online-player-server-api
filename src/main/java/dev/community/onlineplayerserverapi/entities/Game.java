package dev.community.onlineplayerserverapi.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode
@Table(name = "T_GAME")
@NoArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name="host_player_id")
    private Long hostPlayerId;

    @Column(name="start_time")
    private LocalDateTime startTime;

    @Column(name="end_time")
    private LocalDateTime endTime;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "game")
    private List<GameTeam> gameTeams;
}
