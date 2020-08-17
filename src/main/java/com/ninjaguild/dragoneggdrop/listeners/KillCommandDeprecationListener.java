package com.ninjaguild.dragoneggdrop.listeners;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class KillCommandDeprecationListener implements Listener {

    private final Set<UUID> awaitingConfirmation = new HashSet<>();

    private final DragonEggDrop plugin;

    public KillCommandDeprecationListener(DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onDragonKillByCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().trim();

        // This should support both "/kill" and "/execute in world run kill"
        if (!command.startsWith("/kill @e") && !command.contains(" kill @e")) {
            return;
        }

        if (!command.endsWith("@e") && !command.contains(EntityType.ENDER_DRAGON.getKey().getKey())) {
            return;
        }

        Player player = event.getPlayer();
        World world = player.getWorld();
        if (world.getEnvironment() != Environment.THE_END || world.getEntitiesByClass(EnderDragon.class).isEmpty()) {
            return;
        }

        UUID playerUUID = player.getUniqueId();
        if (awaitingConfirmation.add(playerUUID)) {
            Location location = player.getLocation();
            player.playSound(location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.2F);
            player.playSound(location, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.2F);

            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "WARNING! " + ChatColor.GRAY + "This command will kill the dragon in this world!");
            player.sendMessage(ChatColor.GRAY + "This is " + ChatColor.RED + "NOT " + ChatColor.GRAY + "an officially supported way of killing the dragon and may break " + ChatColor.AQUA + "DragonEggDrop" + ChatColor.GRAY + "!");
            player.sendMessage(ChatColor.GRAY + "To " + ChatColor.GREEN + "confirm " + ChatColor.GRAY + "the execution of this command, please run " + ChatColor.YELLOW + command + ChatColor.GRAY + " again.");

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (awaitingConfirmation.remove(playerUUID)) {
                    player.sendMessage(ChatColor.GRAY + "Execution of command " + ChatColor.YELLOW + command + ChatColor.GRAY + " has been cancelled.");
                }
            }, 300L); // 15 seconds

            event.setCancelled(true);
            return;
        }

        this.awaitingConfirmation.remove(playerUUID);
    }

}
