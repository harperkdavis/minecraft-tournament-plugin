package io.github.harperkdavis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import javax.xml.bind.Marshaller;
import java.util.List;

public class MCTMain extends JavaPlugin implements Listener {

    public List<SPlayer> playerList;
    public List<STeam> teamList;

    public void onEnable() {
        loadConfig();
        this.getCommand("mct").setExecutor(new CommandHandler(this));
        Bukkit.getServer().getPluginManager().registerEvents(this,this);

        for (Player p: Bukkit.getOnlinePlayers()) {
            SPlayer player = new SPlayer(p, (p.getDisplayName().equals("not_fyyre")));

            if(!playerList.contains(player))
                playerList.add(player);
        }




    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public void onDisable() {
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(ChatColor.RED + event.getPlayer().getDisplayName() + ChatColor.GRAY + " has joined the Tournament");

        Boolean isAdmin = event.getPlayer().getDisplayName() == "not_fyyre";

        Bukkit.getServer().broadcastMessage(ChatColor.DARK_GRAY+ "Watch out, an Admin is approaching!");

        SPlayer player = new SPlayer(event.getPlayer(), isAdmin);

        if(!playerList.contains(player))
            playerList.add(player);
    }

    public SPlayer getSPlayer(Player d) {
        Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Searching through players");
        for (SPlayer p : playerList) {
            Bukkit.getServer().broadcastMessage(ChatColor.GRAY + "P: " + p);
            if (p.player == d) {
                return p;
            }
        }
        return null;
    }

}
