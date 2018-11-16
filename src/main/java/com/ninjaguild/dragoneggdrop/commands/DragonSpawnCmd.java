package com.ninjaguild.dragoneggdrop.commands;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.management.DEDManager;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

public class DragonSpawnCmd implements CommandExecutor {
	private final DragonEggDrop plugin;

	public DragonSpawnCmd(final DragonEggDrop plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!plugin.getConfig().getBoolean("respawn-on-command", false)) {
			this.plugin.sendMessage(sender, ChatColor.RED + "This feature is disabled!");
			return true;
		}

		if (!sender.hasPermission("dragoneggdrop.spawn")) {
			this.plugin.sendMessage(sender, ChatColor.RED + "Permission denied!");
			return true;
		}

		if (!(sender instanceof Player)) {
			this.plugin.sendMessage(sender, ChatColor.RED + "You must be a player to spawn a dragon!");
			return true;
		}

		final Player player = (Player) sender;

		World world = player.getWorld();

		if (world.getEnvironment() != World.Environment.THE_END) {
			this.plugin.sendMessage(sender, ChatColor.RED + "You must be a in an end world to spawn a dragon!");
			return true;
		}

		EndWorldWrapper worldWrapper = plugin.getDEDManager().getWorldWrapper(world);

		// Reset end crystal states just in case something went awry
		if (!worldWrapper.isRespawnInProgress()) {
			world.getEntitiesByClass(EnderCrystal.class).forEach(e -> {
				e.setInvulnerable(false);
				e.setBeamTarget(null);
			});
		}

		// Dragon respawn logic
		if (plugin.getDEDManager().getWorldWrapper(world).isRespawnInProgress()
				|| !world.getEntitiesByClass(EnderDragon.class).isEmpty())
			return true;

		plugin.getDEDManager().getWorldWrapper(world).startRespawn(DEDManager.RespawnType.COMMAND);
		return true;
	}
}
