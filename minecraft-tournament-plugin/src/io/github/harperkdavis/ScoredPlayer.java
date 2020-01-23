package io.github.harperkdavis;

import org.bukkit.entity.Player;

public class ScoredPlayer {

    public Player player;
    public ScoredTeam team;
    public int score;
    public Boolean admin;

    ScoredPlayer (Player player, Boolean admin) {
        this.player = player;
        this.admin = admin;

    }

}
