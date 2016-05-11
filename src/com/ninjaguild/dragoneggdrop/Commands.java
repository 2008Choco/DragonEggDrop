package com.ninjaguild.dragoneggdrop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commands implements CommandExecutor {
	
	private DragonEggDrop plugin = null;
	
	public Commands(DragonEggDrop plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dragoneggdrop")) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.GOLD + "------------------");
				sender.sendMessage(ChatColor.GOLD + "-- DragonEggDrop --");
				sender.sendMessage(ChatColor.GOLD + "------------------");
				sender.sendMessage(ChatColor.GOLD + "Author: " + plugin.getDescriptionFile().getAuthors().get(0));
				sender.sendMessage(ChatColor.GOLD + "Version: " + plugin.getDescriptionFile().getVersion());
				sender.sendMessage(ChatColor.YELLOW + "/dragoneggdrop help");
				sender.sendMessage(ChatColor.GOLD + "------------------");

				return false;
			}
			else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("help")) {
					sender.sendMessage(ChatColor.GOLD + "-----------------------");
					sender.sendMessage(ChatColor.GOLD + "-- DragonEggDrop HELP --");
					sender.sendMessage(ChatColor.GOLD + "-----------------------");
					sender.sendMessage(ChatColor.GOLD + "Alias: ded");
					sender.sendMessage(ChatColor.GOLD + "/dragoneggdrop help");
					sender.sendMessage(ChatColor.GOLD + "/dragoneggdrop reload");
					sender.sendMessage(ChatColor.GOLD + "/dragoneggdrop addloot");
					sender.sendMessage(ChatColor.GOLD + "-----------------------");
				}
				else if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("dragoneggdrop.reload")) {
						plugin.reloadConfig();
						sender.sendMessage(plugin.getChatPrefix() + ChatColor.GREEN + "Reload Complete");
					}
					else {
						sender.sendMessage(plugin.getChatPrefix() + ChatColor.RED + "Permission Denied!");
					}
					return true;
				}
//				else if (args[0].equalsIgnoreCase("respawn")) {
//					//
//				}
			}
			else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("addloot")) {
					if (!(sender instanceof Player)) {
						sender.sendMessage(plugin.getChatPrefix() + ChatColor.RED + "This command can only be executed by a player!");
						return true;
					}

					Player player = (Player)sender;
					
					try {
						double weight = Double.parseDouble(args[1]);
						ItemStack handItem = player.getInventory().getItemInMainHand();
						if (handItem != null && handItem.getType() != Material.AIR) {
							boolean result = plugin.getLootManager().addItem(weight, handItem);
							if (result) {
								player.sendMessage(plugin.getChatPrefix() + ChatColor.GREEN + "Successfully added loot item!");
							}
							else {
								player.sendMessage(plugin.getChatPrefix() + ChatColor.RED + "Failed to add loot item! Already exist?");
							}
						}
						else {
							player.sendMessage(plugin.getChatPrefix() + ChatColor.YELLOW + "Hold the item you wish to add in your main hand.");
						}
					}
					catch (NumberFormatException ex) {
						sender.sendMessage(plugin.getChatPrefix() + ChatColor.RED + "Invalid value for weight!");
					}
				}
			}
		}

		return true;
	}
	
}
