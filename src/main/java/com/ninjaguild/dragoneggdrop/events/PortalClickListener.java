package com.ninjaguild.dragoneggdrop.events;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;
import com.ninjaguild.dragoneggdrop.nms.DragonBattle;
import com.ninjaguild.dragoneggdrop.nms.NMSUtils;
import com.ninjaguild.dragoneggdrop.utils.math.MathUtils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;

public final class PortalClickListener implements Listener {

    private final DragonEggDrop plugin;

    public PortalClickListener(DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClickEndPortalFrame(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        Block clickedBlock = event.getClickedBlock();

        PlayerInventory inventory = player.getInventory();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || world.getEnvironment() != Environment.THE_END
                || clickedBlock.getType() != Material.BEDROCK || event.getHand() != EquipmentSlot.HAND
                || (inventory.getItemInMainHand().getType() != Material.AIR || inventory.getItemInOffHand().getType() != Material.AIR)) {
            return;
        }

        DragonBattle dragonBattle = NMSUtils.getEnderDragonBattleFromWorld(world);
        Location portalLocation = dragonBattle.getEndPortalLocation();
        if (clickedBlock.getLocation().distanceSquared(portalLocation) > 25) { // 5 blocks
            return;
        }

        EndWorldWrapper endWorld = plugin.getDEDManager().getWorldWrapper(world);
        int secondsRemaining = endWorld.getTimeUntilRespawn();
        if (secondsRemaining <= 0) {
            return;
        }

        this.plugin.sendMessage(player, "Dragon will respawn in " + ChatColor.YELLOW + MathUtils.getFormattedTime(secondsRemaining));
    }

}
