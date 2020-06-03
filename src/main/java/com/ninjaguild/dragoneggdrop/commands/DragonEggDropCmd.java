package com.ninjaguild.dragoneggdrop.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.utils.UpdateChecker;
import com.ninjaguild.dragoneggdrop.utils.UpdateChecker.UpdateResult;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;

public final class DragonEggDropCmd implements TabExecutor {

    private final DragonEggDrop plugin;

    public DragonEggDropCmd(DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            PluginDescriptionFile description = plugin.getDescription();

            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "--------------------------------------------");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GRAY + "Version: " + ChatColor.YELLOW + description.getVersion());
            sender.sendMessage(ChatColor.GRAY + "Developer / Maintainer: " + ChatColor.YELLOW + description.getAuthors().get(0));
            sender.sendMessage(ChatColor.GRAY + "Former author: " + ChatColor.YELLOW + "PixelStix");
            sender.sendMessage(ChatColor.GRAY + "Plugin Page: " + ChatColor.YELLOW + "https://www.spigotmc.org/resources/35570/");
            sender.sendMessage(ChatColor.GRAY + "Report bugs to: " + ChatColor.YELLOW + "https://github.com/2008Choco/DragonEggDrop/issues/");

            UpdateResult result = UpdateChecker.get().getLastResult();
            if (sender.isOp() && result != null && result.requiresUpdate()) {
                sender.sendMessage(ChatColor.AQUA + "New version available: " + result.getNewestVersion());
            }

            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "--------------------------------------------");
            return true;
        }

        else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("dragoneggdrop.command.reload")) {
                DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            this.plugin.reloadConfig();
            DragonTemplate.reload();
            this.plugin.getLootTableRegistry().reloadDragonLootTables();
            DragonEggDrop.sendMessage(sender, ChatColor.GREEN + "Reload complete!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("reload"), options);
        }

        return options;
    }

}
