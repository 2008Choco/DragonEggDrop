package wtf.choco.dragoneggdrop;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import wtf.choco.commons.util.UpdateChecker;
import wtf.choco.commons.util.UpdateChecker.UpdateReason;
import wtf.choco.dragoneggdrop.commands.CommandDragonEggDrop;
import wtf.choco.dragoneggdrop.commands.CommandDragonParticle;
import wtf.choco.dragoneggdrop.commands.CommandDragonRespawn;
import wtf.choco.dragoneggdrop.commands.CommandDragonTemplate;
import wtf.choco.dragoneggdrop.dragon.DamageHistory;
import wtf.choco.dragoneggdrop.dragon.loot.DragonLootTable;
import wtf.choco.dragoneggdrop.listeners.DamageHistoryListener;
import wtf.choco.dragoneggdrop.listeners.DragonLifeListeners;
import wtf.choco.dragoneggdrop.listeners.KillCommandDeprecationListener;
import wtf.choco.dragoneggdrop.listeners.LootListeners;
import wtf.choco.dragoneggdrop.listeners.PortalClickListener;
import wtf.choco.dragoneggdrop.listeners.RespawnListeners;
import wtf.choco.dragoneggdrop.particle.ParticleShapeDefinition;
import wtf.choco.dragoneggdrop.particle.condition.ConditionFactory;
import wtf.choco.dragoneggdrop.placeholder.DragonEggDropPlaceholders;
import wtf.choco.dragoneggdrop.registry.DragonTemplateRegistry;
import wtf.choco.dragoneggdrop.registry.HashRegistry;
import wtf.choco.dragoneggdrop.registry.Registry;
import wtf.choco.dragoneggdrop.utils.DEDConstants;
import wtf.choco.dragoneggdrop.utils.DataFileUtils;
import wtf.choco.dragoneggdrop.world.EndWorldWrapper;

/**
 * DragonEggDrop, reward your players with a dragon egg/loot chest after every ender
 * dragon battle, in grand fashion!
 *
 * @author NinjaStix
 * @author Parker Hawke - Choco (Maintainer)
 */
public final class DragonEggDrop extends JavaPlugin {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String CHAT_PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "DED" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;

    private static DragonEggDrop instance;

    private DragonTemplateRegistry dragonTemplateRegistry = new DragonTemplateRegistry();
    private Registry<@NotNull DragonLootTable> lootTableRegistry = new HashRegistry<>();
    private Registry<@NotNull ParticleShapeDefinition> particleShapeDefinitionRegistry = new HashRegistry<>();

    private BukkitTask updateTask;
    private File tempDataFile;

    private File dragonTemplateDirectory, lootTableDirectory, particleDirectory;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();

        // Load default templates and loot tables
        if ((dragonTemplateDirectory = new File(getDataFolder(), "dragons")).mkdirs()) {
            this.saveDefaultDirectory("dragons");
        }
        if ((lootTableDirectory = new File(getDataFolder(), "loot_tables")).mkdirs()) {
            this.saveDefaultDirectory("loot_tables");
        }
        if ((particleDirectory = new File(getDataFolder(), "particles")).mkdirs()) {
            this.saveDefaultDirectory("particles");
        }

        // Load all necessary data into memory
        DataFileUtils.reloadInMemoryData(this, true);

        // Load temp data (reload support)
        this.tempDataFile = new File(getDataFolder(), "tempData.json");
        if (tempDataFile.exists()) {
            this.getLogger().info("Reading temporary data from previous server session...");
            DataFileUtils.readTempData(this, tempDataFile);
            this.tempDataFile.delete();
        }

        // Register events
        this.getLogger().info("Registering event listeners");
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new DamageHistoryListener(this), this);
        manager.registerEvents(new DragonLifeListeners(this), this);
        manager.registerEvents(new KillCommandDeprecationListener(this), this);
        manager.registerEvents(new LootListeners(), this);
        manager.registerEvents(new PortalClickListener(this), this);
        manager.registerEvents(new RespawnListeners(this), this);

        // Register commands
        this.getLogger().info("Registering command executors and tab completion");
        this.registerCommandSafely("dragoneggdrop", new CommandDragonEggDrop(this));
        this.registerCommandSafely("dragonrespawn", new CommandDragonRespawn(this));
        this.registerCommandSafely("dragontemplate", new CommandDragonTemplate(this));
        this.registerCommandSafely("dragonparticle", new CommandDragonParticle(this));

        // Register external placeholder functionality
        DragonEggDropPlaceholders.registerPlaceholders(this, manager);

        // Enable metrics
        if (getConfig().getBoolean(DEDConstants.CONFIG_METRICS, true)) {
            new Metrics(this, 7697); // https://bstats.org/what-is-my-plugin-id
            this.getLogger().info("Successfully enabled metrics. Thanks for keeping these enabled!");
        }

        // Update check
        UpdateChecker.init(this, 35570);
        if (getConfig().getBoolean(DEDConstants.CONFIG_PERFORM_UPDATE_CHECKS, true)) {
            this.updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                UpdateChecker.get().requestUpdateCheck().whenComplete((result, exception) -> {
                    if (result.requiresUpdate()) {
                        this.getLogger().info(String.format("An update is available! DragonEggDrop %s may be downloaded on SpigotMC", result.getNewestVersion()));
                        Bukkit.getOnlinePlayers().stream().filter(Player::isOp).forEach(p -> sendMessage(p, "A new version is available for download (Version " + result.getNewestVersion() + ")"));
                        return;
                    }

                    UpdateReason reason = result.getReason();
                    if (reason == UpdateReason.UP_TO_DATE) {
                        this.getLogger().info(String.format("Your version of DragonEggDrop (%s) is up to date!", result.getNewestVersion()));
                    }
                    else if (reason == UpdateReason.UNRELEASED_VERSION) {
                        getLogger().info(String.format("Your version of DragonEggDrop (%s) is more recent than the one publicly available. Are you on a development build?", result.getNewestVersion()));
                    }
                    else {
                        getLogger().warning("Could not check for a new version of DragonEggDrop. Reason: " + reason);
                    }
                });
            }, 0L, 432000); // 6 hours
        }
    }

    @Override
    public void onDisable() {
        if (updateTask != null) {
            this.updateTask.cancel();
        }

        try {
            DataFileUtils.writeTempData(tempDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Clear the world wrappers
        EndWorldWrapper.getAll().forEach(EndWorldWrapper::stopRespawn);
        EndWorldWrapper.clear();

        this.particleShapeDefinitionRegistry.clear();
        this.lootTableRegistry.clear();
        this.dragonTemplateRegistry.clear();

        ConditionFactory.clear();
        DamageHistory.clearGlobalDamageHistory();
    }

    /**
     * Get the dragon template registry.
     *
     * @return the dragon template registry.
     */
    @NotNull
    public DragonTemplateRegistry getDragonTemplateRegistry() {
        return dragonTemplateRegistry;
    }

    /**
     * Get the loot table registry for all dragon loot tables.
     *
     * @return the loot table registry
     */
    @NotNull
    public Registry<@NotNull DragonLootTable> getLootTableRegistry() {
        return lootTableRegistry;
    }

    /**
     * Get the particle shape definition registry.
     *
     * @return the particle shapde definition registry
     */
    @NotNull
    public Registry<@NotNull ParticleShapeDefinition> getParticleShapeDefinitionRegistry() {
        return particleShapeDefinitionRegistry;
    }

    /**
     * Get the directory in which dragon templates are located.
     *
     * @return the dragon templates directory
     */
    @NotNull
    public File getDragonTemplateDirectory() {
        return dragonTemplateDirectory;
    }

    /**
     * Get the directory in which loot tables are located.
     *
     * @return the loot table directory
     */
    @NotNull
    public File getLootTableDirectory() {
        return lootTableDirectory;
    }

    /**
     * Get the directory in which particle shape definitions are located.
     *
     * @return the particle shape definition directory
     */
    @NotNull
    public File getParticleDirectory() {
        return particleDirectory;
    }

    /**
     * Get the DragonEggDrop instance.
     *
     * @return this instance
     */
    @NotNull
    public static DragonEggDrop getInstance() {
        return instance;
    }

    /**
     * Send a message to a command sender with the DragonEggDrop chat prefix.
     *
     * @param sender the sender to which the message should be sent
     * @param message the message to send
     *
     * @param <T> command sender type
     */
    @NotNull
    public static <T extends CommandSender> void sendMessage(@NotNull T sender, @NotNull String message) {
        Preconditions.checkArgument(sender != null, "sender must not be null");
        Preconditions.checkArgument(message != null, "message must not be null");

        sender.sendMessage(CHAT_PREFIX + message);
    }

    private void saveDefaultDirectory(@NotNull String directory) {
        Preconditions.checkArgument(directory != null, "directory must not be null");

        try (JarFile jar = new JarFile(getFile())) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (!name.startsWith(directory + "/") || entry.isDirectory()) {
                    continue;
                }

                this.saveResource(name, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerCommandSafely(@NotNull String commandString, @NotNull CommandExecutor executor) {
        PluginCommand command = getCommand(commandString);
        if (command == null) {
            return;
        }

        command.setExecutor(executor);

        if (executor instanceof TabCompleter) {
            command.setTabCompleter((TabCompleter) executor);
        }
    }

}
