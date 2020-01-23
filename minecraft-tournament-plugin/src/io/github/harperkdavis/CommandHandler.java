package io.github.harperkdavis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

public class CommandHandler implements CommandExecutor {

    private final MCTMain main;

    CommandHandler (MCTMain main_) {
        main = main_;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {

        if (sender instanceof Player) {

            Player player = (Player)sender;

            player.sendMessage(ChatColor.BLUE + player.getDisplayName() + ChatColor.WHITE + " has run the command with args: " + ChatColor.BOLD + String.valueOf(args.length));
            for (int i = 0; i < args.length ; i ++) {
                String arg = args[i];
                player.sendMessage(ChatColor.GREEN + "args["+i+"]: " + ChatColor.WHITE + args[i]);
            }

            if (args.length == 0) {
                displayHelp(sender);
            } else if (args.length == 1) {
                if (args[0].equals("team")) { // team add person
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Team Schtuff");
                    if (main.getScoredPlayer(player).team == null) { // no team
                        player.sendMessage(ChatColor.RED + "You are not on a team. Create a team with /mct team create <name>");
                    } else { // on team (make info)
                        main.getScoredPlayer(player).team.sendInfo(player);
                    }
                }
            } else if (args.length == 2) {
                if (args[0] == "start") {
                    if(args[1] == "sg") { //start survival games
                        generateChestLoot(sender);
                        BukkitTask task = new HungerGames(main, Bukkit.getServer().getWorld("sg_map")).runTaskTimer(main, 5, 2);
                    }
                } else {

                }
            } else if (args.length == 3) {
                if (args[0] == "team") { // team
                    if (args[1] == "create") { // create team
                        ScoredTeam team = new ScoredTeam(args[2], randomChatColor(), main.getScoredPlayer(player));
                        Bukkit.getServer().broadcastMessage(ChatColor.BLUE + player.getDisplayName() + ChatColor.WHITE + " has created the team: " + team.col + team.teamName);
                    }
                }
            }


        }

        return true;
    }

    ChatColor randomChatColor() {
        int color = (int)Math.floor(Math.random() * 11);

        switch (color) {
            default:
                return ChatColor.DARK_RED;
            case 1:
                return ChatColor.RED;
            case 2:
                return ChatColor.GOLD;
            case 3:
                return ChatColor.YELLOW;
            case 4:
                return ChatColor.GREEN;
            case 5:
                return ChatColor.DARK_GREEN;
            case 6:
                return ChatColor.DARK_AQUA;
            case 7:
                return ChatColor.AQUA;
            case 8:
                return ChatColor.BLUE;
            case 9:
                return ChatColor.DARK_BLUE;
            case 10:
                return ChatColor.DARK_PURPLE;
            case 11:
                return ChatColor.LIGHT_PURPLE;

        }
    }

    public void displayHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "======= Minecraft Tournament Help =======");
        sender.sendMessage(ChatColor.WHITE+ "/mct - the main base command");
        sender.sendMessage(ChatColor.WHITE+ "/mct start <game> - start game");
    }

    public void generateChestLoot(CommandSender sender) {

        Bukkit.getServer().broadcastMessage(ChatColor.BLUE+"Generating Chests...");

        List<String> tier1Loot = main.getConfig().getConfigurationSection("HungerGames").getConfigurationSection("Tables").getStringList("Tier1");
        List<String> tier2Loot = main.getConfig().getConfigurationSection("HungerGames").getConfigurationSection("Tables").getStringList("Tier2");
        List<String> tier3Loot = main.getConfig().getConfigurationSection("HungerGames").getConfigurationSection("Tables").getStringList("Tier3");
        List<String> midLoot = main.getConfig().getConfigurationSection("HungerGames").getConfigurationSection("Tables").getStringList("Mid");

        //Bukkit.getServer().broadcastMessage(ChatColor.BLUE+"Loaded Loot Tables:");


        if(sender instanceof Player) {
            Player player = (Player) sender;
            //Bukkit.getServer().broadcastMessage(ChatColor.GRAY+player.getName());

            int t1c = 0;
            int t2c = 0;
            int t3c = 0;

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
                                    chanceSpawn = 0.2;
                                    t1c++;
                                    break;
                                case "Tier 2":
                                    lootTable = tier2Loot;
                                    chanceSpawn = 0.2;
                                    t2c++;
                                    break;
                                case "Tier 3":
                                    lootTable = tier3Loot;
                                    chanceSpawn = 0.11;
                                    t3c++;
                                    break;
                                case "Mid Chest":
                                    lootTable = midLoot;
                                    chanceSpawn = 0.15;
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
        }



    }
}
