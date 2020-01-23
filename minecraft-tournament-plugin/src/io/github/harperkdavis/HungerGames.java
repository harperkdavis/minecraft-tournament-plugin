package io.github.harperkdavis;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;



public class HungerGames extends BukkitRunnable {

    private final MCTMain main;

    private int ticks = 0;

    public HungerGames(MCTMain main, World world) {
        this.main = main;
    }

    @Override
    public void run() {
        // What you want to schedule goes here
        ticks++;

    }

    public void updateScoreboard() {

    }



}