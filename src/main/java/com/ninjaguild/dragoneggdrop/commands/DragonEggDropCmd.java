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
import org.bukkit.util.StringUtil;

public final class DragonEggDropCmd implements TabExecutor {

    private final DragonEggDrop plugin;

    public DragonEggDropCmd(final DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "-----------------------");
            sender.sendMessage(ChatColor.GOLD + "-- DRAGONEGGDROP INFO --");
            sender.sendMessage(ChatColor.GOLD + "-----------------------");
            sender.sendMessage(ChatColor.GOLD + "Original Author: PixelStix");
            sender.sendMessage(ChatColor.GOLD + "Maintainer: Choco");
            sender.sendMessage(ChatColor.GOLD + "Version: " + plugin.getDescription().getVersion());

            UpdateResult result = UpdateChecker.get().getLastResult();
            if (sender.isOp() && result != null && result.requiresUpdate()) {
                sender.sendMessage(ChatColor.AQUA + "New version available: " + result.getNewestVersion());
            }

            sender.sendMessage(ChatColor.YELLOW + "/dragoneggdrop help");
            sender.sendMessage(ChatColor.GOLD + "-----------------------");

            return true;
        }

        // "help" and "reload" params
        if (args[0].equalsIgnoreCase("help")) {
            if (!sender.hasPermission("dragoneggdrop.help")) {
                this.plugin.sendMessage(sender, ChatColor.RED + "Permission denied!");
                return true;
            }

            sender.sendMessage(ChatColor.GOLD + "-----------------------");
            sender.sendMessage(ChatColor.GOLD + "-- DRAGONEGGDROP HELP --");
            sender.sendMessage(ChatColor.GOLD + "-----------------------");
            sender.sendMessage(ChatColor.GOLD + "/dragoneggdrop reload");
            sender.sendMessage(ChatColor.GOLD + "/dragonspawn");
            sender.sendMessage(ChatColor.GOLD + "/dragontemplate list");
            sender.sendMessage(ChatColor.GOLD + "/dragontemplate <template> (view/info)");
            sender.sendMessage(ChatColor.GOLD + "-----------------------");
        }

        else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("dragoneggdrop.reload")) {
                this.plugin.sendMessage(sender, ChatColor.RED + "Permission denied!");
                return true;
            }

            this.plugin.reloadConfig();
            DragonTemplate.reload();
            this.plugin.getLootTableRegistry().reloadDragonLootTables();
            this.plugin.sendMessage(sender, ChatColor.GREEN + "Reload complete!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("help", "reload"), options);
        }

        return options;
    }

}
