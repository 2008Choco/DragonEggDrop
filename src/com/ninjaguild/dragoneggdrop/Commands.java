/*
    DragonEggDrop
    Copyright (C) 2016  NinjaStix
    ninjastix84@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ninjaguild.dragoneggdrop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commands implements CommandExecutor {
	
	private final DragonEggDrop plugin;
	
	public Commands(final DragonEggDrop plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("dragoneggdrop")) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.GOLD + "-----------------------");
				sender.sendMessage(ChatColor.GOLD + "-- DRAGONEGGDROP INFO --");
				sender.sendMessage(ChatColor.GOLD + "-----------------------");
				sender.sendMessage(ChatColor.GOLD + "Author: " + plugin.getDescriptionFile().getAuthors().get(0));
				sender.sendMessage(ChatColor.GOLD + "Version: " + plugin.getDescriptionFile().getVersion());
				sender.sendMessage(ChatColor.YELLOW + "/dragoneggdrop help");
				sender.sendMessage(ChatColor.GOLD + "-----------------------");
				
				return true;
			}
			else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("help")) {
					if (sender.hasPermission("dragoneggdrop.help")) {
						sender.sendMessage(ChatColor.GOLD + "-----------------------");
						sender.sendMessage(ChatColor.GOLD + "-- DRAGONEGGDROP HELP --");
						sender.sendMessage(ChatColor.GOLD + "-----------------------");
						sender.sendMessage(ChatColor.YELLOW + "Alias: ded");
						sender.sendMessage(ChatColor.GOLD + "/dragoneggdrop reload");
						sender.sendMessage(ChatColor.GOLD + "/dragoneggdrop addloot <weight>");
						sender.sendMessage(ChatColor.GOLD + "-----------------------");
					}
					else {
						sender.sendMessage(plugin.getChatPrefix() + ChatColor.RED + "Permission Denied!");
					}
				}
				else if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("dragoneggdrop.reload")) {
						plugin.reloadConfig();
						sender.sendMessage(plugin.getChatPrefix() + ChatColor.GREEN + "Reload Complete");
					}
					else {
						sender.sendMessage(plugin.getChatPrefix() + ChatColor.RED + "Permission Denied!");
					}
				}
//				else if (args[0].equalsIgnoreCase("respawn")) {
//					//
//				}
				return true;
			}
			else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("addloot")) {
					if (!(sender instanceof Player)) {
						sender.sendMessage(plugin.getChatPrefix() + ChatColor.RED +
								"This command can only be executed by a player!");
						return true;
					}
					if (!sender.hasPermission("dragoneggdrop.addloot")) {
						sender.sendMessage(plugin.getChatPrefix() + ChatColor.RED + "Permission Denied!");
						return true;
					}

					Player player = (Player)sender;
					
					try {
						double weight = Double.parseDouble(args[1]);
						ItemStack handItem = player.getInventory().getItemInMainHand();
						if (handItem != null && handItem.getType() != Material.AIR) {
							boolean result = plugin.getDEDManager().getLootManager().addItem(weight, handItem);
							if (result) {
								player.sendMessage(plugin.getChatPrefix() + ChatColor.GREEN +
                                        "Successfully added loot item!");
							}
							else {
								player.sendMessage(plugin.getChatPrefix() + ChatColor.RED +
                                        "Failed to add loot item! Already exist?");
							}
							return true;
						}
						else {
							player.sendMessage(plugin.getChatPrefix() + ChatColor.YELLOW +
                                    "Hold the item you wish to add in your main hand.");
							return true;
						}
					}
					catch (NumberFormatException ex) {
						sender.sendMessage(plugin.getChatPrefix() + ChatColor.RED + "Invalid value for weight!");
						return false;
					}
				}
			}
		}

		return false;
	}
	
}
