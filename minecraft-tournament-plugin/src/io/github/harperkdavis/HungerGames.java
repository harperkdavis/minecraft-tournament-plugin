package io.github.harperkdavis;

import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EnumTitleAction;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutTitle;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.util.*;


public class HungerGames extends BukkitRunnable implements Listener {

    // 0:30.0 Game Begins (300)
    // 0:40.0 Grace Period Over (400)
    // 5:00.0 Chest Refill (3000)
    // 9:00.0 Border Shrink to 100 (5400)
    // 13:00.0 Border Shrink to 50 (7800)
    // 16:00.0 Sudden Death (9600)
    // 20:00.0 Game Over (12000)

    private final MCTMain main;

    private int ticks = 0; // decaseconds

    public Map<Player, Integer> playerKills = new HashMap<Player, Integer>();
    public World world;

    public HungerGames(MCTMain main, Player sender) {
        world = sender.getWorld();

        this.main = main;
        main.tasksRunning.add(this);
        generateChestLoot(sender);
        // Initial
        World world = sender.getWorld();
        world.getWorldBorder().setSize(400);

        List<Location> spawnLocations = new ArrayList<Location>();
        spawnLocations.add(world.getHighestBlockAt(16,0).getLocation().add(new Vector(0,1,0)));
        spawnLocations.add(world.getHighestBlockAt(-16,0).getLocation().add(new Vector(0,1,0)));
        spawnLocations.add(world.getHighestBlockAt(0,16).getLocation().add(new Vector(0,1,0)));
        spawnLocations.add(world.getHighestBlockAt(0,-16).getLocation().add(new Vector(0,1,0)));

        for(int i = 0; i < main.teamList.size() ; i++) {
            ScoredTeam t = main.teamList.get(i);
            t.player1.player.teleport(spawnLocations.get(i).add(0.5, 0, 0.5));
            if (t.player2 != null) {
                t.player2.player.teleport(spawnLocations.get(i).add(new Vector(0, 3, 0)));
            }

        }

        for (ScoredPlayer p : main.getAllCompetitors()) {
            p.player.setWalkSpeed(0);
            p.player.getInventory().clear();
            p.player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 600, 128, false, false), true);
            p.player.setHealth(20);
            p.player.setFoodLevel(20);
            p.player.setSaturation(10);
            p.player.setLevel(0);
            playerKills.put(p.player, 0);
        }

        sender.getWorld().setPVP(false);
    }



    @Override
    public void run() {

        ticks++;
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
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 5, 40, 5);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
            }
        } else if (ticks == 250) {
            main.broadcastTitle("5", (ChatColor.YELLOW), EnumTitleAction.SUBTITLE, 0, 20, 0);
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 5, 40, 5);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
            }
        } else if (ticks == 260) {
            main.broadcastTitle("4", (ChatColor.YELLOW), EnumTitleAction.SUBTITLE, 0, 20, 0);
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 5, 40, 5);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
            }
        } else if (ticks == 270) {
            main.broadcastTitle("3", (ChatColor.YELLOW), EnumTitleAction.SUBTITLE, 0, 20, 0);
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 5, 40, 5);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
            }
        }   else if (ticks == 280) {
            main.broadcastTitle("2", (ChatColor.YELLOW), EnumTitleAction.SUBTITLE, 0, 20, 0);
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 5, 40, 5);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
            }
        }else if (ticks == 290) {
            main.broadcastTitle("1", (ChatColor.YELLOW), EnumTitleAction.SUBTITLE, 0, 20, 0);
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 5, 40, 5);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.CLICK, 1, 2);
            }
        } else if (ticks == 300) { // Start Game
            main.broadcastTitle("FIGHT!", (ChatColor.RED), EnumTitleAction.TITLE, 5, 20, 40);
            main.broadcastTitle("", ChatColor.WHITE, EnumTitleAction.TITLE, 5, 40, 5);
            for (ScoredPlayer p : main.getAllCompetitors()) {
                p.player.setWalkSpeed(0.2f);
                p.addScore(5, "participating");
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1, 1);
            }
        } else if (ticks == 400) {
            world.setPVP(true);
        }


    }

    public void setScoreBoard(Player player) {

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective(ChatColor.DARK_AQUA + (ChatColor.BOLD + "MCT #1"), "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score gamePlaying = obj.getScore(ChatColor.AQUA + "Game »" + ChatColor.WHITE + "Hunger Games");
        gamePlaying.setScore(15);

        Team gameTimer = board.registerNewTeam("gameTimer");

        gameTimer.addEntry(ChatColor.BLACK + "" + ChatColor.WHITE);

        // 0:30.0 Game Begins (300)
        // 0:40.0 Grace Period Over (400)
        // 5:00.0 Chest Refill (3000)
        // 9:00.0 Border Shrink to 100 (5400)
        // 13:00.0 Border Shrink to 50 (7800)
        // 16:00.0 Sudden Death (9600)
        // 20:00.0 Game Over (12000)


        int ticksLeft = ticks;
        String nextEvent = "";
        if (ticks <= 300) {
            ticksLeft -= 0;
            nextEvent = "Game Start";
        } else if (ticks <= 400) {
            ticksLeft -= 300;
            nextEvent = "Grace Over";
        } else if (ticks <= 3000) {
            ticksLeft -= 400;
            nextEvent = "Chest Refill";
        } else if (ticks <= 5400) {
            ticksLeft -= 3000;
            nextEvent = "Border (100)";
        } else if (ticks <= 7800) {
            ticksLeft -= 5400;
            nextEvent = "Border (50)";
        } else if (ticks <= 9600) {
            ticksLeft -= 7800;
            nextEvent = "Sudden Death";
        } else if (ticks <= 12000) {
            ticksLeft -= 9600;
            nextEvent = "Game End";
        }

        String seconds = "" + (int)Math.floor((float)ticksLeft/10);
        String minutes = "" + (int)Math.floor((float)ticksLeft/600);

        gameTimer.setPrefix(ChatColor.AQUA + nextEvent + " » " + ChatColor.WHITE + minutes + ":" + seconds);

        obj.getScore(ChatColor.BLACK + "" + ChatColor.WHITE).setScore(14);



        Team playerKills = board.registerNewTeam("playerKills");

        playerKills.addEntry(ChatColor.RED + "" + ChatColor.WHITE);
        playerKills.setPrefix(ChatColor.AQUA + "Kills » " + ChatColor.WHITE + "0");

        obj.getScore(ChatColor.RED + "" + ChatColor.WHITE).setScore(13);

        Score space = obj.getScore(ChatColor.GRAY + " ");
        space.setScore(12);

        Team playerScore = board.registerNewTeam("playerKills");

        playerScore.addEntry(ChatColor.GOLD + "" + ChatColor.WHITE);
        playerKills.setPrefix(ChatColor.RED + "Your Score » " + ChatColor.WHITE + "0");

        obj.getScore(ChatColor.GOLD + "" + ChatColor.WHITE).setScore(11);

        Team teamScore = board.registerNewTeam("playerKills");

        teamScore.addEntry(ChatColor.YELLOW + "" + ChatColor.WHITE);
        teamScore.setPrefix(ChatColor.RED + "Team Score » " + ChatColor.WHITE + "0");

        obj.getScore(ChatColor.YELLOW + "" + ChatColor.WHITE).setScore(10);

    }

    public void registerKill(Player killer, Player killed) {
        playerKills.put(killer, playerKills.get(killer)+1);
        ScoredPlayer scoredKiller = main.getScoredPlayer(killer);
        scoredKiller.addScore(15, "kill");
        for (ScoredPlayer p : main.getAllCompetitors()) {
            if (p.player.getGameMode() != GameMode.SPECTATOR) {
                p.addScore(2, "surviving");
            }
        }
    }

    public void generateChestLoot(Player sender) {

        Bukkit.getServer().broadcastMessage(ChatColor.BLUE+"Generating Chests...");

        List<String> tier1Loot = main.getConfig().getConfigurationSection("HungerGames").getConfigurationSection("Tables").getStringList("Tier1");
        List<String> tier2Loot = main.getConfig().getConfigurationSection("HungerGames").getConfigurationSection("Tables").getStringList("Tier2");
        List<String> tier3Loot = main.getConfig().getConfigurationSection("HungerGames").getConfigurationSection("Tables").getStringList("Tier3");
        List<String> midLoot = main.getConfig().getConfigurationSection("HungerGames").getConfigurationSection("Tables").getStringList("Mid");

        //Bukkit.getServer().broadcastMessage(ChatColor.BLUE+"Loaded Loot Tables:");


        if(sender != null) {
            Player player = (Player) sender;
            //Bukkit.getServer().broadcastMessage(ChatColor.GRAY+player.getName());

            int t1c = 0;
            int t2c = 0;
            int t3c = 0;

            int apples = 0;
            int iron = 0;
            int gold = 0;
            int diamond = 0;

            for(int x = -15; x <= 15; x++) {
                for(int z = -15; z <= 15; z++) {

                    Chunk c = player.getWorld().getChunkAt(x,z);

                    String chp = ("Loaded Chunk: "+ c.getX()+","+c.getZ());
                    //Bukkit.getServer().broadcastMessage(ChatColor.GRAY+chp);

                    for (BlockState b : c.getTileEntities()) {

                        if (b instanceof Chest) {
                            Chest chest = (Chest) b;

                            Inventory inv = chest.getInventory();

                            inv.clear();

                            List<String> lootTable;

                            String chsp = ("Found A Chest At: "+ chest.getX()+","+chest.getY()+","+chest.getZ());
                            //Bukkit.getServer().broadcastMessage(ChatColor.GOLD+chsp);

                            String lt = ("This Chest's Name is: " + inv.getTitle());

                            //Bukkit.getServer().broadcastMessage(ChatColor.AQUA+lt);

                            double chanceSpawn;

                            switch (inv.getTitle()) {
                                case "Tier 1":
                                    lootTable = tier1Loot;
                                    chanceSpawn = 0.15;
                                    t1c++;
                                    break;
                                case "Tier 2":
                                    lootTable = tier2Loot;
                                    chanceSpawn = 0.15;
                                    t2c++;
                                    break;
                                case "Tier 3":
                                    lootTable = tier3Loot;
                                    chanceSpawn = 0.08;
                                    t3c++;
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

                                if(Material.getMaterial(items.toUpperCase()) == null) {
                                    //Bukkit.getServer().broadcastMessage(ChatColor.BLUE+"The Culprit is: " + items);
                                }

                                ItemStack newItem;

                                if(Material.getMaterial(items.toUpperCase()) == Material.ARROW || Material.getMaterial(items.toUpperCase()) == Material.COAL) {
                                    newItem = new ItemStack(Material.getMaterial(items.toUpperCase()), 4);
                                } else if (Material.getMaterial(items.toUpperCase()) == Material.EXP_BOTTLE){
                                    newItem = new ItemStack(Material.getMaterial(items.toUpperCase()), 16);
                                } else {
                                    newItem = new ItemStack(Material.getMaterial(items.toUpperCase()));
                                }

                                if (newItem.getType() == Material.APPLE) {
                                    apples ++;
                                } else if (newItem.getType() == Material.IRON_INGOT) {
                                    iron ++;
                                } else if (newItem.getType() == Material.GOLD_INGOT) {
                                    gold ++;
                                } else if (newItem.getType() == Material.DIAMOND) {
                                    diamond ++;
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

            //completed
            Bukkit.getServer().broadcastMessage(ChatColor.BLUE+"Finished Generating Chests!");
            Bukkit.getServer().broadcastMessage(ChatColor.DARK_PURPLE+"Tier 1: "+ChatColor.WHITE + ChatColor.BOLD + t1c);
            Bukkit.getServer().broadcastMessage(ChatColor.DARK_PURPLE+"Tier 2: "+ChatColor.WHITE + ChatColor.BOLD + t2c);
            Bukkit.getServer().broadcastMessage(ChatColor.DARK_PURPLE+"Tier 3: "+ChatColor.WHITE + ChatColor.BOLD + t3c);

            Bukkit.getServer().broadcastMessage(ChatColor.DARK_PURPLE+"Apples "+ChatColor.WHITE + ChatColor.BOLD + apples);
            Bukkit.getServer().broadcastMessage(ChatColor.DARK_PURPLE+"Iron "+ChatColor.WHITE + ChatColor.BOLD + iron);
            Bukkit.getServer().broadcastMessage(ChatColor.DARK_PURPLE+"Gold "+ChatColor.WHITE + ChatColor.BOLD + gold);
            Bukkit.getServer().broadcastMessage(ChatColor.DARK_PURPLE+"Diamond "+ChatColor.WHITE + ChatColor.BOLD + diamond);
        }



    }



}