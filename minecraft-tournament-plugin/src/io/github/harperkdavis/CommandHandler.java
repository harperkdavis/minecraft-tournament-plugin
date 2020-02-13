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

            // player.sendMessage(ChatColor.BLUE + player.getDisplayName() + ChatColor.WHITE + " has run the command with args: " + ChatColor.BOLD + String.valueOf(args.length));

            if (args.length == 0) {
                displayHelp(sender);
            } else if (args.length == 1) {
                if (args[0].equals("team")) { // team add person
                    if (main.getScoredPlayer(player).team == null) { // no team
                        player.sendMessage(ChatColor.RED + "You are not on a team. Create a team with /mct team create <name>");
                    } else { // on team (make info)
                        main.getScoredPlayer(player).team.sendInfo(player);
                    }
                }
            } else if (args.length == 2) {
                if (args[0].equals("start")) {
                    if(args[1].equals("sg")) { //start survival games
                        player.sendMessage(ChatColor.WHITE + "Starting survival games!");
                        BukkitTask task = new HungerGames(main, player).runTaskTimer(main, 5, 2);
                    }
                } else if (args[0].equals("team")) {
                    if (args[1].equals("leave")) {
                        if (main.getScoredPlayer(player).team == null) {
                            player.sendMessage(ChatColor.RED + "You are not on a team. Create a team with /mct team create <name>");
                        } else {
                            ScoredTeam t = main.getScoredPlayer(player).team;
                            main.teamList.remove(t);
                            player.sendMessage(ChatColor.WHITE + "You have disbanded the team");
                            if(t.player1 != null) {
                                t.player1.team = null;
                                t.player1.player.setDisplayName(t.player1.player.getName());
                            }
                            if(t.player2 != null) {
                                t.player2.player.sendMessage(ChatColor.BLUE + player.getName() + ChatColor.WHITE + " has disbanded the team");
                                t.player2.team = null;
                                t.player2.player.setDisplayName(t.player2.player.getName());
                            }
                        }
                    }
                }
            } else if (args.length == 3) {
                if (args[0].equals("team")) { // team
                    if (args[1].equals("create")) { // create team
                        if (main.getScoredPlayer(player).team == null) {
                            if (args[2].length() <= 16) {
                                ScoredTeam team = new ScoredTeam(args[2], randomChatColor(), main.getScoredPlayer(player));
                                main.teamList.add(team);
                                Bukkit.getServer().broadcastMessage(ChatColor.BLUE + player.getName() + ChatColor.WHITE + " has created the team: " + team.col + team.teamName);
                            } else {
                                player.sendMessage(ChatColor.RED + "That team name is too long! (> 16 characters)");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You are already on a team! Leave with /mct team leave");
                        }
                    } else if (args[1].equals("add")) {
                        if (main.getScoredPlayer(player).team != null) {
                            Player p = Bukkit.getServer().getPlayer(args[2]);
                            if (p != null) {
                                main.getScoredPlayer(player).team.setPlayer2(main.getScoredPlayer(p));
                                player.sendMessage(ChatColor.BLUE + p.getName() + ChatColor.WHITE + " has been added to the team");
                                p.sendMessage(ChatColor.GOLD + "You have been added to " + main.getScoredPlayer(player).team.col + main.getScoredPlayer(player).team.teamName);
                            } else {
                                player.sendMessage(ChatColor.RED + "That player was not found!");
                            }
                        }  else {
                            player.sendMessage(ChatColor.RED + "You are not on a team!");
                        }
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


}
