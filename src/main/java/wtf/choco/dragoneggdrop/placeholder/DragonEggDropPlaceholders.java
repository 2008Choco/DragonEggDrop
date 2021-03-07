package wtf.choco.dragoneggdrop.placeholder;

import com.google.common.base.Preconditions;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.commons.util.MathUtil;
import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.dragon.DamageHistory;
import wtf.choco.dragoneggdrop.dragon.DamageHistory.DamageEntry;
import wtf.choco.dragoneggdrop.dragon.DragonTemplate;
import wtf.choco.dragoneggdrop.utils.ConfigUtils;
import wtf.choco.dragoneggdrop.utils.DEDConstants;
import wtf.choco.dragoneggdrop.world.DragonRespawnData;
import wtf.choco.dragoneggdrop.world.EndWorldWrapper;

public final class DragonEggDropPlaceholders {

    private static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
    private static final Pattern PATTERN_TOP_DAMAGER = Pattern.compile("top_damager(?:_(\\d+))?(?:_([\\w\\d]+))?");
    private static final Pattern PATTERN_TOP_DAMAGE = Pattern.compile("top_damage(?:_(\\d+))?(?:_([\\w\\d]+))?");

    private static PlaceholderProvider provider;

    private DragonEggDropPlaceholders() { }

    public static void registerPlaceholders(@NotNull DragonEggDrop plugin, @NotNull PluginManager pluginManager) {
        Preconditions.checkArgument(plugin != null, "plugin must not be null");
        Preconditions.checkArgument(pluginManager != null, "pluginManager must not be null");

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
    @NotNull
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
    @NotNull
    public static String inject(@Nullable OfflinePlayer player, @NotNull String string) {
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
    public static void inject(@Nullable OfflinePlayer player, @NotNull ItemStack item) {
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
    @NotNull
    public static ItemStack injectCopy(@Nullable OfflinePlayer player, @NotNull final ItemStack item) {
        return provider.injectCopy(player, item);
    }

    @Nullable
    static String replacePlaceholder(@Nullable OfflinePlayer player, @NotNull String placeholder) {
        if (placeholder.equalsIgnoreCase("dragon")) { // %dragoneggdrop_dragon%
            if (player == null || !player.isOnline()) {
                return null;
            }

            World world = getWorld(player);
            if (world == null) {
                return "unknown world";
            }

            if (world.getEnvironment() != Environment.THE_END) {
                return "no dragon in this world";
            }

            EndWorldWrapper endWorld = EndWorldWrapper.of(world);
            DragonTemplate template = endWorld.getActiveTemplate();
            if (template != null) {
                return template.getName();
            }

            DragonTemplate respawningTemplate = endWorld.getRespawningTemplate();
            return respawningTemplate != null ? respawningTemplate.getName() : "no dragon";
        }

        else if (placeholder.startsWith("dragon_")) { // %dragoneggdrop_dragon[_world]%
            World world = Bukkit.getWorld(placeholder.substring("dragon_".length()));
            if (world == null || world.getEnvironment() != Environment.THE_END) {
                return "no dragon in this world";
            }

            EndWorldWrapper endWorld = EndWorldWrapper.of(world);
            DragonTemplate template = endWorld.getActiveTemplate();
            if (template != null) {
                return template.getName();
            }

            DragonTemplate respawningTemplate = endWorld.getRespawningTemplate();
            return respawningTemplate != null ? respawningTemplate.getName() : "no dragon";
        }

        else if (placeholder.equalsIgnoreCase("slain_dragon")) { // %dragoneggdrop_slain_dragon%
            if (player == null || !player.isOnline()) {
                return null;
            }

            World world = getWorld(player);
            if (world == null) {
                return "unknown world";
            }

            if (world.getEnvironment() != Environment.THE_END) {
                return "no dragon in this world";
            }

            EndWorldWrapper endWorld = EndWorldWrapper.of(world);
            DragonTemplate template = endWorld.getPreviousTemplate();
            return (template != null) ? template.getName() : null;
        }

        else if (placeholder.startsWith("slain_dragon_")) { // %dragoneggdrop_slain_dragon[_world]%
            World world = Bukkit.getWorld(placeholder.substring("slain_dragon_".length()));
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

            World world = getWorld(player);
            if (world == null) {
                return "unknown world";
            }

            if (world.getEnvironment() != Environment.THE_END) {
                return "no respawn in this world";
            }

            EndWorldWrapper endWorld = EndWorldWrapper.of(world);

            FileConfiguration config = DragonEggDrop.getInstance().getConfig();
            boolean condensed = config.getBoolean(DEDConstants.CONFIG_RESPAWN_MESSAGES_CONDENSED);
            TimeUnit[] omitions = ConfigUtils.getTimeUnits(config.getStringList(DEDConstants.CONFIG_RESPAWN_MESSAGES_OMIT_TIME_UNITS));

            DragonRespawnData respawnData = endWorld.getDragonRespawnData();
            return (endWorld.isRespawnInProgress() && respawnData != null) ? MathUtil.getFormattedTime(respawnData.getRemainingTime(TimeUnit.SECONDS), TimeUnit.SECONDS, condensed, omitions) : "now";
        }

        else if (placeholder.startsWith("respawn_time_")) { // %dragoneggdrop_respawn_time[_world]%
            World world = Bukkit.getWorld(placeholder.substring("respawn_time_".length()));
            if (world == null || world.getEnvironment() != Environment.THE_END) {
                return "invalid world";
            }

            EndWorldWrapper endWorld = EndWorldWrapper.of(world);

            FileConfiguration config = DragonEggDrop.getInstance().getConfig();
            boolean condensed = config.getBoolean(DEDConstants.CONFIG_RESPAWN_MESSAGES_CONDENSED);
            TimeUnit[] omitions = ConfigUtils.getTimeUnits(config.getStringList(DEDConstants.CONFIG_RESPAWN_MESSAGES_OMIT_TIME_UNITS));

            DragonRespawnData respawnData = endWorld.getDragonRespawnData();
            return (endWorld.isRespawnInProgress() && respawnData != null) ? MathUtil.getFormattedTime(respawnData.getRemainingTime(TimeUnit.SECONDS), TimeUnit.SECONDS, condensed, omitions) : "now";
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

            World world = (matcher.group(2) != null) ? Bukkit.getWorld(matcher.group(2)) : (player != null && player.isOnline() ? getWorld(player) : null);
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

            DamageEntry topDamageEntry = history.getTopDamager(offset);
            if (topDamageEntry == null) {
                return "None";
            }

            Entity topDamager = topDamageEntry.getSourceEntity();
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

            World world = (matcher.group(2) != null) ? Bukkit.getWorld(matcher.group(2)) : (player != null && player.isOnline() ? getWorld(player) : null);
            if (world == null || world.getEnvironment() != Environment.THE_END) {
                return "invalid world";
            }

            DamageHistory history = null;
            EndWorldWrapper endWorld = EndWorldWrapper.of(world);
            UUID previousDragonUUID = endWorld.getPreviousDragonUUID();
            if (previousDragonUUID != null) {
                history = DamageHistory.forEntity(previousDragonUUID);
            }

            if (history == null) {
                return "0";
            }

            DamageEntry topDamageEntry = history.getTopDamager(offset);
            if (topDamageEntry == null) {
                return "0";
            }

            return (history != null && offset < history.uniqueDamagers()) ? DECIMAL_FORMAT.format(topDamageEntry.getDamage()) : "0";
        }

        return null;
    }

    @Nullable
    private static World getWorld(@Nullable OfflinePlayer player) {
        if (player == null) {
            return null;
        }

        Player onlinePlayer = player.getPlayer();
        return onlinePlayer != null ? onlinePlayer.getWorld() : null;
    }

}
