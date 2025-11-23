package dev.community.onlineplayerserverapi.models;

import lombok.Data;

@Data
public class PlayerDto {

    private String nickName;

    private String email;

    private String passwordHash;
}
