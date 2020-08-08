package com.ninjaguild.dragoneggdrop.listeners;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.utils.math.MathUtils;
import com.ninjaguild.dragoneggdrop.world.EndWorldWrapper;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;

public final class PortalClickListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    private void onClickEndPortalFrame(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        Block clickedBlock = event.getClickedBlock();

        PlayerInventory inventory = player.getInventory();
        if (event.getMaterial() != Material.BEDROCK || world.getEnvironment() != World.Environment.THE_END || event.getHand() != EquipmentSlot.HAND
                || (inventory.getItemInMainHand().getType() != Material.AIR || inventory.getItemInOffHand().getType() != Material.AIR)) {
            return;
        }

        DragonBattle dragonBattle = world.getEnderDragonBattle();
        Location portalLocation = dragonBattle.getEndPortalLocation().add(0, 4, 0);
        if (clickedBlock.getLocation().distanceSquared(portalLocation) > 36) { // 6 blocks
            return;
        }

        EndWorldWrapper endWorld = EndWorldWrapper.of(world);
        int secondsRemaining = endWorld.getTimeUntilRespawn();
        if (secondsRemaining <= 0) {
            return;
        }

        DragonEggDrop.sendMessage(player, "Dragon will respawn in " + ChatColor.YELLOW + MathUtils.getFormattedTime(secondsRemaining));
    }

}
