package wtf.choco.dragoneggdrop.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import wtf.choco.commons.util.UpdateChecker;
import wtf.choco.commons.util.UpdateChecker.UpdateReason;
import wtf.choco.commons.util.UpdateChecker.UpdateResult;
import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.utils.DEDConstants;
import wtf.choco.dragoneggdrop.utils.DataFileUtils;

public final class CommandDragonEggDrop implements TabExecutor {

    private final DragonEggDrop plugin;

    public CommandDragonEggDrop(@NotNull DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
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
            if (sender.isOp() && result != null) {
                if (result.requiresUpdate()) {
                    sender.sendMessage(ChatColor.AQUA + "New version available: " + result.getNewestVersion());
                }
                else if (result.getReason() == UpdateReason.UNRELEASED_VERSION) {
                    sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "WARNING! " + ChatColor.AQUA + "You are on a development version of DragonEggDrop. Things may not be stable. Proceed with caution!");
                }
            }

            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "--------------------------------------------");
            return true;
        }

        else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RELOAD)) {
                DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            this.plugin.reloadConfig();
            DataFileUtils.reloadInMemoryData(plugin, false);
            DragonEggDrop.sendMessage(sender, ChatColor.GREEN + "Reload complete!");
        }

        return true;
    }

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        List<String> options = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RELOAD)) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("reload"), options);
        }

        return options;
    }

}
