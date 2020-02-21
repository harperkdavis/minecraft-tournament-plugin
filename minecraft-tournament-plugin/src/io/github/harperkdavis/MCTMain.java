package io.github.harperkdavis;

import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EnumTitleAction;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayList;
import java.util.List;

public class MCTMain extends JavaPlugin implements Listener {

    List<ScoredPlayer> playerList = new ArrayList<>();
    List<ScoredTeam> teamList = new ArrayList<>();

    List<BukkitRunnable> tasksRunning = new ArrayList<>();

    Boolean allowBlockChanging = false;

    private List<String> deathMessages;

    public void onEnable() {
        loadConfig();
        this.getCommand("mct").setExecutor(new CommandHandler(this));
        Bukkit.getServer().getPluginManager().registerEvents(this,this);

        deathMessages = getConfig().getStringList("DeathMessages");
        new LobbyScoreboard(this).runTaskTimer(this, 0, 2);

        for (Player p: Bukkit.getOnlinePlayers()) {
            ScoredPlayer player = new ScoredPlayer(p, (p.isOp()));

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

        Boolean isAdmin = event.getPlayer().isOp();

        if (isAdmin) {
            Bukkit.getServer().broadcastMessage(ChatColor.DARK_GRAY + "Watch out, an Admin is approaching!");
            event.getPlayer().setWalkSpeed(0.2f);
        }


        ScoredPlayer player = new ScoredPlayer(event.getPlayer(), isAdmin);

        if(!playerList.contains(player))
            playerList.add(player);
    }

    @EventHandler
    public void blockPlacing(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp() && !allowBlockChanging) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockBreaking(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp() && !allowBlockChanging) {
            event.setCancelled(true);
        }
    }

    void broadcastTitle(String text, ChatColor col, EnumTitleAction e, int i, int i1, int i2) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            IChatBaseComponent chatTitle = ChatSerializer.a("{\"text\": \"" + text + "\",color:" + col.name().toLowerCase() + "}");

            PacketPlayOutTitle title = new PacketPlayOutTitle(e, chatTitle);
            PacketPlayOutTitle length = new PacketPlayOutTitle(i, i1, i2);



            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(title);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(length);

        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {

        Player killed = e.getEntity();
        Player killer = e.getEntity().getKiller();

        killer.getWorld().strikeLightningEffect(killer.getLocation());
        String killerName = killer.getName();

        for (BukkitRunnable r : tasksRunning) {
            if (r instanceof HungerGames) {
                HungerGames hg = (HungerGames) r;
                hg.registerKill(killer);

            }
            if (r instanceof Manhunt) {
                Manhunt mh = (Manhunt) r;
                mh.registerKill(killer, killed);
            }
        }


        String message = deathMessages.get((int)Math.floor(Math.random() * deathMessages.size()));

        e.setDeathMessage(ChatColor.RED + killed.getName() + " " + message + " " + killerName + ChatColor.GRAY + " (" + ChatColor.YELLOW + killer.getHealth() + ChatColor.RED + "❤" + ChatColor.GRAY + ")");

        killed.setGameMode(GameMode.SPECTATOR);
        getScoredPlayer(killed).broadcastTitle("You Died!", ChatColor.RED, EnumTitleAction.SUBTITLE, 5, 40, 5);
        getScoredPlayer(killed).broadcastTitle("", ChatColor.RED, EnumTitleAction.TITLE, 5, 40, 5);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {

        if(e.getDamager() instanceof Arrow) {
            Player hit = (Player) e.getEntity();
            Player hitter = (Player) ((Arrow)e.getDamager()).getShooter();

            hitter.sendMessage(ChatColor.GOLD + hit.getName() + ChatColor.WHITE + " is on " + ChatColor.RED + Math.round(hit.getHealth()*10)/10);

        }

    }

    ScoredPlayer getScoredPlayer(Player d) {
        // Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Searching through players");
        for (ScoredPlayer p : playerList) {
            // Bukkit.getServer().broadcastMessage(ChatColor.GRAY + "P: " + p);
            if (p.player == d) {
                return p;
            }
        }
        // No Results, so add the player to the list!
        ScoredPlayer p = new ScoredPlayer(d, d.isOp());
        playerList.add(p);
        return p;
    }

    List<ScoredPlayer> getAllCompetitors() {
        List<ScoredPlayer> sp = new ArrayList<>();
        for (ScoredPlayer p : playerList) {
            if (!p.admin)
                sp.add(p);
        }

        return sp;
    }

}

