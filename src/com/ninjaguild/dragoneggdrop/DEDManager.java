package com.ninjaguild.dragoneggdrop;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEnderDragon;
import org.bukkit.entity.EnderDragon;
import org.bukkit.scheduler.BukkitTask;

import net.minecraft.server.v1_9_R2.BossBattleServer;
import net.minecraft.server.v1_9_R2.ChatMessage;
import net.minecraft.server.v1_9_R2.EnderDragonBattle;
import net.minecraft.server.v1_9_R2.PacketPlayOutBoss;
import net.minecraft.server.v1_9_R2.WorldProviderTheEnd;

public class DEDManager {

	private final DragonEggDrop plugin;
	
	private List<String> dragonNames = null;
	private LootManager lootMan = null;
	
	private BukkitTask respawnTask = null;
	
	private final int joinDelay;
	private final int deathDelay;
	
	public DEDManager(final DragonEggDrop plugin) {
		this.plugin = plugin;
		
		dragonNames = plugin.getConfig().getStringList("dragon-names");
        setDragonBossBarTitle();
        
        lootMan = new LootManager(plugin);
        
		joinDelay = plugin.getConfig().getInt("join-respawn-delay", 60);//seconds
		deathDelay = plugin.getConfig().getInt("death-respawn-delay", 300);//seconds
	}
	
	private void setDragonBossBarTitle() {
		for (World world : plugin.getServer().getWorlds()) {
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
	
	protected LootManager getLootManager() {
		return lootMan;
	}
	
	protected void startRespawn(Location eggLoc, RespawnType type) {
		if (respawnTask == null || 
				plugin.getServer().getScheduler().isCurrentlyRunning(respawnTask.getTaskId()) || 
				plugin.getServer().getScheduler().isQueued(respawnTask.getTaskId())) {
			int respawnDelay = ((type == RespawnType.JOIN) ? joinDelay : deathDelay) * 20;
			respawnTask = Bukkit.getScheduler().runTaskLater(plugin, new RespawnRunnable(plugin, eggLoc), respawnDelay);
		}
	}
	
	protected void stopRespawn() {
		if (respawnTask != null) {
			respawnTask.cancel();
			respawnTask = null;
		}
	}
	
	protected enum RespawnType {
		JOIN,
		DEATH
	}
	
}
