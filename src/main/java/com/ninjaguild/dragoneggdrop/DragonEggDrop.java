package com.ninjaguild.dragoneggdrop;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonWriter;
import com.ninjaguild.dragoneggdrop.commands.DragonEggDropCmd;
import com.ninjaguild.dragoneggdrop.commands.DragonRespawnCmd;
import com.ninjaguild.dragoneggdrop.commands.DragonTemplateCmd;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.dragon.loot.DragonLootTable;
import com.ninjaguild.dragoneggdrop.dragon.loot.DragonLootTableRegistry;
import com.ninjaguild.dragoneggdrop.events.DragonLifeListeners;
import com.ninjaguild.dragoneggdrop.events.LootListeners;
import com.ninjaguild.dragoneggdrop.events.PortalClickListener;
import com.ninjaguild.dragoneggdrop.events.RespawnListeners;
import com.ninjaguild.dragoneggdrop.management.DEDManager;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;
import com.ninjaguild.dragoneggdrop.nms.NMSUtils;
import com.ninjaguild.dragoneggdrop.placeholder.DragonEggDropPlaceholders;
import com.ninjaguild.dragoneggdrop.utils.UpdateChecker;
import com.ninjaguild.dragoneggdrop.utils.UpdateChecker.UpdateReason;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * DragonEggDrop, reward your players with a dragon egg/loot chest
 * after every ender dragon battle, in grand fashion!
 *
 * @author NinjaStix
 * @author Parker Hawke - Choco (Maintainer)
 */
public class DragonEggDrop extends JavaPlugin {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String CHAT_PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "DED" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;

    private static DragonEggDrop instance;

    private DEDManager dedManager;
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

        this.dedManager = new DEDManager(this);
        this.dedManager.reloadDragonTemplates();

        // Load temp data (reload support)
        this.tempDataFile = new File(getDataFolder(), "tempData.json");
        if (tempDataFile.exists()) {
            this.readTempData();
            this.tempDataFile.delete();
        }

        // Register events
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new DragonLifeListeners(this), this);
        manager.registerEvents(new LootListeners(this), this);
        manager.registerEvents(new RespawnListeners(this), this);
        manager.registerEvents(new PortalClickListener(this), this);

        // Register commands
        this.registerCommand("dragoneggdrop", new DragonEggDropCmd(this));
        this.registerCommand("dragonrespawn", new DragonRespawnCmd(this));
        this.registerCommand("dragontemplate", new DragonTemplateCmd(this));

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
                            Bukkit.getOnlinePlayers().stream().filter(Player::isOp)
                                .forEach(p -> sendMessage(p, "A new version is available for download (Version " + result.getNewestVersion() + ")"));
                            return;
                        }

                        UpdateReason reason = result.getReason();
                        if (reason == UpdateReason.UP_TO_DATE) {
                            getLogger().info(String.format("Your version of DragonEggDrop (%s) is up to date!", result.getNewestVersion()));
                        } else if (reason == UpdateReason.UNRELEASED_VERSION) {
                            getLogger().info(String.format("Your version of DragonEggDrop (%s) is more recent than the one publicly available. Are you on a development build?", result.getNewestVersion()));
                        } else {
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
            this.writeTempData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.lootTableRegistry.clear();
        this.dedManager.clearTemplates();

        // Clear the world wrappers
        this.dedManager.getWorldWrappers().forEach(EndWorldWrapper::stopRespawn);
        this.dedManager.clearWorldWrappers();
    }

    /**
     * Send a message to a command sender with the DragonEggDrop chat prefix.
     *
     * @param sender the sender to send the message to
     * @param message the message to send
     */
    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(CHAT_PREFIX + message);
    }

    /**
     * Get the main DEDManager instance.
     *
     * @return the DEDManager instance
     */
    public DEDManager getDEDManager() {
        return dedManager;
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

    private void saveDefaultDirectory(String directory) {
        try (JarFile jar = new JarFile(getFile())){
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (!name.startsWith(directory + "/")) {
                    continue;
                }

                this.saveResource(name, false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeTempData() throws IOException {
        if (!tempDataFile.createNewFile()) {
            return;
        }

        JsonObject root = new JsonObject();

        for (EndWorldWrapper world : dedManager.getWorldWrappers()) {
            if (!world.isRespawnInProgress() && world.getActiveBattle() == null) {
                return;
            }

            JsonObject jsonWorld = new JsonObject();
            if (world.isRespawnInProgress()) {
                jsonWorld.addProperty("respawnTime", world.getTimeUntilRespawn());
            }
            if (world.getActiveBattle() != null) {
                jsonWorld.addProperty("activeTemplate", world.getActiveBattle().getIdentifier());
            }
            if (world.hasLootTableOverride()) {
                jsonWorld.addProperty("lootTableOverride", world.getLootTableOverride().getId());
            }

            root.add(world.getWorld().getName(), jsonWorld);
        }

        try (PrintWriter writer = new PrintWriter(tempDataFile)) {
            GSON.toJson(root, new JsonWriter(writer));
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
        }
    }

    private void readTempData() {
        try (FileReader reader = new FileReader(tempDataFile)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) {
                return;
            }

            for (Entry<String, JsonElement> entry : root.entrySet()) {
                World world = Bukkit.getWorld(entry.getKey());
                if (world == null) {
                    return;
                }

                EndWorldWrapper wrapper = dedManager.getWorldWrapper(world);
                JsonObject element = entry.getValue().getAsJsonObject();

                if (element.has("respawnTime")) {
                    if (wrapper.isRespawnInProgress()) {
                        wrapper.stopRespawn();
                    }

                    wrapper.startRespawn(element.get("respawnTime").getAsInt());
                }

                Collection<EnderDragon> dragons = world.getEntitiesByClass(EnderDragon.class);
                if (element.has("activeTemplate") && !dragons.isEmpty()) {
                    DragonTemplate template = dedManager.getTemplate(element.get("activeTemplate").getAsString());
                    if (template != null) {
                        wrapper.setActiveBattle(template);
                        template.applyToBattle(Iterables.get(dragons, 0), NMSUtils.getEnderDragonBattleFromWorld(world));
                    }
                }

                if (element.has("lootTableOverride")) {
                    DragonLootTable lootTable = lootTableRegistry.getLootTable(element.get("lootTableOverride").getAsString());
                    if (lootTable != null) {
                        wrapper.setLootTableOverride(lootTable);
                    }
                }
            }
        } catch (IOException | JsonParseException e) {
            e.printStackTrace();
        }
    }

    private void registerCommand(String command, CommandExecutor executor, TabCompleter tabCompleter) {
        if (tabCompleter == null && !(executor instanceof TabCompleter)) {
            throw new UnsupportedOperationException();
        }

        PluginCommand commandObject = this.getCommand(command);
        if (commandObject == null) {
            return;
        }

        commandObject.setExecutor(executor);
        commandObject.setTabCompleter(tabCompleter != null ? tabCompleter : (TabCompleter) executor);
    }

    private void registerCommand(String command, CommandExecutor executor) {
        this.registerCommand(command, executor, null);
    }

}
