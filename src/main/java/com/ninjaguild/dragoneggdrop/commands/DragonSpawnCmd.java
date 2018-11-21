package com.ninjaguild.dragoneggdrop.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.management.DEDManager;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class DragonSpawnCmd implements CommandExecutor, TabCompleter {

	private final DragonEggDrop plugin;

	public DragonSpawnCmd(DragonEggDrop plugin) {
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

		World world = (sender instanceof Player) ? ((Player) sender).getWorld() : null;
		if (args.length >= 1) {
			world = Bukkit.getWorld(args[0]);
		} else if (!(sender instanceof Player)) {
			this.plugin.sendMessage(sender, "You must specify a world when executing this command from the console");
			return true;
		}

		if (world == null) {
			this.plugin.sendMessage(sender, "A world with the name " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + " does not exist");
			return true;
		}

		if (world.getEnvironment() != World.Environment.THE_END) {
			this.plugin.sendMessage(sender, ChatColor.RED + "Dragons can only be spawned in the end!");
			return true;
		}

		DragonTemplate template = plugin.getDEDManager().getRandomTemplate();
		if (args.length >= 2) {
			DragonTemplate templateArgument = plugin.getDEDManager().getTemplate(args[1]);
			if (templateArgument == null) {
				this.plugin.sendMessage(sender, "A dragon with the template " + args[1] + " could not be found... did you spell it correctly?");
				return true;
			}

			template = templateArgument;
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
		if (worldWrapper.isRespawnInProgress() || !world.getEntitiesByClass(EnderDragon.class).isEmpty()) {
			this.plugin.sendMessage(sender, ChatColor.RED + "A respawn could not be forced because either a respawn is already in progress or a dragon exists already in the world");
			return true;
		}

		worldWrapper.startRespawn(DEDManager.RespawnType.COMMAND, template);
		this.plugin.sendMessage(sender, "Respawning a dragon in world " + ChatColor.GREEN + world.getName() + ChatColor.GRAY + " with template ID " + ChatColor.YELLOW + template.getIdentifier());
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			return StringUtil.copyPartialMatches(args[0], Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == Environment.THE_END)
					.map(World::getName).collect(Collectors.toList()), new ArrayList<>());
		} else if (args.length == 2) {
			return StringUtil.copyPartialMatches(args[0], plugin.getDEDManager().getDragonTemplates().stream()
					.map(DragonTemplate::getIdentifier).collect(Collectors.toList()), new ArrayList<>());
		}

		return null;
	}

}