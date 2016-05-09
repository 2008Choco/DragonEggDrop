package com.ninjaguild.dragoneggdrop;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_9_R1.BossBattleServer;
import net.minecraft.server.v1_9_R1.ChatMessage;
import net.minecraft.server.v1_9_R1.EnderDragonBattle;
import net.minecraft.server.v1_9_R1.PacketPlayOutBoss;
import net.minecraft.server.v1_9_R1.WorldProviderTheEnd;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEnderDragon;

public class DragonEggDrop extends JavaPlugin implements Listener {

	private PluginDescriptionFile pdf = null;
	private List<String> dragonNames = null;

	public void onEnable() {
		saveDefaultConfig();
		pdf = getDescription();

		//update config version to match plugin
		String configVersion = getConfig().getString("version").trim();
		if (!configVersion.equals(pdf.getVersion().trim())) {
			ConfigUtil cu = new ConfigUtil(this);
			cu.updateConfig(pdf.getVersion());
		}

		try {
			Particle.valueOf(getConfig().getString("particle-type", "FLAME").toUpperCase());
		} catch (IllegalArgumentException ex) {
			getLogger().log(Level.WARNING, "INVALID PARTICLE TYPE SPECIFIED! DISABLING...");
			getServer().getPluginManager().disablePlugin(this);
			getLogger().log(Level.INFO, "PLUGIN DISABLED");
			return;
		}

		getServer().getPluginManager().registerEvents(this, this);
		getCommand("dragoneggdrop").setExecutor(this);

		dragonNames = getConfig().getStringList("dragon-names");
        setDragonBossBarTitle();
	}

	public void onDisable() {
		//
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
			battleServer.title = new ChatMessage(ChatColor.translateAlternateColorCodes('&', title), new Object[0]);
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
		return ((CraftEnderDragon)dragon).getHandle().cU();
	}
	
	protected final List<String> getDragonNames() {
		return dragonNames;
	}
	
	protected PluginDescriptionFile getDescriptionFile() {
		return pdf;
	}

}
