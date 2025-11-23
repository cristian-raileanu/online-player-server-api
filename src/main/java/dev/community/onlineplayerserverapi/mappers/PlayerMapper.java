package dev.community.onlineplayerserverapi.mappers;

import dev.community.onlineplayerserverapi.entities.Player;
import dev.community.onlineplayerserverapi.models.PlayerDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    Player toEntity(PlayerDto playerDto);
}
