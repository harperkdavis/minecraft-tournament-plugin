package io.github.harperkdavis;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;

public class Manhunt extends BukkitRunnable {

    private int ticks = 0;

    public Map<Player, Player> playerTargets= new HashMap<Player, Player>();
    private MCTMain main;

    private World world;

    public Manhunt(MCTMain main, World world) {
        this.main = main;
        this.world = world;
        main.tasksRunning.add(this);
        // Pick List
        Boolean targets = false;
        List<ScoredPlayer> players = main.getAllCompetitors();
        while(!targets) {
            Bukkit.getServer().broadcastMessage("while loop");
            Collections.shuffle(players);
            targets = true;
            for (int i = 0; i < players.size() - 1; i++) {
                if(players.get(i).team == players.get(i+1).team) {
                    Bukkit.getServer().broadcastMessage("monopoly" + i);
                    targets = false;
                    break;
                }
            }
            if (players.get(players.size() - 1).team == players.get(0).team) {
                targets = false;
                Bukkit.getServer().broadcastMessage("not epic");
            } else {
                Bukkit.getServer().broadcastMessage("epic");
            }

        }
        for(int i = 0; i < players.size()-1; i++) {
            playerTargets.put(players.get(i).player, players.get(i + 1).player);
            Bukkit.getServer().broadcastMessage("efe"+i);
        }
        playerTargets.put(players.get(players.size()-1).player, players.get(0).player);
        for (ScoredPlayer p : main.getAllCompetitors()) {
            int xPos = new Random().nextInt(2000)-1000;
            int zPos = new Random().nextInt(2000)-1000;
            p.player.teleport(new Location(world, xPos,world.getHighestBlockYAt(xPos,zPos),zPos));
            p.player.getInventory().clear();
            p.player.getInventory().setItem(8, new ItemStack (Material.COMPASS, 1));
            p.player.setHealth(20);
            p.player.setFoodLevel(20);
            p.player.setSaturation(20);
            p.player.setExp(0);
            p.player.setLevel(0);
        }
        for (ScoredPlayer p : main.playerList) {
            setScoreBoard(p.player);
        }
        main.allowBlockChanging = true;

        for (ScoredPlayer p : main.getAllCompetitors()) {
            p.addScore(5, "participating");
        }
    }



    @Override
    public void run() {
        ticks++;
        for (ScoredPlayer p : main.getAllCompetitors()) {
            p.player.setCompassTarget(playerTargets.get(p.player).getLocation());
        }
        for (ScoredPlayer p : main.playerList) {
            updateScoreboard(p.player);
        }
        if (ticks % 600 == 0) {
            for (ScoredPlayer p : main.getAllCompetitors()) {
                if (p.player.getGameMode() == GameMode.SURVIVAL) {
                    p.addScore((int) ticks / 1200 + 1, "surviving");
                }
            }
        }
        if (ticks == 9000) {
            this.cancel();
        }
    }

    public void registerKill(Player killer, Player killed) {
        ScoredPlayer scoredKiller = main.getScoredPlayer(killer);
        if (playerTargets.get(killer) == killed) {
            scoredKiller.addScore(15, "killed target");
        }
        ScoredPlayer scoredHunted = main.getScoredPlayer(playerTargets.get(killed));
        scoredHunted.addScore(5,"hunter died (" + scoredHunted.player.getName() + ")");
        if (killer == playerTargets.get(killed)) {
            scoredHunted.addScore(15,"killing your hunter (" + scoredHunted.player.getName() + ")");
        }
    }

    public void setScoreBoard(Player player) {

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective(ChatColor.DARK_AQUA + (ChatColor.BOLD + "MCT #1"), "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score line1 = obj.getScore(ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 36) + ChatColor.BLUE);
        line1.setScore(15);

        Score gamePlaying = obj.getScore(ChatColor.AQUA + "Game » " + ChatColor.WHITE + "Manhunt");
        gamePlaying.setScore(14);

        Team gameTimer = board.registerNewTeam("gameTimer");

        gameTimer.addEntry(ChatColor.BLACK + "" + ChatColor.WHITE);

        gameTimer.setPrefix("n");


        obj.getScore(ChatColor.BLACK + "" + ChatColor.WHITE).setScore(13);

        Score space1 = obj.getScore(ChatColor.GRAY + " ");
        space1.setScore(12);

        Score target = obj.getScore(ChatColor.AQUA + "Your Target » ");
        target.setScore(11);

        Team playerTarget = board.registerNewTeam("playerTarget");

        playerTarget.addEntry(ChatColor.RED + "" + ChatColor.WHITE);
        playerTarget.setPrefix(ChatColor.GRAY + "Nobody :(");

        obj.getScore(ChatColor.RED + "" + ChatColor.WHITE).setScore(10);

        Score line2 = obj.getScore(ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 36) + ChatColor.RED);
        line2.setScore(9);

        Team playerScore = board.registerNewTeam("playerScore");

        playerScore.addEntry(ChatColor.GOLD + "" + ChatColor.WHITE);
        playerScore.setPrefix("You are");

        obj.getScore(ChatColor.GOLD + "" + ChatColor.WHITE).setScore(8);

        Team teamScore = board.registerNewTeam("teamScore");

        teamScore.addEntry(ChatColor.YELLOW + "" + ChatColor.WHITE);
        teamScore.setPrefix("an admin");

        obj.getScore(ChatColor.YELLOW + "" + ChatColor.WHITE).setScore(7);

        Score line3 = obj.getScore(ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 36) + ChatColor.LIGHT_PURPLE);
        line3.setScore(6);

        Score name = obj.getScore(ChatColor.DARK_GRAY + player.getName());
        name.setScore(5);

        player.setScoreboard(board);
    }

    public void updateScoreboard(Player player) {
        if(player.getWorld() != world) {
            return;
        }

        Scoreboard board = player.getScoreboard();

        int ticksLeft = ticks;

        ticksLeft = Math.abs(ticksLeft - 9000);
        ticksLeft += 20;

        String seconds = "" + (int) Math.floor((float) ticksLeft / 10f) % 60;
        String minutes = "" + (int) Math.floor((float) ticksLeft / 600f);

        if (seconds.length() == 1) {
            seconds = "0" + seconds;
        }

        String gameTimer = ChatColor.AQUA + "Kill Your Target » " + minutes + ":" + seconds;
        if (board.getTeam("gameTimer") == null) {
            setScoreBoard(player);
            return;
        }

        if (gameTimer.length() > 16) {
            board.getTeam("gameTimer").setPrefix(gameTimer.substring(0, 16));
            board.getTeam("gameTimer").setSuffix(gameTimer.substring(16));
        } else {
            board.getTeam("gameTimer").setPrefix(gameTimer);
        }
        if (!player.isOp()) {
            String playerTarget = ChatColor.WHITE + playerTargets.get(player).getName() + " " + ChatColor.YELLOW + (Math.round(playerTargets.get(player).getHealth() * 10) / 10) + "❤";

            if (playerTarget.length() > 16) {
                board.getTeam("playerTarget").setPrefix(playerTarget.substring(0, 16));
                board.getTeam("playerTarget").setSuffix(playerTarget.substring(16));
            } else {
                board.getTeam("playerTarget").setPrefix(playerTarget);
            }

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

    }
}
