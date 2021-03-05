package wtf.choco.dragoneggdrop.commands;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.dragon.DragonTemplate;
import wtf.choco.dragoneggdrop.dragon.loot.DragonLootTable;
import wtf.choco.dragoneggdrop.particle.ParticleShapeDefinition;
import wtf.choco.dragoneggdrop.utils.CommandUtils;
import wtf.choco.dragoneggdrop.utils.DEDConstants;

public final class CommandDragonTemplate implements TabExecutor {

    private static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private static final Map<@NotNull BarColor, @NotNull ChatColor> BAR_COLOURS = new EnumMap<>(BarColor.class);
    static {
        BAR_COLOURS.put(BarColor.BLUE, ChatColor.BLUE);
        BAR_COLOURS.put(BarColor.GREEN, ChatColor.GREEN);
        BAR_COLOURS.put(BarColor.PINK, ChatColor.LIGHT_PURPLE);
        BAR_COLOURS.put(BarColor.PURPLE, ChatColor.DARK_PURPLE);
        BAR_COLOURS.put(BarColor.RED, ChatColor.RED);
        BAR_COLOURS.put(BarColor.WHITE, ChatColor.WHITE);
        BAR_COLOURS.put(BarColor.YELLOW, ChatColor.YELLOW);
    }

    // /template <list|"template"> <(view/info)|generateloot>

    private final DragonEggDrop plugin;

    public CommandDragonTemplate(@NotNull DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            if (label.endsWith("s")) {
                if (!sender.hasPermission(DEDConstants.PERMISSION_COMMAND_TEMPLATE_LIST)) {
                    DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                    return true;
                }

                this.listTemplates(sender);
                return true;
            }

            DragonEggDrop.sendMessage(sender, "Please specify the name of a template, or \"list\" to list all templates");
            return true;
        }

        // List all existing templates
        if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission(DEDConstants.PERMISSION_COMMAND_TEMPLATE_LIST)) {
                DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            this.listTemplates(sender);
            return true;
        }

        // Template was identified
        DragonTemplate template = plugin.getDragonTemplateRegistry().get(args[0]);

        // No template found
        if (template == null) {
            DragonEggDrop.sendMessage(sender, "Could not find a template with the name \"" + ChatColor.YELLOW + args[0] + ChatColor.GRAY + "\"");
            return true;
        }

        if (args.length < 2) {
            DragonEggDrop.sendMessage(sender, "Missing arguments... " + ChatColor.YELLOW + "/" + label + " " + args[0] + " <view|info>");
            return true;
        }

        // "view/info" and "edit" params
        if (args[1].equalsIgnoreCase("view") || args[1].equalsIgnoreCase("info")) {
            if (!sender.hasPermission(DEDConstants.PERMISSION_COMMAND_TEMPLATE_INFO)) {
                DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            double totalWeight = plugin.getDragonTemplateRegistry().values().stream().mapToDouble(DragonTemplate::getSpawnWeight).sum();
            double chanceToSpawn = (template.getSpawnWeight() / totalWeight) * 100;
            DragonLootTable lootTable = template.getLootTable();
            ParticleShapeDefinition particleShapeDefinition = template.getParticleShapeDefinition();

            sender.sendMessage(ChatColor.GRAY + "Dragon Name: " + ChatColor.GREEN + template.getName());
            sender.sendMessage(ChatColor.GRAY + "Bar Style: " + ChatColor.GREEN + ChatColor.BOLD + template.getBarStyle());
            sender.sendMessage(ChatColor.GRAY + "Bar Colour: " + BAR_COLOURS.get(template.getBarColor()) + ChatColor.BOLD + template.getBarColor());
            sender.sendMessage(ChatColor.GRAY + "Spawn Weight: " + ChatColor.DARK_GREEN + template.getSpawnWeight() + (template.getSpawnWeight() > 0.0 ? ChatColor.GREEN + " (out of " + ChatColor.DARK_GREEN + totalWeight + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + DECIMAL_FORMAT.format(chanceToSpawn) + "% " + ChatColor.GREEN + "chance to spawn)" : ChatColor.RED.toString() + ChatColor.BOLD + " (IMPOSSIBLE)"));
            sender.sendMessage(ChatColor.GRAY + "Announce Spawn: " + (template.shouldAnnounceSpawn() ? ChatColor.GREEN : ChatColor.RED) + template.shouldAnnounceSpawn());
            sender.sendMessage(ChatColor.GRAY + "Loot table: " + ChatColor.YELLOW + (lootTable != null ? lootTable.getId() : "N/A"));
            sender.sendMessage(ChatColor.GRAY + "Particle: " + ChatColor.YELLOW + (particleShapeDefinition != null ? particleShapeDefinition.getId() : "N/A"));
        }

        else if (args[1].equalsIgnoreCase("generateloot")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command cannot be run from the console");
                return true;
            }

            if (!sender.hasPermission(DEDConstants.PERMISSION_COMMAND_TEMPLATE_GENERATELOOT)) {
                DragonEggDrop.sendMessage(sender, ChatColor.RED + "You have insufficient privileges to execute this command");
                return true;
            }

            Block block = ((Player) sender).getLocation().getBlock();
            if (block.getType() != Material.AIR) {
                DragonEggDrop.sendMessage(sender, "You must be standing on " + ChatColor.YELLOW + "air" + ChatColor.GRAY + " in order to generate a loot chest");
                return true;
            }

            DragonLootTable lootTable = template.getLootTable();
            if (lootTable == null) {
                DragonEggDrop.sendMessage(sender, "This template does not have an assigned loot table.");
                return true;
            }

            lootTable.generate(block, template, ((Player) sender));
            DragonEggDrop.sendMessage(sender, ChatColor.GREEN + "The loot of " + template.getName() + ChatColor.GREEN + " has been generated!");
        }

        return true;
    }

    @NotNull
    @Override
    @SuppressWarnings("null")
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        // Before completion: "/dragontemplate "
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(plugin.getDragonTemplateRegistry().keys());
            CommandUtils.addIfHasPermission(sender, DEDConstants.PERMISSION_COMMAND_TEMPLATE_LIST, suggestions, "list");
            return StringUtil.copyPartialMatches(args[0], suggestions, new ArrayList<>());
        }

        // Before completion: "/dragontemplate <template> "
        else if (args.length == 2) {
            if (args[0].equals("list")) {
                return Collections.emptyList();
            }

            List<String> suggestions = new ArrayList<>();

            CommandUtils.addIfHasPermission(sender, DEDConstants.PERMISSION_COMMAND_TEMPLATE_INFO, suggestions, "view", "info");
            CommandUtils.addIfHasPermission(sender, DEDConstants.PERMISSION_COMMAND_TEMPLATE_GENERATELOOT, suggestions, "generateloot");

            return StringUtil.copyPartialMatches(args[1], suggestions, new ArrayList<>());
        }

        return Collections.emptyList();
    }

    private void listTemplates(@NotNull CommandSender sender) {
        Collection<@NotNull DragonTemplate> templates = plugin.getDragonTemplateRegistry().values();
        DragonEggDrop.sendMessage(sender, ChatColor.GRAY + "Loaded Templates:");

        // Don't do any component building for non-players... it's super unnecessary. Converts to legacy anyways
        if (!(sender instanceof Player)) {
            String templatesMessage = templates.stream().map(t -> (t.getSpawnWeight() > 0.0 ? ChatColor.GREEN : ChatColor.DARK_GREEN) + t.getId()).collect(Collectors.joining(ChatColor.GRAY + ", "));
            sender.sendMessage(templatesMessage);
            return;
        }

        double totalWeight = templates.stream().mapToDouble(DragonTemplate::getSpawnWeight).sum();
        ComponentBuilder componentBuilder = new ComponentBuilder();
        templates.forEach(template -> {
            componentBuilder.append(template.getId()).color(template.getSpawnWeight() > 0.0 ? net.md_5.bungee.api.ChatColor.GREEN : net.md_5.bungee.api.ChatColor.DARK_GREEN);
            componentBuilder.italic(template.getSpawnWeight() <= 0.0);

            double chanceToSpawn = (template.getSpawnWeight() / totalWeight) * 100;

            ComponentBuilder hoverComponentBuilder = new ComponentBuilder();
            hoverComponentBuilder.append("Dragon Name: ").color(net.md_5.bungee.api.ChatColor.GRAY).append(template.getName()).color(net.md_5.bungee.api.ChatColor.GREEN);
            hoverComponentBuilder.append("\nBar Style: ").color(net.md_5.bungee.api.ChatColor.GRAY).append(template.getBarStyle().name()).color(net.md_5.bungee.api.ChatColor.GREEN).bold(true);
            hoverComponentBuilder.append("\nBar Colour: ", FormatRetention.NONE).color(net.md_5.bungee.api.ChatColor.GRAY).append(template.getBarColor().name()).color(BAR_COLOURS.get(template.getBarColor()).asBungee()).bold(true);
            hoverComponentBuilder.append("\nSpawn Weight: ", FormatRetention.NONE).color(net.md_5.bungee.api.ChatColor.GRAY).append(String.valueOf(template.getSpawnWeight())).color(net.md_5.bungee.api.ChatColor.DARK_GREEN);
            if (template.getSpawnWeight() > 0.0) {
                hoverComponentBuilder.append(" (out of ").color(net.md_5.bungee.api.ChatColor.GREEN).append(String.valueOf(totalWeight)).color(net.md_5.bungee.api.ChatColor.DARK_GREEN).append(" - ").color(net.md_5.bungee.api.ChatColor.GREEN).append(DECIMAL_FORMAT.format(chanceToSpawn) + "% ").color(net.md_5.bungee.api.ChatColor.DARK_GREEN).append("chance to spawn)").color(net.md_5.bungee.api.ChatColor.GREEN); // Did you really scroll all the way to the end? Weirdo...
            } else {
                hoverComponentBuilder.append(" (IMPOSSIBLE)").color(net.md_5.bungee.api.ChatColor.RED).bold(true);
            }
            hoverComponentBuilder.append("\nAnnounce Spawn: ", FormatRetention.NONE).color(net.md_5.bungee.api.ChatColor.GRAY).append(String.valueOf(template.shouldAnnounceSpawn())).color(template.shouldAnnounceSpawn() ? net.md_5.bungee.api.ChatColor.GREEN : net.md_5.bungee.api.ChatColor.RED);

            DragonLootTable lootTable = template.getLootTable();
            hoverComponentBuilder.append("\nLoot Table: ").color(net.md_5.bungee.api.ChatColor.GRAY).append(lootTable != null ? lootTable.getId() : "N/A").color(net.md_5.bungee.api.ChatColor.YELLOW);

            ParticleShapeDefinition particleShapeDefinition = template.getParticleShapeDefinition();
            hoverComponentBuilder.append("\nParticle: ").color(net.md_5.bungee.api.ChatColor.GRAY).append(particleShapeDefinition != null ? particleShapeDefinition.getId() : "N/A").color(net.md_5.bungee.api.ChatColor.YELLOW);

            componentBuilder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverComponentBuilder.create())));

            if (sender.hasPermission(DEDConstants.PERMISSION_COMMAND_RESPAWN_START)) {
                componentBuilder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/dragonrespawn start 10s " + ((Player) sender).getWorld().getName() + " " + template.getId()));
            }

            componentBuilder.append(", ", FormatRetention.NONE).color(net.md_5.bungee.api.ChatColor.GRAY);
        });
        componentBuilder.removeComponent(componentBuilder.getParts().size() - 1); // Remove the trailing comma

        sender.spigot().sendMessage(componentBuilder.create());
    }

}
