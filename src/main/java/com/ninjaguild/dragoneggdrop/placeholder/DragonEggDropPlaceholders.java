package com.ninjaguild.dragoneggdrop.placeholder;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DamageHistory;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.utils.math.MathUtils;
import com.ninjaguild.dragoneggdrop.world.EndWorldWrapper;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

public final class DragonEggDropPlaceholders {

    private static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final Pattern PATTERN_TOP_DAMAGER = Pattern.compile("top_damager(?:_(\\d+))?(?:_([\\w\\d]+))?");
    private static final Pattern PATTERN_TOP_DAMAGE = Pattern.compile("top_damage(?:_(\\d+))?(?:_([\\w\\d]+))?");

    private static PlaceholderProvider provider;

    private DragonEggDropPlaceholders() { }

    public static void registerPlaceholders(DragonEggDrop plugin, PluginManager pluginManager) {
        // DragonEggDrop PlaceholderExpansion (PlaceholderAPI)
        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            PlaceholderProviderPlaceholderAPI expansion = new PlaceholderProviderPlaceholderAPI(plugin);
            expansion.register();

            DragonEggDropPlaceholders.provider = expansion;
            plugin.getLogger().info("Hooked into PlaceholderAPI - Expansion registered");
        }

        // Default to DragonEggDrop's placeholders
        else {
            DragonEggDropPlaceholders.provider = new PlaceholderProviderDefault();
            plugin.getLogger().info("No placeholder library found. Using the default DragonEggDrop placeholder provider");
        }
    }

    /**
     * Get the active placeholder provider.
     *
     * @return the placeholder provider
     */
    public static PlaceholderProvider getProvider() {
        return provider;
    }

    /**
     * Inject placeholders into the provided string given an optional player context.
     * <p>
     * This is a utility method for {@link PlaceholderProvider#inject(OfflinePlayer, String)}
     *
     * @param player the player context or null if none
     * @param string the string to inject
     *
     * @return the injected string
     *
     * @see PlaceholderProvider#inject(OfflinePlayer, String)
     */
    public static String inject(OfflinePlayer player, String string) {
        return provider.inject(player, string);
    }

    /**
     * Inject placeholders into the Strings from the provided item given an optional
     * player context.
     * <p>
     * This is a utility method for {@link PlaceholderProvider#inject(OfflinePlayer, ItemStack)}
     *
     * @param player the player context or null if none
     * @param item the item to inject
     *
     * @see PlaceholderProvider#inject(OfflinePlayer, ItemStack)
     */
    public static void inject(OfflinePlayer player, ItemStack item) {
        provider.inject(player, item);
    }

    /**
     * Inject placeholders into the Strings from the provided item given an optional
     * player context. The passed ItemStack is final and will not be modified. Instead,
     * a copy of the ItemStack (which has been injected) is returned.
     * <p>
     * This is a utility method for {@link PlaceholderProvider#injectCopy(OfflinePlayer, ItemStack)}
     *
     * @param player the player context or null if none
     * @param item the item to inject
     *
     * @return the injected ItemStack
     *
     * @see PlaceholderProvider#injectCopy(OfflinePlayer, ItemStack)
     */
    public static ItemStack injectCopy(OfflinePlayer player, final ItemStack item) {
        return provider.injectCopy(player, item);
    }

    static String replacePlaceholder(OfflinePlayer player, String placeholder) {
        if (placeholder.equalsIgnoreCase("dragon")) { // %dragoneggdrop_dragon%
            if (player == null || !player.isOnline()) {
                return null;
            }

            World world = player.getPlayer().getWorld();
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

        else if (placeholder.equalsIgnoreCase("slain_dragon")) { // %dragoneggdrop_slain_dragon%
            if (player == null || !player.isOnline()) {
                return null;
            }

            World world = player.getPlayer().getWorld();
            if (world.getEnvironment() != Environment.THE_END) {
                return "no dragon in this world";
            }

            EndWorldWrapper endWorld = EndWorldWrapper.of(world);
            DragonTemplate template = endWorld.getPreviousTemplate();
            return (template != null) ? template.getName() : null;
        }

        else if (placeholder.startsWith("slain_dragon_")) { // %dragoneggdrop_slain_dragon[_world]%
            World world = Bukkit.getWorld(placeholder.substring("dragon_".length()));
            if (world == null || world.getEnvironment() != Environment.THE_END) {
                return "no dragon in this world";
            }

            EndWorldWrapper endWorld = EndWorldWrapper.of(world);
            DragonTemplate template = endWorld.getPreviousTemplate();
            return (template != null) ? template.getName() : "no dragon";
        }

        else if (placeholder.equalsIgnoreCase("respawn_time")) { // %dragoneggdrop_respawn_time%
            if (player == null || !player.isOnline()) {
                return null;
            }

            World world = player.getPlayer().getWorld();
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

            World world = (matcher.group(2) != null) ? Bukkit.getWorld(matcher.group(2)) : (player != null && player.isOnline() ? player.getPlayer().getWorld() : null);
            if (world == null || world.getEnvironment() != Environment.THE_END) {
                return "invalid world";
            }

            DamageHistory history = null;
            EndWorldWrapper endWorld = EndWorldWrapper.of(world);
            UUID previousDragonUUID = endWorld.getPreviousDragonUUID();
            if (previousDragonUUID != null) {
                history = DamageHistory.forEntity(previousDragonUUID);
            }

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

            World world = (matcher.group(2) != null) ? Bukkit.getWorld(matcher.group(2)) : (player != null && player.isOnline() ? player.getPlayer().getWorld() : null);
            if (world == null || world.getEnvironment() != Environment.THE_END) {
                return "invalid world";
            }

            DamageHistory history = null;
            EndWorldWrapper endWorld = EndWorldWrapper.of(world);
            UUID previousDragonUUID = endWorld.getPreviousDragonUUID();
            if (previousDragonUUID != null) {
                history = DamageHistory.forEntity(previousDragonUUID);
            }

            return (history != null && offset < history.uniqueDamagers()) ? DECIMAL_FORMAT.format(history.getTopDamager(offset).getDamage()) : "0";
        }

        return null;
    }

}
