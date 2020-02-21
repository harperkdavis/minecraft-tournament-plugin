package io.github.harperkdavis;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class LobbyScoreboard extends BukkitRunnable {

    private final MCTMain main;
    private int ticks = 0;

    LobbyScoreboard(MCTMain main) {
        this.main = main;
        main.tasksRunning.add(this);

        for(ScoredPlayer p : main.playerList) {
            if(p.player.getWorld() == Bukkit.getServer().getWorld("world")) {
                setScoreBoard(p.player);
            }
        }

    }

    private void updateTopList() {
        World world = Bukkit.getServer().getWorld("world");
        for (Entity e : world.getEntities()) {
            if (e instanceof ArmorStand) {
                e.remove();
            }
        }

        Location teamldb = new Location(world, -10.5, 66.5, 51.5);

        Vector h = new Vector(0, 0, 0);

        List<ScoredTeam> sortedTeamList = new ArrayList<>();
        int[] teamScores = new int[main.teamList.size()];

        for (int i = 0; i < teamScores.length; i++) {
            teamScores[i] = main.teamList.get(i).getScore();
        }

        Arrays.sort(teamScores);

        for (int score : teamScores) {
            for (ScoredTeam st : main.teamList) {
                if (st.score == score && !sortedTeamList.contains(st)) {
                    sortedTeamList.add(st);
                }
            }
        }

        for (ScoredTeam st : sortedTeamList) {
            ArmorStand as = (ArmorStand) world.spawnEntity(teamldb.add(h), EntityType.ARMOR_STAND);
            as.setVisible(false);
            as.setCustomNameVisible(true);
            as.setCustomName(ChatColor.BOLD + (st.col + st.teamName + ChatColor.YELLOW + ": " + ChatColor.WHITE + st.getScore()));
            as.setGravity(false);
            h.add(new Vector(0, 0.4, 0));
        }

        ArmorStand topTeamLDB = (ArmorStand) world.spawnEntity(teamldb.add(h), EntityType.ARMOR_STAND);
        topTeamLDB.setVisible(false);
        topTeamLDB.setCustomNameVisible(true);
        topTeamLDB.setCustomName(ChatColor.BOLD + (ChatColor.GREEN + " Team List"));
        topTeamLDB.setGravity(false);

        Location phyla = new Location(world, 10.5, 66.5, 51.5);

        Vector h2 = new Vector(0, 0, 0);

        List<ScoredPlayer> sortedPlayerList = new ArrayList<>();
        int[] playerScores = new int[main.getAllCompetitors().size()];

        for (int i = 0; i < playerScores.length; i++) {
            playerScores[i] = main.getAllCompetitors().get(i).score;
        }

        Arrays.sort(playerScores);

        for (int score : playerScores) {
            for (ScoredPlayer sp : main.getAllCompetitors()) {
                if (sp.score == score && !sortedPlayerList.contains(sp)) {
                    sortedPlayerList.add(sp);
                }
            }
        }

        for (ScoredPlayer sp : sortedPlayerList) {
            ArmorStand as = (ArmorStand) world.spawnEntity(phyla.add(h2), EntityType.ARMOR_STAND);
            as.setVisible(false);
            as.setCustomNameVisible(true);
            if (sp.team != null) {
                as.setCustomName(ChatColor.BOLD + (sp.team.col + sp.player.getName() + ChatColor.YELLOW + ": " + ChatColor.WHITE + sp.score));
            } else {
                as.setCustomName(ChatColor.BOLD + (ChatColor.GRAY + sp.player.getName() + ChatColor.YELLOW + ": " + ChatColor.WHITE + sp.score));
            }
            as.setGravity(false);
            h2.add(new Vector(0, 0.3, 0));
        }

        ArmorStand topPlayerLDB = (ArmorStand) world.spawnEntity(phyla.add(h2), EntityType.ARMOR_STAND);
        topPlayerLDB.setVisible(false);
        topPlayerLDB.setCustomNameVisible(true);
        topPlayerLDB.setCustomName(ChatColor.BOLD + (ChatColor.GREEN + " Player List"));
        topPlayerLDB.setGravity(false);

    }

    @Override
    public void run() {
        ticks++;
        for(ScoredPlayer p : main.playerList) {
            if(p.player.getWorld() == Bukkit.getServer().getWorld("world")) {
                if(p.player.getScoreboard() == null) {
                    setScoreBoard(p.player);
                }
                updateScoreboard(p.player);
                p.player.setFoodLevel(20);
                p.player.setHealth(20);
                p.player.setFallDistance(0);
            }
        }
        if (ticks%20 == 0) {
            updateTopList();
        }
    }

    private void setScoreBoard(Player player) {

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective(ChatColor.DARK_AQUA + (ChatColor.BOLD + "MCT #1"), "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score line1 = obj.getScore(ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 36) + ChatColor.BLUE);
        line1.setScore(15);

        Score gamePlaying = obj.getScore(ChatColor.AQUA + "Team » ");
        gamePlaying.setScore(14);

        Team playerTeam = board.registerNewTeam("playerTeam");

        playerTeam.addEntry(ChatColor.BLACK + "" + ChatColor.WHITE);

        playerTeam.setPrefix(ChatColor.RED + "No Team!");

        obj.getScore(ChatColor.BLACK + "" + ChatColor.WHITE).setScore(13);

        Score line2 = obj.getScore(ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 36) + ChatColor.RED);
        line2.setScore(13);

        Team playerScore = board.registerNewTeam("playerScore");

        playerScore.addEntry(ChatColor.GOLD + "" + ChatColor.WHITE);
        playerScore.setPrefix(ChatColor.RED + "You are not on");

        obj.getScore(ChatColor.GOLD + "" + ChatColor.WHITE).setScore(12);

        Team teamScore = board.registerNewTeam("teamScore");

        teamScore.addEntry(ChatColor.YELLOW + "" + ChatColor.WHITE);
        teamScore.setPrefix(ChatColor.RED + "a team!");

        obj.getScore(ChatColor.YELLOW + "" + ChatColor.WHITE).setScore(11);

        Score line3 = obj.getScore(ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 36) + ChatColor.YELLOW);
        line3.setScore(10);

        player.setScoreboard(board);
    }

    private void updateScoreboard(Player player) {

        Scoreboard board = player.getScoreboard();

        String playerTeam;

        if (main.getScoredPlayer(player).team == null) {
            playerTeam = ChatColor.RED + "No Team!";
        } else {
            playerTeam = main.getScoredPlayer(player).team.col + main.getScoredPlayer(player).team.teamName;
        }

        if (playerTeam.length() > 16) {
            board.getTeam("playerTeam").setPrefix(playerTeam.substring(0, 16));
            board.getTeam("playerTeam").setSuffix(playerTeam.substring(16));
        } else {
            if (board.getTeam("playerTeam") != null) {
                board.getTeam("playerTeam").setPrefix(playerTeam);
            } else {
                setScoreBoard(player);
                return;
            }
        }

        if (!player.isOp() && main.getScoredPlayer(player).team != null) {
            String playerScore = ChatColor.RED + "Your Score »  " + main.getScoredPlayer(player).score;

            if (playerScore.length() > 16) {
                board.getTeam("playerScore").setPrefix(playerScore.substring(0, 16));
                board.getTeam("playerScore").setSuffix(playerScore.substring(16));
            } else {
                board.getTeam("playerScore").setPrefix(playerScore);
            }

            String teamScore = ChatColor.RED + "Team Score »  " + main.getScoredPlayer(player).team.getScore();

            if (teamScore.length() > 16) {
                board.getTeam("teamScore").setPrefix(teamScore.substring(0, 16));
                board.getTeam("teamScore").setSuffix(teamScore.substring(16));
            } else {
                board.getTeam("teamScore").setPrefix(teamScore);
            }
        }

        player.setScoreboard(board);

    }

}