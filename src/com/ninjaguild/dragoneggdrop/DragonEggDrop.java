package com.ninjaguild.dragoneggdrop;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.EnderDragon;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_9_R2.BossBattleServer;
import net.minecraft.server.v1_9_R2.ChatMessage;
import net.minecraft.server.v1_9_R2.EnderDragonBattle;
import net.minecraft.server.v1_9_R2.PacketPlayOutBoss;
import net.minecraft.server.v1_9_R2.WorldProviderTheEnd;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEnderDragon;

public class DragonEggDrop extends JavaPlugin {

	private PluginDescriptionFile pdf = null;
	private List<String> dragonNames = null;
	
	private LootManager lootMan = null;
	
	private String chatPrefix = null;

	public void onEnable() {
		saveDefaultConfig();
		pdf = getDescription();

		//update config version
		String currentVersion = getConfig().getString("version").trim();
		ConfigUtil cu = new ConfigUtil(this);
		cu.updateConfig(currentVersion);

		try {
			Particle.valueOf(getConfig().getString("particle-type", "FLAME").toUpperCase());
		} catch (IllegalArgumentException ex) {
			getLogger().log(Level.WARNING, "INVALID PARTICLE TYPE SPECIFIED! DISABLING...");
			getServer().getPluginManager().disablePlugin(this);
			getLogger().log(Level.INFO, "PLUGIN DISABLED");
			return;
		}

		ConfigurationSerialization.registerClass(LootEntry.class);
		
		getServer().getPluginManager().registerEvents(new Events(this), this);
		getCommand("dragoneggdrop").setExecutor(new Commands(this));

		dragonNames = getConfig().getStringList("dragon-names");
        setDragonBossBarTitle();
        
        lootMan = new LootManager(this);
        
        chatPrefix = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "DED" + ChatColor.DARK_GRAY + "] ";
	}

	public void onDisable() {
		//
	}
	
	protected String getChatPrefix() {
		return chatPrefix;
	}
	
	private void setDragonBossBarTitle() {
		for (World world : getServer().getWorlds()) {
			if (world.getEnvironment() == Environment.THE_END) {
				Collection<EnderDragon> dragons = world.getEntitiesByClass(EnderDragon.class);
				if (!dragons.isEmpty()) {
					String dragonName = dragons.iterator().next().getCustomName();
					if (dragonName != null && !dragonName.isEmpty()) {
						setDragonBossBarTitle(dragonName, getEnderDragonBattleFromWorld(world));
					}
				}
			}
		}
	}

	protected void setDragonBossBarTitle(String title, EnderDragonBattle battle) {
		try {
			Field f = EnderDragonBattle.class.getDeclaredField("c");
			f.setAccessible(true);
			BossBattleServer battleServer = (BossBattleServer)f.get(battle);
			battleServer.title = new ChatMessage(title, new Object[0]);
			battleServer.sendUpdate(PacketPlayOutBoss.Action.UPDATE_NAME);
			f.setAccessible(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected EnderDragonBattle getEnderDragonBattleFromWorld(World world) {
		return ((WorldProviderTheEnd)((CraftWorld)world).getHandle().worldProvider).s();
	}

	protected EnderDragonBattle getEnderDragonBattleFromDragon(EnderDragon dragon) {
		return ((CraftEnderDragon)dragon).getHandle().cV();
	}
	
	protected List<String> getDragonNames() {
		return dragonNames;
	}
	
	protected PluginDescriptionFile getDescriptionFile() {
		return pdf;
	}
	
	protected LootManager getLootManager() {
		return lootMan;
	}

}
