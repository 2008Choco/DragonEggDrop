package com.ninjaguild.dragoneggdrop;

import java.util.logging.Level;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

public class DragonEggDrop extends JavaPlugin implements Listener {

	public void onEnable() {
		saveDefaultConfig();

		try {
			Particle.valueOf(getConfig().getString("particle-type", "FLAME").toUpperCase());
		} catch (IllegalArgumentException ex) {
			getLogger().log(Level.WARNING, "INVALID PARTICLE TYPE SPECIFIED! DISABLING...");
			getServer().getPluginManager().disablePlugin(this);
			getLogger().log(Level.INFO, "PLUGIN DISABLED");
			return;
		}
		
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("dragoneggdrop").setExecutor(this);
	}
	
	public void onDisable() {
		//
	}

	@EventHandler
	private void onDragonDeath(EntityDeathEvent e) {
		if (e.getEntityType() == EntityType.ENDER_DRAGON) {
			World world = e.getEntity().getWorld();
			getServer().getScheduler().runTask(this, new DragonDeathRunnable(this, world));
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dragoneggdrop")) {
			if (args.length == 0) {
        		PluginDescriptionFile pdf = getDescription();
        		
    	        sender.sendMessage(ChatColor.GOLD + "-------------------");
    	        sender.sendMessage(ChatColor.GOLD + "--  DragonEggDrop  --");
    	        sender.sendMessage(ChatColor.GOLD + "-------------------");
    	        sender.sendMessage(ChatColor.GOLD + "Author: " + pdf.getAuthors().get(0));
    	        sender.sendMessage(ChatColor.GOLD + "Version: " + pdf.getVersion());
    	        sender.sendMessage(ChatColor.GOLD + "-------------------");
        		
        		return false;
			}
			else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("dragoneggdrop.reload")) {
						reloadConfig();
						sender.sendMessage(ChatColor.GREEN + "Reload Complete");
					}
					else {
						sender.sendMessage(ChatColor.RED + "Permission Denied!");
					}
					return true;
				}
			}
		}
		
		return false;
	}
}
