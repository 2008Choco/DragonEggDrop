package wtf.choco.dragoneggdrop.utils;

import com.google.common.base.Preconditions;

import java.util.List;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import wtf.choco.dragoneggdrop.placeholder.DragonEggDropPlaceholders;

public final class ActionBarUtil {

    private ActionBarUtil() { }

    public static void sendActionBar(@NotNull String message, @NotNull Player player, boolean injectPlaceholders) {
        Preconditions.checkArgument(message != null, "Message must not be null");
        Preconditions.checkArgument(player != null, "Player must not be null");

        if (injectPlaceholders) {
            message = DragonEggDropPlaceholders.inject(player, message);
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    public static void broadcastActionBar(@NotNull String message, @NotNull World world, boolean injectPlaceholders) {
        Preconditions.checkArgument(world != null, "World must not be null");
        world.getPlayers().forEach(p -> ActionBarUtil.sendActionBar(message, p, injectPlaceholders));
    }

    public static void broadcastActionBar(@NotNull String message, @NotNull Location location, int radiusSquared, boolean injectPlaceholders) {
        if (location == null || location.getWorld() == null || radiusSquared < 0) {
            return;
        }

        World world = location.getWorld();
        assert world != null; // Impossible

        List<Player> players = world.getPlayers();
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
