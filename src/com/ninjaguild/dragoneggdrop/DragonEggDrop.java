/*
    DragonEggDrop
    Copyright (C) 2016  NinjaStix
    ninjastix84@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
import java.util.logging.Level;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonWriter;
import com.ninjaguild.dragoneggdrop.commands.DragonEggDropCmd;
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
import com.ninjaguild.dragoneggdrop.versions.v1_10.NMSAbstract1_10_R1;
import com.ninjaguild.dragoneggdrop.versions.v1_11.NMSAbstract1_11_R1;
import com.ninjaguild.dragoneggdrop.versions.v1_12.NMSAbstract1_12_R1;
import com.ninjaguild.dragoneggdrop.versions.v1_9.NMSAbstract1_9_R1;
import com.ninjaguild.dragoneggdrop.versions.v1_9.NMSAbstract1_9_R2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
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
	
	/* TODO for version (or 1.4.0)
	 * - Aesthetic oriented update
	 * - The portal should display a broken fire sphere-like effect surrounding it
	 * - Configuration option, "eerie-end", which covers the central end island with purple particles and smoke
	 */
	
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

		try {
			Particle.valueOf(getConfig().getString("Particles.type", "FLAME").toUpperCase());
		} catch (IllegalArgumentException ex) {
			this.getLogger().log(Level.WARNING, "INVALID PARTICLE TYPE SPECIFIED! DISABLING...");
			this.getLogger().log(Level.INFO, "PLUGIN DISABLED");
			this.getServer().getPluginManager().disablePlugin(this);
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
			this.readTempData(tempDataFile);
			this.tempDataFile.delete();
		}
		
		// Register events
		Bukkit.getPluginManager().registerEvents(new DragonLifeListeners(this), this);
		Bukkit.getPluginManager().registerEvents(new LootListeners(this), this);
		Bukkit.getPluginManager().registerEvents(new RespawnListeners(this), this);
		Bukkit.getPluginManager().registerEvents(new PortalClickListener(this), this);

		// Register commands
		this.getCommand("dragoneggdrop").setExecutor(new DragonEggDropCmd(this));
		this.getCommand("dragontemplate").setExecutor(new DragonTemplateCmd(this));
		
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
		if (this.updateTask != null)
			this.updateTask.cancel();
		
		if (!tempDataFile.exists()) {
			try {
				this.tempDataFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.writeTempData(tempDataFile);
		
		this.dedManager.clearTemplates();
		
		// Clear the world wrappers
		this.dedManager.getWorldWrappers().forEach(EndWorldWrapper::stopRespawn);
		this.dedManager.clearWorldWrappers();
	}
	
	/**
	 * Send a message to a command sender with the DragonEggDrop chat prefix
	 * 
	 * @param sender the sender to send the message to
	 * @param message the message to send
	 */
	public void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(CHAT_PREFIX + message);
	}
	
	/**
	 * Get the main DEDManager instance
	 * 
	 * @return the DEDManager instance
	 */
	public DEDManager getDEDManager() {
		return dedManager;
	}
	
	/**
	 * Get the current implementation of the NMSAbstract interface
	 * 
	 * @return the NMSAbstract interface
	 */
	public NMSAbstract getNMSAbstract() {
		return nmsAbstract;
	}
	
	/**
	 * Get whether there is a new version available and ready for
	 * download or not
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
	
	private void writeTempData(File file) {
		JsonObject root = new JsonObject();
		
		for (EndWorldWrapper world : dedManager.getWorldWrappers()) {
			if (!world.isRespawnInProgress() && world.getActiveBattle() == null) return;
			
			JsonObject jsonWorld = new JsonObject();
			
			if (world.isRespawnInProgress()) jsonWorld.addProperty("respawnTime", world.getTimeUntilRespawn());
			if (world.getActiveBattle() != null) jsonWorld.addProperty("activeTemplate", world.getActiveBattle().getFile().getName());
			
			root.add(world.getWorld().getName(), jsonWorld);
		}
		
		try (PrintWriter writer = new PrintWriter(tempDataFile)) {
			GSON.toJson(root, new JsonWriter(writer));
		} catch (JsonIOException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readTempData(File file) {
		try (FileReader reader = new FileReader(tempDataFile)) {
			JsonObject root = GSON.fromJson(reader, JsonObject.class);
			
			for (Entry<String, JsonElement> entry : root.entrySet()) {
				World world = Bukkit.getWorld(entry.getKey());
				if (world == null) return;
				
				EndWorldWrapper wrapper = dedManager.getWorldWrapper(world);
				JsonObject element = entry.getValue().getAsJsonObject();
				
				if (element.has("respawnTime")) {
					if (wrapper.isRespawnInProgress()) wrapper.stopRespawn();
					
					wrapper.startRespawn(element.get("respawnTime").getAsInt(), getConfig().getBoolean("announce-respawn", true));
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
        if (version.equals("v1_9_R1")){ // 1.9.0 - 1.9.3
        	this.nmsAbstract = new NMSAbstract1_9_R1();
        } else if (version.equals("v1_9_R2")){ // 1.9.4
        	this.nmsAbstract = new NMSAbstract1_9_R2();
        } else if (version.equals("v1_10_R1")){ // 1.10.0 - 1.10.2
        	this.nmsAbstract = new NMSAbstract1_10_R1();
        } else if (version.equals("v1_11_R1")){ // 1.11.0 - 1.11.2
        	this.nmsAbstract = new NMSAbstract1_11_R1();
        } else if (version.equals("v1_12_R1")) { // 1.12.0 - 1.12.1
        	this.nmsAbstract = new NMSAbstract1_12_R1();
        }
        
        return this.nmsAbstract != null;
	}
}