package com.ninjaguild.dragoneggdrop.placeholder;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DamageHistory;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.utils.math.MathUtils;
import com.ninjaguild.dragoneggdrop.world.EndWorldWrapper;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

final class DragonEggDropPlaceholderAPIExpansion extends PlaceholderExpansion {

    private static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    private final DragonEggDrop plugin;

    DragonEggDropPlaceholderAPIExpansion(DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return plugin.getDescription().getName().toLowerCase(); // dragoneggdrop
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {
        if (placeholder.equalsIgnoreCase("dragon")) { // %dragoneggdrop_dragon%
            if (player == null) {
                return null;
            }

            World world = player.getWorld();
            if (world.getEnvironment() != Environment.THE_END) {
                return "no dragon in this world";
            }

            EndWorldWrapper endWorld = EndWorldWrapper.of(world);
            DragonTemplate template = endWorld.getActiveTemplate();
            return (template != null) ? template.getName() : null;
        }

        else if (placeholder.startsWith("dragon_")) { // %dragoneggdrop_dragon[_world]%
            World world = Bukkit.getWorld(placeholder.substring("dragon_".length()));
            if (world == null || world.getEnvironment() != Environment.THE_END) {
                return "no dragon in this world";
            }

            EndWorldWrapper endWorld = EndWorldWrapper.of(world);
            DragonTemplate template = endWorld.getActiveTemplate();
            return (template != null) ? template.getName() : "no dragon";
        }

        else if (placeholder.equalsIgnoreCase("respawn_time")) { // %dragoneggdrop_respawn_time%
            if (player == null) {
                return null;
            }

            World world = player.getWorld();
            if (world.getEnvironment() != Environment.THE_END) {
                return "no respawn in this world";
            }

            EndWorldWrapper endWorld = EndWorldWrapper.of(world);
            return (endWorld.isRespawnInProgress()) ? MathUtils.getFormattedTime(endWorld.getTimeUntilRespawn()) : "no respawn in progress";
        }

        else if (placeholder.startsWith("respawn_time_")) { // %dragoneggdrop_respawn_time[_world]%
            World world = Bukkit.getWorld(placeholder.substring("respawn_time_".length()));
            if (world == null || world.getEnvironment() != Environment.THE_END) {
                return "invalid world";
            }

            EndWorldWrapper endWorld = EndWorldWrapper.of(world);
            return (endWorld.isRespawnInProgress()) ? MathUtils.getFormattedTime(endWorld.getTimeUntilRespawn()) : null;
        }

        else if (DragonEggDropPlaceholders.PATTERN_TOP_DAMAGER.asPredicate().test(placeholder)) { // %dragoneggdrop_top_damager<_number>[_world]%
            Matcher matcher = DragonEggDropPlaceholders.PATTERN_TOP_DAMAGER.matcher(placeholder);
            if (!matcher.find()) {
                return null;
            }

            int offset = (matcher.group(1) != null) ? NumberUtils.toInt(matcher.group(1), 0) - 1 : 0;
            if (offset < 0) {
                return null;
            }

            World world = (matcher.group(2) != null) ? Bukkit.getWorld(matcher.group(2)) : (player != null ? player.getWorld() : null);
            if (world == null || world.getEnvironment() != Environment.THE_END) {
                return "invalid world";
            }

            DamageHistory history = null;
            EndWorldWrapper endWorld = EndWorldWrapper.of(world);
            if (endWorld.getActiveTemplate() != null) {
                history = DamageHistory.forEntity(world.getEnderDragonBattle().getEnderDragon());
            }

            // TODO: Get history from most recent battle if active battle is null

            if (history == null || offset >= history.uniqueDamagers()) {
                return "None";
            }

            Entity topDamager = history.getTopDamager(offset).getSourceEntity();
            return (topDamager != null) ? topDamager.getName() : "INVALID_ENTITY";
        }

        else if (DragonEggDropPlaceholders.PATTERN_TOP_DAMAGE.asPredicate().test(placeholder)) { // %dragoneggdrop_top_damage<_number>[_world]%
            Matcher matcher = DragonEggDropPlaceholders.PATTERN_TOP_DAMAGE.matcher(placeholder);
            if (!matcher.find()) {
                return null;
            }

            int offset = (matcher.group(1) != null) ? NumberUtils.toInt(matcher.group(1), 0) - 1 : 0;
            if (offset < 0) {
                return null;
            }

            World world = (matcher.group(2) != null) ? Bukkit.getWorld(matcher.group(2)) : (player != null ? player.getWorld() : null);
            if (world == null || world.getEnvironment() != Environment.THE_END) {
                return "invalid world";
            }

            DamageHistory history = null;
            EndWorldWrapper endWorld = EndWorldWrapper.of(world);
            if (endWorld.getActiveTemplate() != null) {
                history = DamageHistory.forEntity(world.getEnderDragonBattle().getEnderDragon());
            }

            // TODO: Get history from most recent battle if active battle is null

            return (history != null && offset < history.uniqueDamagers()) ? DECIMAL_FORMAT.format(history.getTopDamager(offset).getDamage()) : "0";
        }

        return null;
    }

    static String inject(OfflinePlayer player, String string) {
        return PlaceholderAPI.setPlaceholders(player, string);
    }

}
