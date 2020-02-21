package io.github.harperkdavis;

import net.minecraft.server.v1_8_R1.EnumTitleAction;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.util.*;


public class HungerGames extends BukkitRunnable implements Listener {

    // 0:30.0 Game Begins (300)
    // 0:40.0 Grace Period Over (400)
    // 7:00.0 Chest Refill (4200)
    // 9:00.0 Border Shrink to 100 (5400)
    // 12:00.0 Border Shrink to 50 (7200)

    private final MCTMain main;

    private int ticks = 0; // deca seconds

    private Map<Player, Integer> playerKills = new HashMap<>();
    private World world;

    HungerGames(MCTMain main, Player sender) {
        world = sender.getWorld();

        this.main = main;
        main.tasksRunning.add(this);
        generateChestLoot(sender.getWorld());
        main.allowBlockChanging = false;
        // Initial
        World world = sender.getWorld();
        world.getWorldBorder().setSize(400);

        List<Location> spawnLocations = new ArrayList<>();
        spawnLocations.add(world.getHighestBlockAt(16, 0).getLocation().add(new Vector(0, 0, 0)));
        spawnLocations.add(world.getHighestBlockAt(-16, 0).getLocation().add(new Vector(0, 0, 0)));
        spawnLocations.add(world.getHighestBlockAt(0, 16).getLocation().add(new Vector(0, 0, 0)));
        spawnLocations.add(world.getHighestBlockAt(0, -16).getLocation().add(new Vector(0, 0, 0)));
        spawnLocations.add(world.getHighestBlockAt(12, 12).getLocation().add(new Vector(0, 0, 0)));
        spawnLocations.add(world.getHighestBlockAt(-12, -12).getLocation().add(new Vector(0, 0, 0)));
        spawnLocations.add(world.getHighestBlockAt(-12, 12).getLocation().add(new Vector(0, 0, 0)));
        spawnLocations.add(world.getHighestBlockAt(12, -12).getLocation().add(new Vector(0, 0, 0)));

        for (int i = 0; i < main.teamList.size(); i++) {
            ScoredTeam t = main.teamList.get(i);
            t.player1.player.teleport(spawnLocations.get(i).add(0.5, 0, 0.5));
            if (t.player2 != null) {
                t.player2.player.teleport(spawnLocations.get(i).add(new Vector(0, 3, 0)));
            }

        }

        for (ScoredPlayer p : main.getAllCompetitors()) {
            p.player.setWalkSpeed(0);
            p.player.setExp(0);
            p.player.getInventory().clear();
            p.player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 600, 128, false, false), true);
            p.player.setHealth(20);
            p.player.setFoodLevel(20);
            p.player.setSaturation(10);
            p.player.setLevel(0);
            setBarriers(p.player.getLocation(), true);
        }

        for (ScoredPlayer p : main.playerList) {
            setScoreBoard(p.player);
            playerKills.put(p.player, 0);
        }

        sender.getWorld().setPVP(false);
    }

    private void setBarriers(Location loc, Boolean setBarrier) {
        if (setBarrier) {
            world.getBlockAt(loc.add(new Vector(0.5, 0, 0))).setType(Material.BARRIER);
            world.getBlockAt(loc.add(new Vector(0, 0, 0.5))).setType(Material.BARRIER);
            world.getBlockAt(loc.add(new Vector(-1.5, 0, 0))).setType(Material.BARRIER);
            world.getBlockAt(loc.add(new Vector(0, 0, -1.5))).setType(Material.BARRIER);
        } else {
            world.getBlockAt(loc.add(new Vector(0.5, 0, 0))).setType(Material.AIR);
            world.getBlockAt(loc.add(new Vector(0, 0, 0.5))).setType(Material.AIR);
            world.getBlockAt(loc.add(new Vector(-1.5, 0, 0))).setType(Material.AIR);
            world.getBlockAt(loc.add(new Vector(0, 0, -1.5))).setType(Material.AIR);
        }
    }

    private void spawnAirdrop(int x, int z) {

        Location airdrop = world.getHighestBlockAt(x, z).getLocation().add(new Vector(0, 2, 0));

        main.broadcastTitle(x + ", " + z, (ChatColor.LIGHT_PURPLE), EnumTitleAction.SUBTITLE, 40, 80, 40);
        main.broadcastTitle("AIRDROP", (ChatColor.YELLOW), EnumTitleAction.TITLE, 40, 80, 40);

        Bukkit.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "Airdrop at " + x + ", " + z);

        airdrop.getBlock().setType(Material.CHEST);

        Chest chest = (Chest) airdrop.getBlock().getState();

        Inventory inv = chest.getInventory();

        inv.clear();

        List<String> lootTable = main.getConfig().getConfigurationSection("HungerGames").getConfigurationSection("Tables").getStringList("Airdrop");

        for (int i = 0; i < 27; i++) {

            int index = new Random().nextInt(lootTable.size());
            String items = lootTable.get(index);

            ItemStack newItem;

            if (Material.getMaterial(items.toUpperCase()) == Material.ARROW || Material.getMaterial(items.toUpperCase()) == Material.COAL) {
                newItem = new ItemStack(Material.getMaterial(items.toUpperCase()), 16);
            } else if (Material.getMaterial(items.toUpperCase()) == Material.EXP_BOTTLE) {
                newItem = new ItemStack(Material.getMaterial(items.toUpperCase()), 32);
            } else {
                newItem = new ItemStack(Material.getMaterial(items.toUpperCase()));
            }

            if (Math.random() < 0.25) {
                chest.getInventory().setItem(i, newItem);
            } else {
                chest.getInventory().setItem(i, new ItemStack(Material.AIR, 1));
            }


        }
    }


    @Override
    public void run() {
        ticks++;
        for (ScoredPlayer p : main.playerList) {
            updateScoreboard(p.player);
        }
        endGame();

        if (ticks == 1) {
            main.broadcastTitle("WEEK #1", (ChatColor.YELLOW), EnumTitleAction.SUBTITLE, 20, 40, 20);
            main.broadcastTitle("MINECRAFT TOURNAMENT", (ChatColor.RED), EnumTitleAction.TITLE, 20, 40, 20);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 0);
            }
        } else if (ticks == 100) {
            main.broadcastTitle("HUNGER GAMES", (ChatColor.RED), EnumTitleAction.TITLE, 20, 40, 20);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
            }
        } else if (ticks == 200) {
            main.broadcastTitle("10", (ChatColor.YELLOW), EnumTitleAction.SUBTITLE, 0, 20, 0);
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 0, 20, 0);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
            }
        } else if (ticks == 250) {
            main.broadcastTitle("5", (ChatColor.YELLOW), EnumTitleAction.SUBTITLE, 0, 20, 0);
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 0, 20, 0);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
            }
        } else if (ticks == 260) {
            main.broadcastTitle("4", (ChatColor.YELLOW), EnumTitleAction.SUBTITLE, 0, 20, 0);
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 0, 20, 0);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
            }
        } else if (ticks == 270) {
            main.broadcastTitle("3", (ChatColor.YELLOW), EnumTitleAction.SUBTITLE, 0, 20, 0);
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 0, 20, 0);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
            }
        } else if (ticks == 280) {
            main.broadcastTitle("2", (ChatColor.YELLOW), EnumTitleAction.SUBTITLE, 0, 20, 0);
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 0, 20, 0);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
            }
        } else if (ticks == 290) {
            main.broadcastTitle("1", (ChatColor.YELLOW), EnumTitleAction.SUBTITLE, 0, 20, 0);
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 0, 20, 0);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
            }
        } else if (ticks == 300) { // Start Game
            main.broadcastTitle("", (ChatColor.YELLOW), EnumTitleAction.SUBTITLE, 0, 20, 0);
            main.broadcastTitle("FIGHT!", (ChatColor.RED), EnumTitleAction.TITLE, 5, 20, 40);
            for (ScoredPlayer p : main.getAllCompetitors()) {
                p.addScore(5, "participating");
                setBarriers(p.player.getLocation(), false);
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1, 1);
                player.setWalkSpeed(0.2f);
            }
        } else if (ticks == 400) {
            world.setPVP(true);
            main.broadcastTitle("PVP Enabled!", (ChatColor.RED), EnumTitleAction.SUBTITLE, 0, 20, 0);
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 0, 20, 0);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.WITHER_DEATH, 1, 1);
                player.setWalkSpeed(0.2f);
            }
        } else if (ticks == 3000) {
            int x = new Random().nextInt(360) - 180;
            int z = new Random().nextInt(360) - 180;
            while(distance(x,z) > 170) {
                x = new Random().nextInt(360) - 180;
                z = new Random().nextInt(360) - 180;
            }
            spawnAirdrop(x, z);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 0);
            }
        } else if (ticks == 4200) {
            generateChestLoot(world);
            main.broadcastTitle("Chests have been refilled!", (ChatColor.YELLOW), EnumTitleAction.SUBTITLE, 10, 20, 10);
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 10, 20, 10);
            Bukkit.getServer().broadcastMessage(ChatColor.RED + "Chests " + ChatColor.YELLOW + "have been refilled!");
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.CHEST_CLOSE, 1, 1);
            }
        } else if (ticks == 5400) {
            main.broadcastTitle("Get within +100, -100!", (ChatColor.DARK_RED), EnumTitleAction.SUBTITLE, 10, 20, 10);
            main.broadcastTitle("! BORDER !", ChatColor.RED, EnumTitleAction.TITLE, 10, 20, 10);
            Bukkit.getServer().broadcastMessage(ChatColor.RED + "Border is shrinking! Get within +100, -100");
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.WITHER_IDLE, 1, 0);
            }
            world.getWorldBorder().setSize(200, 30);
        } else if (ticks == 6000) {
            int x = new Random().nextInt(196) - 93;
            int z = new Random().nextInt(196) - 93;
            spawnAirdrop(x, z);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.FIREWORK_LAUNCH, 1, 0);
            }
        } else if (ticks == 7200) {
            main.broadcastTitle("Get within +25, -25", (ChatColor.DARK_RED), EnumTitleAction.SUBTITLE, 10, 20, 10);
            main.broadcastTitle("! BORDER !", ChatColor.RED, EnumTitleAction.TITLE, 10, 20, 10);
            Bukkit.getServer().broadcastMessage(ChatColor.RED + "Border is shrinking! Get within +25, -25");
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.WITHER_IDLE, 1, 0);
            }
            world.getWorldBorder().setSize(50, 30);
        }

        // 0:30.0 Game Begins (300)
        // 0:40.0 Grace Period Over (400)
        // 5:00.0 Airdrop #1
        // 7:00.0 Chest Refill (4200)
        // 9:00.0 Border Shrink to 100 (5400)
        // 10:00.0 Airdrop #2
        // 12:00.0 Border Shrink to 50 (7200)

    }

    private double distance(double x1, double y1) {
        return Math.sqrt(((double) 0 - y1) * ((double) 0 - y1) + ((double) 0 - x1) * ((double) 0 - x1));
    }

    private void setScoreBoard(Player player) {

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective(ChatColor.DARK_AQUA + (ChatColor.BOLD + "MCT #1"), "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score line1 = obj.getScore(ChatColor.STRIKETHROUGH + StringUtils.repeat(" ", 36) + ChatColor.BLUE);
        line1.setScore(15);

        Score gamePlaying = obj.getScore(ChatColor.AQUA + "Game » " + ChatColor.WHITE + "Hunger Games");
        gamePlaying.setScore(14);

        Team gameTimer = board.registerNewTeam("gameTimer");

        gameTimer.addEntry(ChatColor.BLACK + "" + ChatColor.WHITE);

        // 0:30.0 Game Begins (300)
        // 0:40.0 Grace Period Over (400)
        // 7:00.0 Chest Refill (4200)
        // 9:00.0 Border Shrink to 100 (5400)
        // 12:00.0 Border Shrink to 50 (7200)

        gameTimer.setPrefix("n");


        obj.getScore(ChatColor.BLACK + "" + ChatColor.WHITE).setScore(13);

        Score space1 = obj.getScore(ChatColor.GRAY + " ");
        space1.setScore(12);

        Team playersLeft = board.registerNewTeam("playersLeft");

        playersLeft.addEntry(ChatColor.DARK_RED + "" + ChatColor.WHITE);
        playersLeft.setPrefix("SOMETHING IS");

        obj.getScore(ChatColor.DARK_RED + "" + ChatColor.WHITE).setScore(11);

        Team playerKillsS = board.registerNewTeam("playerKills");

        playerKillsS.addEntry(ChatColor.RED + "" + ChatColor.WHITE);
        playerKillsS.setPrefix("BROKEN");

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

    private void updateScoreboard(Player player) {
        if(player.getWorld() != world) {
            return;
        }
        Scoreboard board = player.getScoreboard();

        int ticksLeft = ticks;
        String nextEvent;
        // 0:30.0 Game Begins (300)
        // 0:40.0 Grace Period Over (400)
        // 7:00.0 Chest Refill (4200)
        // 9:00.0 Border Shrink to 100 (5400)
        // 12:00.0 Border Shrink to 50 (7200)
        if (ticks <= 300) {
            nextEvent = "Game Start";
            ticksLeft -= 300;
        } else if (ticks <= 400) {
            ticksLeft -= 400;
            nextEvent = "Grace Over";
        } else if (ticks <= 4200) {
            ticksLeft -= 4200;
            nextEvent = "Chest Refill";
        } else if (ticks <= 5400) {
            ticksLeft -= 5400;
            nextEvent = "Border (200)";
        } else if (ticks <= 7200) {
            ticksLeft -= 7200;
            nextEvent = "Border (50)";
        } else {
            ticksLeft = 0;
            nextEvent = "Final Fight!";
        }

        ticksLeft = Math.abs(ticksLeft);
        ticksLeft += 20;

        String seconds = "" + (int) Math.floor((float) ticksLeft / 10f) % 60;
        String minutes = "" + (int) Math.floor((float) ticksLeft / 600f);

        if (seconds.length() == 1) {
            seconds = "0" + seconds;
        }

        String gameTimer = ChatColor.AQUA + nextEvent + " » " + minutes + ":" + seconds;
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

        int pl = 0;
        for (ScoredPlayer p : main.getAllCompetitors()) {
            if (p.player.getGameMode() == GameMode.SURVIVAL) {
                pl++;
            }
        }

        String playersLeft = ChatColor.AQUA + "Remaining » " + ChatColor.WHITE + pl;

        if (playersLeft.length() > 16) {
            board.getTeam("playersLeft").setPrefix(playersLeft.substring(0, 16));
            board.getTeam("playersLeft").setSuffix(playersLeft.substring(16));
        } else {
            board.getTeam("gameTimer").setPrefix(playersLeft);
        }

        String playerKillsS = ChatColor.AQUA + "Kills » " + ChatColor.WHITE + playerKills.get(player);

        if (playerKillsS.length() > 16) {
            board.getTeam("playerKills").setPrefix(playerKillsS.substring(0, 16));
            board.getTeam("playerKills").setSuffix(playerKillsS.substring(16));
        } else {
            board.getTeam("playerKills").setPrefix(playerKillsS);
        }
        if (!player.isOp()) {
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


    void registerKill(Player killer) {
        playerKills.put(killer, playerKills.get(killer) + 1);
        ScoredPlayer scoredKiller = main.getScoredPlayer(killer);
        scoredKiller.addScore(15, "kill");
        for (ScoredPlayer p : main.getAllCompetitors()) {
            if (p.player.getGameMode() != GameMode.SPECTATOR) {
                p.addScore(2, "surviving");
            }
        }
    }

    private void endGame() {

        List<ScoredTeam> teamsAlive = new ArrayList<>();
        for (ScoredPlayer p : main.getAllCompetitors()) {
            if (p.player.getGameMode() == GameMode.SURVIVAL) {
                if (!teamsAlive.contains(p.team)) {
                    teamsAlive.add(p.team);
                }
            }
        }
        if (teamsAlive.size() == 1) {
            main.broadcastTitle(teamsAlive.get(0).teamName + " wins!", (ChatColor.GRAY), EnumTitleAction.SUBTITLE, 10, 20, 10);
            main.broadcastTitle("VICTORY!", ChatColor.GOLD, EnumTitleAction.TITLE, 10, 20, 10);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
            }
            this.cancel();
        }
    }

    private void generateChestLoot(World world) {

        List<String> tier1Loot = main.getConfig().getConfigurationSection("HungerGames").getConfigurationSection("Tables").getStringList("Tier1");
        List<String> tier2Loot = main.getConfig().getConfigurationSection("HungerGames").getConfigurationSection("Tables").getStringList("Tier2");
        List<String> tier3Loot = main.getConfig().getConfigurationSection("HungerGames").getConfigurationSection("Tables").getStringList("Tier3");
        List<String> midLoot = main.getConfig().getConfigurationSection("HungerGames").getConfigurationSection("Tables").getStringList("Mid");

        //Bukkit.getServer().broadcastMessage(ChatColor.BLUE+"Loaded Loot Tables:");

        //Bukkit.getServer().broadcastMessage(ChatColor.GRAY+player.getName());

        for (int x = -15; x <= 15; x++) {
            for (int z = -15; z <= 15; z++) {

                Chunk c = world.getChunkAt(x, z);

                for (BlockState b : c.getTileEntities()) {

                    if (b instanceof Chest) {
                        Chest chest = (Chest) b;

                        Inventory inv = chest.getInventory();

                        inv.clear();

                        List<String> lootTable;

                        double chanceSpawn;

                        switch (inv.getTitle()) {
                            case "Tier 1":
                                lootTable = tier1Loot;
                                chanceSpawn = 0.15;
                                break;
                            case "Tier 2":
                                lootTable = tier2Loot;
                                chanceSpawn = 0.15;
                                break;
                            case "Tier 3":
                                lootTable = tier3Loot;
                                chanceSpawn = 0.12;
                                break;
                            case "Mid Chest":
                                lootTable = midLoot;
                                chanceSpawn = 0.12;
                                break;
                            default:
                                lootTable = tier1Loot;
                                chanceSpawn = 0;
                                break;
                        }


                        for (int i = 0; i < 27; i++) {

                            int index = new Random().nextInt(lootTable.size());
                            String items = lootTable.get(index);

                            ItemStack newItem;

                            if (Material.getMaterial(items.toUpperCase()) == Material.ARROW || Material.getMaterial(items.toUpperCase()) == Material.COAL) {
                                newItem = new ItemStack(Material.getMaterial(items.toUpperCase()), 4);
                            } else if (Material.getMaterial(items.toUpperCase()) == Material.EXP_BOTTLE) {
                                newItem = new ItemStack(Material.getMaterial(items.toUpperCase()), 16);
                            } else {
                                newItem = new ItemStack(Material.getMaterial(items.toUpperCase()));
                            }

                            if (Math.random() < chanceSpawn) {
                                chest.getInventory().setItem(i, newItem);
                            } else {
                                chest.getInventory().setItem(i, new ItemStack(Material.AIR, 1));
                            }

                        }
                    }
                }
            }

        }

    }


}