package io.github.harperkdavis;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

class ScoredTeam {

    String teamName;
    ChatColor col;
    ScoredPlayer player1;
    ScoredPlayer player2;
    int score = 0;

    ScoredTeam(String teamName, ChatColor col, ScoredPlayer c) {
        this.teamName = teamName;
        this.col = col;
        setPlayer1(c);
        // team.setPrefix(col+teamName+ChatColor.WHITE+" ");
    }

    void sendInfo(Player player) {
        player.sendMessage(ChatColor.WHITE + "========== " + ChatColor.BOLD + (col + teamName.toUpperCase()) + ChatColor.WHITE + " ==========");

        String player1s = player1.player.getName() + ChatColor.GRAY + "(" + player1.score + ")";
        String player2s;
        if (player2 != null) {
            player2s = player2.player.getName() + ChatColor.GRAY + "(" + player2.score + ")";
        } else {
            player2s = "nobody else";
        }

        player.sendMessage(ChatColor.BLUE + "Team: " + ChatColor.WHITE + player1s + " and " + player2s);
        player.sendMessage(ChatColor.BLUE + "Total Score: " + ChatColor.WHITE + score);
    }

    int getScore() {
        score = player1.score;
        if(player2 != null) {
            score += player2.score;
        }
        return score;
    }

    private void setPlayer1(ScoredPlayer p) {
        this.player1 = p;
        p.team = this;
        p.player.setDisplayName(ChatColor.BOLD + (col + teamName.toUpperCase()) + ChatColor.WHITE + " " + p.player.getName());
    }

    void setPlayer2(ScoredPlayer p) {
        this.player2 = p;
        p.team = this;
        p.player.setDisplayName(ChatColor.BOLD + (col + teamName.toUpperCase()) + ChatColor.WHITE + " " + p.player.getName());
    }

}
