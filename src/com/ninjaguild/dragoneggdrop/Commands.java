package com.ninjaguild.dragoneggdrop;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor {
	
	private DragonEggDrop plugin = null;
	
	public Commands(DragonEggDrop plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dragoneggdrop")) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.GOLD + "-------------------");
				sender.sendMessage(ChatColor.GOLD + "    DragonEggDrop");
				sender.sendMessage(ChatColor.GOLD + "-------------------");
				sender.sendMessage(ChatColor.GOLD + "Author: " + plugin.getDescriptionFile().getAuthors().get(0));
				sender.sendMessage(ChatColor.GOLD + "Version: " + plugin.getDescriptionFile().getVersion());
				sender.sendMessage(ChatColor.GOLD + "-------------------");

				return false;
			}
			else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("help")) {
					//
				}
				else if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("dragoneggdrop.reload")) {
						plugin.reloadConfig();
						sender.sendMessage(ChatColor.GREEN + "Reload Complete");
					}
					else {
						sender.sendMessage(ChatColor.RED + "Permission Denied!");
					}
					return true;
				}
				else if (args[0].equalsIgnoreCase("respawn")) {
					//
				}
				else if (args[0].equalsIgnoreCase("removeloot")) {
					
				}
			}
			else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("addloot")) {
					try {
						int weight = Integer.parseInt(args[1]);
					}
					catch (NumberFormatException ex) {
						sender.sendMessage(ChatColor.RED + "Invalid value for weight!");
					}
				}
			}
		}

		return false;
	}
	
}
