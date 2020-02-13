package io.github.harperkdavis;

import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EnumTitleAction;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import javax.xml.bind.Marshaller;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MCTMain extends JavaPlugin implements Listener {

    public List<ScoredPlayer> playerList = new ArrayList<ScoredPlayer>();
    public List<ScoredTeam> teamList = new ArrayList<ScoredTeam>();

    public List<BukkitRunnable> tasksRunning = new ArrayList<BukkitRunnable>();

    Boolean allowBlockPlacing = false;

    private List<String> deathMessages;

    public void onEnable() {
        loadConfig();
        this.getCommand("mct").setExecutor(new CommandHandler(this));
        Bukkit.getServer().getPluginManager().registerEvents(this,this);

        // Bukkit.getServer().getConsoleSender().sendMessage("Players in list:"+playerList.size());

        deathMessages = getConfig().getStringList("DeathMessages");
        BukkitTask task = new LobbyScoreboard(this).runTaskTimer(this, 0, 2);
        //tasksRunning.add(task);

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

        Boolean isAdmin = event.getPlayer().isOp(); //event.getPlayer().getName() == "not_fyyre";

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

        if (!player.isOp()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockBreaking(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp()) {
            event.setCancelled(true);
        }
    }

    public void broadcastTitle(String text, ChatColor col, EnumTitleAction e, int i, int i1, int i2) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            IChatBaseComponent chatTitle = ChatSerializer.a("{\"text\": \"" + text + "\",color:" + col.name().toLowerCase() + "}");

            PacketPlayOutTitle title = new PacketPlayOutTitle(e, chatTitle);
            PacketPlayOutTitle length = new PacketPlayOutTitle(i, i1, i2);



            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(title);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(length);

        }
    }

    @EventHandler
    public void onKill(PlayerDeathEvent e) {

        Player killed = (Player)e.getEntity();
        Player killer = (Player)e.getEntity().getKiller();

        killer.getWorld().strikeLightningEffect(killer.getLocation());

        String message = deathMessages.get((int)Math.floor(Math.random() * deathMessages.size()));

        e.setDeathMessage(ChatColor.RED + killed.getName() + " " + message + " " + killer.getName() + ChatColor.GRAY + " (" + ChatColor.YELLOW + killer.getHealth() + ChatColor.RED + "❤" + ChatColor.GRAY + ")");


        for (BukkitRunnable r : tasksRunning) {
            if (r instanceof HungerGames) {
                HungerGames hg = (HungerGames)r;
                hg.registerKill(killer, killed);

            }
        }

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

    public ScoredPlayer getScoredPlayer(Player d) {
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

    public List<ScoredPlayer> getAllCompetitors() {
        List<ScoredPlayer> sp = new ArrayList<ScoredPlayer>();
        for (ScoredPlayer p : playerList) {
            if (!p.admin)
                sp.add(p);
        }

        return sp;
    }

    public void ChangeWorldEvent(PlayerChangedWorldEvent event) {

        Player player = (Player) event.getPlayer();
        World world = player.getWorld();

        if ((world.getName().equals("world"))){
            for (BukkitRunnable r : tasksRunning) {
                if (r instanceof HungerGames) {
                    LobbyScoreboard ls = (LobbyScoreboard) r;
                    ls.setScoreBoard(player);
                }
            }
        }

    }

}

