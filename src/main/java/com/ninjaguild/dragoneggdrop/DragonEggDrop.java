package com.ninjaguild.dragoneggdrop;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ninjaguild.dragoneggdrop.commands.DragonEggDropCmd;
import com.ninjaguild.dragoneggdrop.commands.DragonRespawnCmd;
import com.ninjaguild.dragoneggdrop.commands.DragonTemplateCmd;
import com.ninjaguild.dragoneggdrop.dragon.DamageHistory;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.dragon.loot.DragonLootTable;
import com.ninjaguild.dragoneggdrop.dragon.loot.DragonLootTableRegistry;
import com.ninjaguild.dragoneggdrop.listeners.DamageHistoryListener;
import com.ninjaguild.dragoneggdrop.listeners.DragonLifeListeners;
import com.ninjaguild.dragoneggdrop.listeners.LootListeners;
import com.ninjaguild.dragoneggdrop.listeners.PortalClickListener;
import com.ninjaguild.dragoneggdrop.listeners.RespawnListeners;
import com.ninjaguild.dragoneggdrop.placeholder.DragonEggDropPlaceholders;
import com.ninjaguild.dragoneggdrop.utils.TempDataUtils;
import com.ninjaguild.dragoneggdrop.utils.UpdateChecker;
import com.ninjaguild.dragoneggdrop.utils.UpdateChecker.UpdateReason;
import com.ninjaguild.dragoneggdrop.world.EndWorldWrapper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * DragonEggDrop, reward your players with a dragon egg/loot chest after every ender
 * dragon battle, in grand fashion!
 *
 * @author NinjaStix
 * @author Parker Hawke - Choco (Maintainer)
 */
public class DragonEggDrop extends JavaPlugin {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String CHAT_PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "DED" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;

    private static DragonEggDrop instance;

    private DragonLootTableRegistry lootTableRegistry;

    private BukkitTask updateTask;
    private File tempDataFile;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();

        // Load default templates and loot tables
        if (DragonTemplate.DRAGONS_FOLDER.mkdirs()) {
            this.saveDefaultDirectory("dragons");
        }
        if (DragonLootTable.LOOT_TABLES_FOLDER.mkdirs()) {
            this.saveDefaultDirectory("loot_tables");
        }

        this.lootTableRegistry = new DragonLootTableRegistry();
        this.lootTableRegistry.reloadDragonLootTables();

        DragonTemplate.reload();

        // Load temp data (reload support)
        this.tempDataFile = new File(getDataFolder(), "tempData.json");
        if (tempDataFile.exists()) {
            TempDataUtils.readTempData(this, tempDataFile);
            this.tempDataFile.delete();
        }

        // Register events
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new DragonLifeListeners(this), this);
        manager.registerEvents(new LootListeners(), this);
        manager.registerEvents(new RespawnListeners(this), this);
        manager.registerEvents(new PortalClickListener(), this);
        manager.registerEvents(new DamageHistoryListener(), this);

        // Register commands
        this.registerCommand("dragoneggdrop", new DragonEggDropCmd(this));
        this.registerCommand("dragonrespawn", new DragonRespawnCmd(this));
        this.registerCommand("dragontemplate", new DragonTemplateCmd());

        // Register external placeholder functionality
        DragonEggDropPlaceholders.registerPlaceholders(this, manager);

        // Update check
        UpdateChecker.init(this, 35570);
        if (getConfig().getBoolean("perform-update-checks", true)) {
            this.updateTask = new BukkitRunnable() {
                @Override
                public void run() {
                    UpdateChecker.get().requestUpdateCheck().whenComplete((result, exception) -> {
                        if (result.requiresUpdate()) {
                            getLogger().info(String.format("An update is available! DragonEggDrop %s may be downloaded on SpigotMC", result.getNewestVersion()));
                            Bukkit.getOnlinePlayers().stream().filter(Player::isOp).forEach(p -> sendMessage(p, "A new version is available for download (Version " + result.getNewestVersion() + ")"));
                            return;
                        }

                        UpdateReason reason = result.getReason();
                        if (reason == UpdateReason.UP_TO_DATE) {
                            getLogger().info(String.format("Your version of DragonEggDrop (%s) is up to date!", result.getNewestVersion()));
                        }
                        else if (reason == UpdateReason.UNRELEASED_VERSION) {
                            getLogger().info(String.format("Your version of DragonEggDrop (%s) is more recent than the one publicly available. Are you on a development build?", result.getNewestVersion()));
                        }
                        else {
                            getLogger().warning("Could not check for a new version of DragonEggDrop. Reason: " + reason);
                        }
                    });
                }
            }.runTaskTimerAsynchronously(this, 0, 432000); // 6 hours
        }
    }

    @Override
    public void onDisable() {
        if (updateTask != null) {
            this.updateTask.cancel();
        }

        try {
            TempDataUtils.writeTempData(tempDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.lootTableRegistry.clear();
        DragonTemplate.clear();
        DamageHistory.clearGlobalDamageHistory();

        // Clear the world wrappers
        EndWorldWrapper.getAll().forEach(EndWorldWrapper::stopRespawn);
        EndWorldWrapper.clear();
    }

    /**
     * Get the loot table registry for all dragon loot tables.
     *
     * @return the loot table registry
     */
    public DragonLootTableRegistry getLootTableRegistry() {
        return lootTableRegistry;
    }

    /**
     * Get the DragonEggDrop instance.
     *
     * @return this instance
     */
    public static DragonEggDrop getInstance() {
        return instance;
    }

    /**
     * Send a message to a command sender with the DragonEggDrop chat prefix.
     *
     * @param sender the sender to which the message should be sent
     * @param message the message to send
     */
    public static <T extends CommandSender> void sendMessage(T sender, String message) {
        sender.sendMessage(CHAT_PREFIX + message);
    }

    private void saveDefaultDirectory(String directory) {
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

    private void registerCommand(String command, CommandExecutor executor, TabCompleter tabCompleter) {
        PluginCommand commandObject = this.getCommand(command);
        if (commandObject == null) {
            return;
        }

        commandObject.setExecutor(executor);
        commandObject.setTabCompleter(tabCompleter);
    }

    private void registerCommand(String command, TabExecutor executor) {
        this.registerCommand(command, executor, executor);
    }

}
