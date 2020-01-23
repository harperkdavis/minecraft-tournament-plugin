package io.github.harperkdavis;

import org.bukkit.entity.Player;

public class SPlayer {

    public Player player;
    public STeam team;
    public int score;
    public Boolean admin;

    SPlayer (Player player, Boolean admin) {
        this.player = player;
        this.admin = admin;

    }

}
