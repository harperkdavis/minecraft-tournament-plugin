package io.github.harperkdavis;

import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EnumTitleAction;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scoreboard.Team;

public class ScoredTeam {

    public Team team;
    public String teamName;
    public ChatColor col;
    public ScoredPlayer player1;
    public ScoredPlayer player2;
    public int score = 0;

    ScoredTeam(String teamName, ChatColor col, ScoredPlayer c) {
        this.teamName = teamName;
        this.col = col;
        setPlayer1(c);
        // team.setPrefix(col+teamName+ChatColor.WHITE+" ");
    }

    public void sendInfo(Player player) {
        player.sendMessage(ChatColor.WHITE + "========== " + ChatColor.BOLD + (col + teamName.toUpperCase()) + ChatColor.WHITE + " ==========");

        String player1s = player1.player.getName() + ChatColor.GRAY + "(" + player1.score + ")";
        String player2s = "";
        if (player2 != null) {
            player2s = player2.player.getName() + ChatColor.GRAY + "(" + player2.score + ")";
        } else {
            player2s = "nobody else";
        }

        player.sendMessage(ChatColor.BLUE + "Team: " + ChatColor.WHITE + player1s + " and " + player2s);
        player.sendMessage(ChatColor.BLUE + "Total Score: " + ChatColor.WHITE + score);
    }

    public int getScore() {
        score = player1.score;
        if(player2 != null) {
            score += player2.score;
        }
        return score;
    }

    public void setPlayer1(ScoredPlayer p) {
        this.player1 = p;
        p.team = this;
        p.player.setDisplayName(ChatColor.BOLD + (col + teamName.toUpperCase()) + ChatColor.WHITE + " " + p.player.getName());
    }

    public void setPlayer2(ScoredPlayer p) {
        this.player2 = p;
        p.team = this;
        p.player.setDisplayName(ChatColor.BOLD + (col + teamName.toUpperCase()) + ChatColor.WHITE + " " + p.player.getName());
    }

}
