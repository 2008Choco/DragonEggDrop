package com.ninjaguild.dragoneggdrop.events;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;
import com.ninjaguild.dragoneggdrop.versions.DragonBattle;
import com.ninjaguild.dragoneggdrop.versions.NMSAbstract;

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

public class PortalClickListener implements Listener {
	
	private final DragonEggDrop plugin;
	
	public PortalClickListener(DragonEggDrop plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onClickEndPortalFrame(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		World world = player.getWorld();
		Block clickedBlock = event.getClickedBlock();
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || world.getEnvironment() != Environment.THE_END 
				|| clickedBlock.getType() != Material.BEDROCK || event.getHand() != EquipmentSlot.HAND
				|| (player.getInventory().getItemInMainHand() != null || player.getInventory().getItemInOffHand() != null)) return;
		
		NMSAbstract nmsAbstract = plugin.getNMSAbstract();
		DragonBattle dragonBattle = nmsAbstract.getEnderDragonBattleFromWorld(world);
		Location portalLocation = dragonBattle.getEndPortalLocation();
		
		if (event.getClickedBlock().getLocation().distanceSquared(portalLocation) > 36) return; // 5 blocks
		
		EndWorldWrapper endWorld = plugin.getDEDManager().getWorldWrapper(world);
		int secondsRemaining = endWorld.getTimeUntilRespawn();
		if (secondsRemaining <= 0) return;
		
		plugin.sendMessage(player, "Dragon will respawn in " + ChatColor.YELLOW + secondsRemaining);
	}
}