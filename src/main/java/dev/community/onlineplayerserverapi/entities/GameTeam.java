package dev.community.onlineplayerserverapi.entities;

import dev.community.onlineplayerserverapi.converters.LongSetToStringConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Entity
@EqualsAndHashCode
@Table(name = "T_GAME_TEAM")
@NoArgsConstructor
public class GameTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id", nullable=false)
    private Game game;

    @Column(name = "player_ids")
    @Convert(converter = LongSetToStringConverter.class)
    private Set<Long> playersIds;

    @Column(name = "remaining_players")
    @Convert(converter = LongSetToStringConverter.class)
    private Set<Long> remainingPlayers;
}
