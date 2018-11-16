package com.ninjaguild.dragoneggdrop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
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
import com.ninjaguild.dragoneggdrop.commands.DragonSpawnCmd;
import com.ninjaguild.dragoneggdrop.commands.DragonTemplateCmd;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.events.DragonLifeListeners;
import com.ninjaguild.dragoneggdrop.events.LootListeners;
import com.ninjaguild.dragoneggdrop.events.PortalClickListener;
import com.ninjaguild.dragoneggdrop.events.RespawnListeners;
import com.ninjaguild.dragoneggdrop.management.DEDManager;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;
import com.ninjaguild.dragoneggdrop.utils.ConfigUtil;
import com.ninjaguild.dragoneggdrop.versions.NMSAbstract;
import com.ninjaguild.dragoneggdrop.versions.v1_13_R2.NMSAbstract1_13_R2;

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
 * @author Parker Hawke - 2008Choco (Maintainer)
 */
public class DragonEggDrop extends JavaPlugin {

	private static final String CHAT_PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "DED" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final int RESOURCE_ID = 35570;
	private static final String SPIGET_LINK = "https://api.spiget.org/v2/resources/" + RESOURCE_ID + "/versions/latest";

	private boolean newVersionAvailable = false;
	private String newVersion;

	private DEDManager dedManager;
	private NMSAbstract nmsAbstract;

	private BukkitTask updateTask;

	private File tempDataFile;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();

		// Update configuration version
		ConfigUtil cu = new ConfigUtil(this);
		cu.updateConfig(this.getConfig().getInt("version"));

		// Setup version abstraction
		if (!this.setupNMSAbstract()) {
			this.getLogger().severe("THE CURRENT SERVER VERSION IS NOT SUPPORTED. BOTHER THE MAINTAINER");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		// Load default templates
		if (DragonTemplate.DRAGONS_FOLDER.mkdirs()) {
			this.saveDefaultTemplates();
		}

		this.dedManager = new DEDManager(this);

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
		this.registerCommand("dragonspawn", new DragonSpawnCmd(this));
		this.registerCommand("dragontemplate", new DragonTemplateCmd(this));

		// Update check
		if (this.getConfig().getBoolean("perform-update-checks", true)) {
			this.updateTask = new BukkitRunnable() {
				@Override
				public void run() {
					boolean previousState = newVersionAvailable;
					doVersionCheck();

					// New version found
					if (previousState != newVersionAvailable) {
						Bukkit.getOnlinePlayers().stream()
							.filter(Player::isOp)
							.forEach(p -> sendMessage(p, ChatColor.GRAY + "A new version is available for download (Version " + newVersion + "). "));
					}
				}
			}.runTaskTimerAsynchronously(this, 0, 36000);
		}
	}

	@Override
	public void onDisable() {
		if (this.updateTask != null) {
			this.updateTask.cancel();
		}

		if (!tempDataFile.exists()) {
			try {
				this.tempDataFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.writeTempData();
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
	 * Get the current implementation of the NMSAbstract interface.
	 *
	 * @return the NMSAbstract interface
	 */
	public NMSAbstract getNMSAbstract() {
		return nmsAbstract;
	}

	/**
	 * Get whether there is a new version available and ready for
	 * download or not.
	 *
	 * @return true if available
	 */
	public boolean isNewVersionAvailable() {
		return newVersionAvailable;
	}

	/**
	 * Get the version of the available update (if one exists).
	 *
	 * @see #isNewVersionAvailable()
	 * @return the new version
	 */
	public String getNewVersion() {
		return newVersion;
	}

	private void saveDefaultTemplates() {
		try (JarFile jar = new JarFile(getFile())){
			Enumeration<JarEntry> entries = jar.entries();

			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();

				if (!name.startsWith("dragons/")) continue;

				this.saveResource(name, false);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeTempData() {
		JsonObject root = new JsonObject();

		for (EndWorldWrapper world : dedManager.getWorldWrappers()) {
			if (!world.isRespawnInProgress() && world.getActiveBattle() == null) return;

			JsonObject jsonWorld = new JsonObject();

			if (world.isRespawnInProgress()) jsonWorld.addProperty("respawnTime", world.getTimeUntilRespawn());
			if (world.getActiveBattle() != null) jsonWorld.addProperty("activeTemplate", world.getActiveBattle().getIdentifier());

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
			if (root == null) return;

			for (Entry<String, JsonElement> entry : root.entrySet()) {
				World world = Bukkit.getWorld(entry.getKey());
				if (world == null) return;

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
					if (template == null) return;

					wrapper.setActiveBattle(template);
					template.applyToBattle(nmsAbstract, Iterables.get(dragons, 0), nmsAbstract.getEnderDragonBattleFromWorld(world));
				}
			}
		} catch (IOException | JsonParseException e) {
			e.printStackTrace();
		}
	}

	private void registerCommand(String command, CommandExecutor executor, TabCompleter tabCompleter) {
		if (tabCompleter == null && !(executor instanceof TabCompleter))
			throw new UnsupportedOperationException();

		PluginCommand commandObject = this.getCommand(command);
		if (commandObject == null) return;

		commandObject.setExecutor(executor);
		commandObject.setTabCompleter(tabCompleter != null ? tabCompleter : (TabCompleter) executor);
	}

	private void registerCommand(String command, CommandExecutor executor) {
		this.registerCommand(command, executor, null);
	}

	private void doVersionCheck() {
		new BukkitRunnable() {
			@Override
			public void run() {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(SPIGET_LINK).openStream()))){
					JsonObject object = GSON.fromJson(reader, JsonObject.class);
					String currentVersion = getDescription().getVersion();
					String recentVersion = object.get("name").getAsString();

					if (!currentVersion.equals(recentVersion)) {
						getLogger().info("New version available. Your Version = " + currentVersion + ". New Version = " + recentVersion);
						newVersionAvailable = true;
						newVersion = recentVersion;
					}
				} catch (IOException e) {
					getLogger().info("Could not check for a new version. Perhaps the website is down?");
				}
			}
		}.runTaskAsynchronously(this);
	}

	private final boolean setupNMSAbstract(){
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

		switch (version) {
			case "v1_13_R2": nmsAbstract = new NMSAbstract1_13_R2(); break; // 1.13.1 and 1.13.2
		}

        return nmsAbstract != null;
	}
}