package wtf.choco.dragoneggdrop.commands;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import wtf.choco.commons.util.MathUtil;
import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.dragon.DragonTemplate;
import wtf.choco.dragoneggdrop.dragon.loot.DragonLootTable;
import wtf.choco.dragoneggdrop.utils.CommandUtils;
import wtf.choco.dragoneggdrop.utils.DEDConstants;
import wtf.choco.dragoneggdrop.world.DragonBattleRecord;
import wtf.choco.dragoneggdrop.world.EndWorldWrapper;

public final class CommandDragonRespawn implements TabExecutor {

    /* /dragonrespawn
     *     : <stop, interrupt, cancel> [world] - Stop any active respawn countdown
     *     : start [time] [world] [template] [loot_table] - Start a respawn
     *     : template - Get the template to spawn in the current world
     *         set <template> [world] - Set the dragon to spawn while the countdown is active
     *         [world] - Get the template to spawn in the specified world
     *     : history [world]
     */

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
             .withLocale(Locale.US)
             .withZone(ZoneId.systemDefault());

    private final DragonEggDrop plugin;

    public CommandDragonRespawn(@NotNull DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            DragonEggDrop.sendMessage(sender, "Missing arguments... " + ChatColor.YELLOW + "/" + label + " <stop|start|template>");
            return true;
        }

        if (args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("interrupt") || args[0].equalsIgnoreCase("cancel")) {
            if (!sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_STOP)) {
                DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            World world = getWorldFromContext(sender, args, 1);
            if (world == null) {
                return true;
            }

            EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);
            if (!worldWrapper.isRespawnInProgress()) {
                DragonEggDrop.sendMessage(sender, "No respawn is currently in progress");
                return true;
            }

            worldWrapper.stopRespawn();
            DragonEggDrop.sendMessage(sender, "The respawn in world " + ChatColor.YELLOW + world.getName() + ChatColor.GRAY + " has been stopped");
        }

        else if (args[0].equalsIgnoreCase("start")) {
            if (!sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_START)) {
                DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            World world = getWorldFromContext(sender, args, 2);
            if (world == null) {
                return true;
            }

            if (plugin.getConfig().getStringList(DEDConstants.CONFIG_DISABLED_WORLDS).contains(world.getName())) {
                DragonEggDrop.sendMessage(sender, "Dragon respawning is disabled in this world.");
                return true;
            }

            EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);
            if (worldWrapper.isRespawnInProgress()) {
                DragonEggDrop.sendMessage(sender, "A respawn is already in progress. It must be stopped (" + ChatColor.YELLOW + "/" + label + " <stop>" + ChatColor.GRAY + ") before starting another");
                return true;
            }

            long respawnSeconds = (args.length >= 2) ? MathUtil.parseSeconds(args[1]) : 300; // Default 5 minutes
            DragonTemplate template = plugin.getDragonTemplateRegistry().getRandomTemplate();
            assert template != null; // Impossible

            if (args.length >= 4) {
                DragonTemplate templateArgument = plugin.getDragonTemplateRegistry().get(args[3]);
                if (templateArgument == null) {
                    DragonEggDrop.sendMessage(sender, "A template with the name " + ChatColor.YELLOW + args[3] + ChatColor.GRAY + " does not exist");
                    return true;
                }

                template = templateArgument;
            }

            DragonLootTable lootTable = null;
            if (args.length >= 5) {
                DragonLootTable lootTableArgument = plugin.getLootTableRegistry().get(args[4]);
                if (lootTableArgument == null) {
                    DragonEggDrop.sendMessage(sender, "A loot table with the id " + ChatColor.YELLOW + args[4] + ChatColor.GRAY + " does not exist");
                    return true;
                }

                lootTable = lootTableArgument;
            }

            if (!worldWrapper.startRespawn(respawnSeconds, template, lootTable)) {
                DragonEggDrop.sendMessage(sender, "A respawn could not be started... does a dragon already exist in this world?");
                return true;
            }

            DragonLootTable lootTableOverride = worldWrapper.getLootTableOverride();
            DragonEggDrop.sendMessage(sender, "A respawn has been started in world " + ChatColor.YELLOW + world.getName() + ChatColor.GRAY + " with template " + ChatColor.GREEN + template.getId()
                    + (lootTableOverride != null ? ChatColor.GRAY + " (loot table override: " + ChatColor.LIGHT_PURPLE + lootTableOverride.getId() + ChatColor.GRAY + ")" : ""));
        }

        else if (args[0].equalsIgnoreCase("template")) {
            if (!sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_TEMPLATE)) {
                DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            // /dragonrespawn template set <template> [world]
            if (args.length >= 2 && args[1].equalsIgnoreCase("set")) {
                World world = getWorldFromContext(sender, args, 3);
                if (world == null) {
                    return true;
                }

                if (plugin.getConfig().getStringList(DEDConstants.CONFIG_DISABLED_WORLDS).contains(world.getName())) {
                    DragonEggDrop.sendMessage(sender, "Dragon respawning is disabled in this world.");
                    return true;
                }

                EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);
                if (!worldWrapper.isRespawnInProgress()) {
                    DragonEggDrop.sendMessage(sender, "No respawn is currently in progress, cannot set the template to spawn");
                    return true;
                }

                if (args.length < 3) {
                    DragonEggDrop.sendMessage(sender, "Please specify the template you would like to set for this world");
                    return true;
                }

                DragonTemplate template = plugin.getDragonTemplateRegistry().get(args[2]);
                if (template == null) {
                    DragonEggDrop.sendMessage(sender, "A template with the name " + ChatColor.YELLOW + args[2] + ChatColor.GRAY + " does not exist");
                    return true;
                }

                worldWrapper.setRespawningTemplate(template);
                DragonEggDrop.sendMessage(sender, "The dragon template " + ChatColor.YELLOW + template.getId() + ChatColor.GRAY + " will be spawned in the world " + ChatColor.GREEN + world.getName());
                return true;
            }

            // /dragonrespawn template [world]
            World world = getWorldFromContext(sender, args, 1);
            if (world == null) {
                return true;
            }

            EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);
            DragonTemplate template = worldWrapper.getRespawningTemplate();
            if (template == null) {
                DragonEggDrop.sendMessage(sender, "No respawn is currently in progress, no template has yet been determined");
                return true;
            }

            DragonEggDrop.sendMessage(sender, "The template with ID " + ChatColor.YELLOW + template.getId() + ChatColor.GRAY + " will be spawned in the world " + ChatColor.GREEN + world.getName());
            return true;
        }

        else if (args[0].equalsIgnoreCase("history")) {
            World world = getWorldFromContext(sender, args, 1);
            if (world == null) {
                return true;
            }

            EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);
            List<@NotNull DragonBattleRecord> previousDragonBattles = worldWrapper.getPreviousDragonBattles();
            if (previousDragonBattles.isEmpty()) {
                DragonEggDrop.sendMessage(sender, "There have been no dragon battles in this world.");
                return true;
            }

            sender.sendMessage(ChatColor.GRAY + "Previous battles in world " + ChatColor.GREEN + world.getName() + ChatColor.GRAY + ":");
            for (int i = 0; i < previousDragonBattles.size(); i++) {
                DragonBattleRecord battleRecord = previousDragonBattles.get(i);

                DragonTemplate template = battleRecord.getTemplate();
                DragonLootTable lootTable = battleRecord.getLootTable();
                Instant deathTimestamp = battleRecord.getDeathInstant().truncatedTo(ChronoUnit.SECONDS);
                String deathTimestampString = DATE_FORMATTER.format(deathTimestamp);

                sender.sendMessage(ChatColor.WHITE.toString() + (i + 1) + ". " + template.getName() + ChatColor.GRAY + (lootTable != null ? " (loot table: " + ChatColor.AQUA + lootTable.getId() + ChatColor.GRAY + ")" : "") + " killed " + ChatColor.YELLOW + deathTimestampString);
            }
        }

        else {
            DragonEggDrop.sendMessage(sender, "Unknown argument " + ChatColor.YELLOW + args[0] + ChatColor.GRAY + ". Usage: " + ChatColor.YELLOW + "/" + label + " <stop|start|template>");
        }

        return true;
    }

    private static final List<String> DEFAULT_TIME_SUGGESTIONS = Arrays.asList("30s", "5m", "1h", "7d", "2w", "2w7d1h5m30s");
    private static final char[] POSSIBLE_TIME_SUFFIXES = {'s', 'm', 'h', 'd', 'w'};

    @NotNull
    @Override
    @SuppressWarnings("null") // Eclipse weirdness
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            CommandUtils.addIfHasPermission(sender, DEDConstants.PERMISSION_COMMAND_RESPAWN_STOP, suggestions, "stop", "interrupt", "cancel");
            CommandUtils.addIfHasPermission(sender, DEDConstants.PERMISSION_COMMAND_RESPAWN_START, suggestions, "start");
            CommandUtils.addIfHasPermission(sender, DEDConstants.PERMISSION_COMMAND_RESPAWN_TEMPLATE, suggestions, "template");
            CommandUtils.addIfHasPermission(sender, DEDConstants.PERMISSION_COMMAND_HISTORY, suggestions, "history");

            return StringUtil.copyPartialMatches(args[0], suggestions, new ArrayList<>());
        }

        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("interrupt") || args[0].equalsIgnoreCase("cancel") && sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_STOP)) {
                return StringUtil.copyPartialMatches(args[1], getNonDisabledWorlds(), new ArrayList<>());
            }

            else if (args[0].equalsIgnoreCase("start") && sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_START)) {
                String timeArg = args[1];
                if (timeArg.isEmpty()) {
                    return DEFAULT_TIME_SUGGESTIONS;
                }

                char character = timeArg.charAt(timeArg.length() - 1);
                if (character >= '0' && character <= '9') {
                    List<String> suggestions = new ArrayList<>();

                    for (char timeSuffix : POSSIBLE_TIME_SUFFIXES) {
                        if (timeArg.lastIndexOf(timeSuffix) == -1) {
                            suggestions.add(timeArg + timeSuffix);
                        }
                    }

                    return StringUtil.copyPartialMatches(args[1], suggestions, new ArrayList<>());
                }
            }

            else if (args[0].equalsIgnoreCase("template") && sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_TEMPLATE)) {
                List<String> suggestions = getNonDisabledWorlds();
                suggestions.add("set");

                return StringUtil.copyPartialMatches(args[1], suggestions, new ArrayList<>());
            }

            else if (args[0].equalsIgnoreCase("history") && sender.hasPermission(DEDConstants.PERMISSION_COMMAND_HISTORY)) {
                return StringUtil.copyPartialMatches(args[1], getNonDisabledWorlds(), new ArrayList<>());
            }
        }

        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("start") && sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_START)) {
                return StringUtil.copyPartialMatches(args[2], getNonDisabledWorlds(), new ArrayList<>());
            }

            else if (args[0].equalsIgnoreCase("template") && args[1].equalsIgnoreCase("set") && sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_TEMPLATE)) {
                return StringUtil.copyPartialMatches(args[2], plugin.getDragonTemplateRegistry().keys(), new ArrayList<>());
            }
        }

        else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("start") && sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_START)) {
                return StringUtil.copyPartialMatches(args[3], plugin.getDragonTemplateRegistry().keys(), new ArrayList<>());
            }

            else if (args[0].equalsIgnoreCase("template") && args[1].equalsIgnoreCase("set") && sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_TEMPLATE)) {
                return StringUtil.copyPartialMatches(args[3], getNonDisabledWorlds(), new ArrayList<>());
            }
        }

        else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("start") && sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_START)) {
                return StringUtil.copyPartialMatches(args[4], plugin.getLootTableRegistry().keys(), new ArrayList<>());
            }
        }

        return Collections.emptyList();
    }

    private World getWorldFromContext(CommandSender sender, String[] args, int argumentIndex) {
        if (args.length >= (argumentIndex + 1)) {
            String worldName = args[argumentIndex];
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                DragonEggDrop.sendMessage(sender, "Could not find a world with the name " + ChatColor.YELLOW + worldName);
                return null;
            }

            if (world.getEnvironment() != Environment.THE_END) {
                DragonEggDrop.sendMessage(sender, "The specified world (" + ChatColor.YELLOW + worldName + ChatColor.GRAY + ") is not an end world");
                return null;
            }

            return Bukkit.getWorld(worldName);
        }

        if (!(sender instanceof Player)) {
            DragonEggDrop.sendMessage(sender, "A world name must be specified when sending this command from the console");
            return null;
        }

        World world = ((Player) sender).getWorld();
        if (world.getEnvironment() != Environment.THE_END) {
            DragonEggDrop.sendMessage(sender, "The world in which you are executing this command is not an end world... please specify a world instead");
            return null;
        }

        return world;
    }

    private List<@NotNull String> getNonDisabledWorlds() {
        List<@NotNull String> result = new ArrayList<>();

        List<@NotNull String> disabledWorlds = plugin.getConfig().getStringList(DEDConstants.CONFIG_DISABLED_WORLDS);
        Bukkit.getWorlds().forEach(world -> {
            String worldName = world.getName();
            if (world.getEnvironment() == Environment.THE_END && !disabledWorlds.contains(worldName)) {
                result.add(worldName);
            }
        });

        return result;
    }

}
