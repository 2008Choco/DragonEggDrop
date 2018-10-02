package com.ninjaguild.dragoneggdrop.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.management.DEDManager;

import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DragonTemplateCmd implements CommandExecutor, TabCompleter {
	
	private final DragonEggDrop plugin;
	private final DEDManager manager;
	
	public DragonTemplateCmd(DragonEggDrop plugin) {
		this.plugin = plugin;
		this.manager = plugin.getDEDManager();
	}
	
	// /template <list|"template"> <(view/info)|edit>
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			this.plugin.sendMessage(sender, "Please specify the name of a template, or \"list\" to list all templates");
			return true;
		}
		
		Collection<DragonTemplate> templates = manager.getDragonTemplates();
		
		// List all existing templates
		if (args[0].equalsIgnoreCase("list")) {
			if (!sender.hasPermission("dragoneggdrop.template.list")) {
				this.plugin.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
				return true;
			}
			
			String[] templateNames = templates.stream()
					.map(t -> ChatColor.GREEN + t.getIdentifier())
					.toArray(String[]::new);
			
			this.plugin.sendMessage(sender, ChatColor.GRAY + "Active Templates:\n" + String.join(ChatColor.GRAY + ", ", templateNames));
			return true;
		}
		
		// Template was identified
		DragonTemplate template = plugin.getDEDManager().getTemplate(args[0]);
		
		// No template found
		if (template == null) {
			this.plugin.sendMessage(sender, "Could not find a template with the name \"" + args[0] + "\"");
			return true;
		}
		
		if (args.length < 2) {
			this.plugin.sendMessage(sender, cmd.getLabel() + " " + args[0] + " <(view/info)|edit>");
			return true;
		}
		
		// "view/info" and "edit" params
		if (args[1].equalsIgnoreCase("view") || args[1].equalsIgnoreCase("info")) {
			if (!sender.hasPermission("dragoneggdrop.template.info")) {
				this.plugin.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
				return true;
			}
			
			sender.sendMessage(ChatColor.GRAY + "Dragon Name: " + ChatColor.GREEN + template.getName());
			sender.sendMessage(ChatColor.GRAY + "Bar Style: " + ChatColor.GREEN + template.getBarStyle());
			sender.sendMessage(ChatColor.GRAY + "Bar Color: " + ChatColor.GREEN + template.getBarColor());
			sender.sendMessage(ChatColor.GRAY + "Spawn Weight: " + ChatColor.GREEN + template.getSpawnWeight());
			sender.sendMessage(ChatColor.GRAY + "Announce Respawn: " + (template.shouldAnnounceRespawn() ? ChatColor.GREEN : ChatColor.RED) + template.shouldAnnounceRespawn());
		}
		
		else if (args[1].equalsIgnoreCase("edit")) {
			if (args.length < 3) {
				this.plugin.sendMessage(sender, cmd.getLabel() + " " + args[0] + " edit <addloot|set>");
				return true;
			}
			
			// "addloot" and "set" params
			if (args[2].equalsIgnoreCase("addloot")) {
				if (!(sender instanceof Player)) {
					this.plugin.sendMessage(sender, "You must be a player to add loot to a template. An item must be held in hand");
					return true;
				}
				
				if (!sender.hasPermission("dragoneggdrop.template.edit.addloot")) {
					this.plugin.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
					return true;
				}
				
				Player player = (Player) sender;
				ItemStack item = player.getInventory().getItemInMainHand();
				double weight = 1;
				
				if (item == null) {
					this.plugin.sendMessage(sender, "You must be holding an item in your main hand to add it to the dragon loot");
					return true;
				}
				
				if (args.length >= 4) {
					weight = NumberUtils.toDouble(args[3], 1.0);
				}
				
				template.getLoot().addLootItem(item, weight);
				this.plugin.sendMessage(sender, "Added " + ChatColor.GREEN + item.getType() + ChatColor.GRAY + " with a weight of " 
						+ ChatColor.YELLOW + weight + ChatColor.GRAY + " to " + args[0] + "'s loot");
				return true;
			}
			
			if (args[2].equalsIgnoreCase("set")) {
				if (!sender.hasPermission("dragoneggdrop.template.edit.set")) {
					this.plugin.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
					return true;
				}
				
				plugin.sendMessage(sender, "This command is a work in progress. Bother Choco or something... I dunno");
				// TODO set variables in configuration file
			}
		}
		
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> options = new ArrayList<>();
		
		// Before completion: "/dragontemplate "
		if (args.length == 1) {
			options.add("list");
			plugin.getDEDManager().getDragonTemplates().stream().map(DragonTemplate::getIdentifier).forEach(t -> options.add(t));
		}
		
		// Before completion: "/dragontemplate <template> "
		else if (args.length == 2) {
			options.add("view");
			options.add("edit");
		}
		
		else if (args.length == 3) {
			// Before completion: "/dragontemplate <template> edit "
			if (args[1].equalsIgnoreCase("edit")) {
				options.add("addloot");
				options.add("set");
			}
		}
		
		return options;
	}
	
}