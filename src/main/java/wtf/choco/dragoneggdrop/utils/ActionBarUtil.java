package wtf.choco.dragoneggdrop.utils;

import com.google.common.base.Preconditions;

import java.util.List;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import wtf.choco.dragoneggdrop.placeholder.DragonEggDropPlaceholders;

public final class ActionBarUtil {

    private ActionBarUtil() { }

    public static void sendActionBar(String message, Player player, boolean injectPlaceholders) {
        Preconditions.checkArgument(message != null, "Message must not be null");
        Preconditions.checkArgument(player != null, "Player must not be null");

        if (injectPlaceholders) {
            message = DragonEggDropPlaceholders.inject(player, message);
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    public static void broadcastActionBar(String message, World world, boolean injectPlaceholders) {
        Preconditions.checkArgument(world != null, "World must not be null");
        world.getPlayers().forEach(p -> ActionBarUtil.sendActionBar(message, p, injectPlaceholders));
    }

    public static void broadcastActionBar(String message, Location location, int radiusSquared, boolean injectPlaceholders) {
        if (location == null || location.getWorld() == null || radiusSquared < 0) {
            return;
        }

        List<Player> players = location.getWorld().getPlayers();
        if (players.isEmpty()) {
            return;
        }

        for (Player player : players) {
            if (player.getLocation().distanceSquared(location) > radiusSquared) {
                continue;
            }

            ActionBarUtil.sendActionBar(message, player, injectPlaceholders);
        }

    }

}
