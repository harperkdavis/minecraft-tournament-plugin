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

    public List<ScoredPlayer> playerList;
    public List<ScoredTeam> teamList;

    public void onEnable() {
        loadConfig();
        this.getCommand("mct").setExecutor(new CommandHandler(this));
        Bukkit.getServer().getPluginManager().registerEvents(this,this);

        for (Player p: Bukkit.getOnlinePlayers()) {
            ScoredPlayer player = new ScoredPlayer(p, (p.getDisplayName().equals("not_fyyre")));

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

        displayName = event.getPlayer().getDisplayName();
        Boolean isAdmin = displayName == "not_fyyre" | displayName == "DatMrMe";

        Bukkit.getServer().broadcastMessage(ChatColor.DARK_GRAY+ "Watch out, an Admin is approaching!");

        ScoredPlayer player = new ScoredPlayer(event.getPlayer(), isAdmin);

        if(!playerList.contains(player))
            playerList.add(player);
    }

    public ScoredPlayer getScoredPlayer(Player d) {
        Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Searching through players");
        for (ScoredPlayer p : playerList) {
            Bukkit.getServer().broadcastMessage(ChatColor.GRAY + "P: " + p);
            if (p.player == d) {
                return p;
            }
        }
        return null;
    }

}

