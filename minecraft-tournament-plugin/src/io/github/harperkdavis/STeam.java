package io.github.harperkdavis;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class STeam {

    public Team team;
    public String teamName;
    public ChatColor col;
    public SPlayer player1;
    public SPlayer player2;
    public int score = 0;

    STeam(String teamName, ChatColor col, SPlayer c) {
        this.teamName = teamName;
        this.col = col;
        player1 = c;
        team.setPrefix(col+teamName+ChatColor.WHITE+" ");
        player1.team = this;
    }

    public void sendInfo(Player player) {
        player.sendMessage(ChatColor.WHITE + "==========" + ChatColor.BOLD + (col + teamName) + ChatColor.WHITE + "==========");
        player.sendMessage(ChatColor.BLUE + "Team: " + ChatColor.WHITE + player1 + " " + player2);
        player.sendMessage(ChatColor.BLUE + "Total Score" + ChatColor.WHITE + score);
    }

}