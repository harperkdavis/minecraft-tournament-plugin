package io.github.harperkdavis;

import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EnumTitleAction;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ScoredPlayer {

    public Player player;
    public ScoredTeam team;
    public int score;
    public Boolean admin;

    ScoredPlayer (Player player, Boolean admin) {
        this.player = player;
        this.admin = admin;

    }

    public void addScore(int score, String reason) {
        this.score += score;
        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + "+" + score + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY + " points for " + ChatColor.GREEN + reason);
    }

    public void broadcastTitle(String text, ChatColor col, EnumTitleAction e, int i, int i1, int i2) {
        IChatBaseComponent chatTitle = ChatSerializer.a("{\"text\": \"" + text + "\",color:" + col.name().toLowerCase() + "}");

        PacketPlayOutTitle title = new PacketPlayOutTitle(e, chatTitle);
        PacketPlayOutTitle length = new PacketPlayOutTitle(i, i1, i2);


        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(length);
    }

}
