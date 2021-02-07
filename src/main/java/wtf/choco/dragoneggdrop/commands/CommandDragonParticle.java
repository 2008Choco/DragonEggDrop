package wtf.choco.dragoneggdrop.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.particle.AnimatedParticleSession;
import wtf.choco.dragoneggdrop.particle.ParticleShapeDefinition;

public final class CommandDragonParticle implements TabExecutor {

    // /dragonparticle <particle> [x y z] [world] [startY]

    private final DragonEggDrop plugin;

    public CommandDragonParticle(DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command. You can't see particles from the console, silly");
            return true;
        }

        if (args.length < 1) {
            DragonEggDrop.sendMessage(sender, "Missing arguments... " + ChatColor.YELLOW + "/" + label + " <particle>");
            return true;
        }

        ParticleShapeDefinition particleShapeDefinition = plugin.getParticleShapeDefinitionRegistry().get(args[0].toLowerCase());
        if (particleShapeDefinition == null) {
            DragonEggDrop.sendMessage(sender, ChatColor.RED + "Unknown particle shape definition with id " + ChatColor.YELLOW + args[0]);
            return true;
        }

        Player player = (Player) sender;
        Location endLocation = player.getLocation();

        // Specify an end location
        if (args.length >= 2) {
            if (args.length < 4 || !NumberUtils.isNumber(args[1]) || !NumberUtils.isNumber(args[2]) || !NumberUtils.isNumber(args[3])) {
                DragonEggDrop.sendMessage(player, ChatColor.RED + "A complete, valid set of " + ChatColor.YELLOW + "coordinates " + ChatColor.RED + "must be provided");
                return true;
            }

            World world = player.getWorld();
            if (args.length >= 5) {
                world = Bukkit.getWorld(args[4]);
            }

            if (world == null) {
                DragonEggDrop.sendMessage(player, ChatColor.RED + "Invalid or unknown world name, " + ChatColor.AQUA + args[4]);
                return true;
            }

            double x = NumberUtils.toInt(args[1]) + 0.5;
            double y = NumberUtils.toInt(args[2]);
            double z = NumberUtils.toInt(args[3]) + 0.5;

            endLocation = new Location(world, x, y, z);
        }

        double startY = args.length >= 6 ? NumberUtils.toDouble(args[5], particleShapeDefinition.getStartY()) : particleShapeDefinition.getStartY();
        if (startY < endLocation.getY()) {
            DragonEggDrop.sendMessage(player, "The start y (" + startY + ") must not be lower than the end y (" + endLocation.getY() + ")");
            return true;
        }

        Location finalEndLocation = endLocation;
        AnimatedParticleSession particleSession = particleShapeDefinition.createSession(endLocation.getWorld(), endLocation.getX(), startY, endLocation.getZ());

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            particleSession.tick();

            if (particleSession.shouldStop() || particleSession.getCurrentLocation().getY() < finalEndLocation.getY()) {
                DragonEggDrop.sendMessage(player, ChatColor.GREEN + "Done! " + ChatColor.GRAY + "(" + ChatColor.YELLOW + particleShapeDefinition.getId() + ChatColor.GRAY + ")");
                task.cancel();
            }
        }, 0L, 1L);

        String suffix = "";
        if (!endLocation.equals(player.getLocation())) {
            suffix += ChatColor.GRAY + " at " + ChatColor.AQUA + String.format("(%s, %s, %s)", endLocation.getX(), endLocation.getY(), endLocation.getZ());

            if (!player.getLocation().getWorld().equals(endLocation.getWorld())) {
                suffix += ChatColor.GRAY + " in world " + ChatColor.GREEN + endLocation.getWorld().getName();
            }
        }
        suffix += ChatColor.GRAY + ".";

        DragonEggDrop.sendMessage(player, "Playing particle animation " + ChatColor.YELLOW + particleShapeDefinition.getId() + suffix);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            for (ParticleShapeDefinition particleShapeDefinition : plugin.getParticleShapeDefinitionRegistry().values()) {
                if (particleShapeDefinition.getId().startsWith(args[0].toLowerCase())) {
                    suggestions.add(particleShapeDefinition.getId());
                }
            }

            return suggestions;
        }

        // Target block completion
        else if (args.length >= 2 && args.length < 6 && sender instanceof Player) {
            Player player = (Player) sender;
            Block target = player.getTargetBlockExact(6);

            if (target == null) {
                if (args.length >= 2 && args.length < 5 && args[1].isEmpty()) {
                    return Arrays.asList("[x y z]");
                }
                else if (args.length >= 5 && args[4].isEmpty()) {
                    List<String> suggestions = new ArrayList<>();

                    for (World world : Bukkit.getWorlds()) {
                        String worldName = world.getName();
                        if (worldName.startsWith(args[4])) {
                            suggestions.add(worldName);
                        }
                    }

                    return suggestions;
                }
                else {
                    return Collections.emptyList();
                }
            }

            // Switch expressions please :((
            switch (args.length) {
                case 2: return StringUtil.copyPartialMatches(args[1], Arrays.asList(String.valueOf(target.getX())), new ArrayList<>());
                case 3: return StringUtil.copyPartialMatches(args[2], Arrays.asList(String.valueOf(target.getY())), new ArrayList<>());
                case 4: return StringUtil.copyPartialMatches(args[3], Arrays.asList(String.valueOf(target.getZ())), new ArrayList<>());
                case 5: return StringUtil.copyPartialMatches(args[4], Arrays.asList(target.getWorld().getName()), new ArrayList<>());
            }
        }

        else if (args.length == 6 && args[5].isEmpty()) {
            return Arrays.asList("[startY]");
        }

        return Collections.emptyList();
    }

}
