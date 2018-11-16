package com.ninjaguild.dragoneggdrop.utils.runnables;

import java.util.List;
import java.util.stream.Collectors;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.api.BattleState;
import com.ninjaguild.dragoneggdrop.api.BattleStateChangeEvent;
import com.ninjaguild.dragoneggdrop.api.PortalCrystal;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;
import com.ninjaguild.dragoneggdrop.versions.DragonBattle;
import com.ninjaguild.dragoneggdrop.versions.NMSAbstract;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents a BukkitRunnable that handles the respawning of the
 * Ender Dragon after it has been slain.
 */
public class RespawnRunnable extends BukkitRunnable {

	private final DragonEggDrop plugin;
	private final EndWorldWrapper worldWrapper;
	private final NMSAbstract nmsAbstract;
	
	private final DragonBattle dragonBattle;
	private final EnderDragon dragon;
	
	private final boolean announceRespawn;
	private final List<String> announceMessages;
	
	private int currentCrystal = 0, currentMessage = 0;
	private int secondsUntilRespawn;
	
	/**
	 * Construct a new RespawnRunnable object.
	 * 
	 * @param plugin an instance of the DragonEggDrop plugin
	 * @param world the world to execute a respawn
	 * @param respawnTime the time in seconds until the respawn is executed
	 */
	public RespawnRunnable(DragonEggDrop plugin, World world, int respawnTime) {
		this.plugin = plugin;
		this.worldWrapper = plugin.getDEDManager().getWorldWrapper(world);
		this.secondsUntilRespawn = respawnTime;
		this.nmsAbstract = plugin.getNMSAbstract();
		
		this.dragonBattle = nmsAbstract.getEnderDragonBattleFromWorld(world);
		this.dragon = dragonBattle.getEnderDragon();
		
		this.announceMessages = plugin.getConfig().getStringList("announce-messages").stream()
				.map(s -> ChatColor.translateAlternateColorCodes('&', s))
				.collect(Collectors.toList());
		this.announceRespawn = announceMessages.size() > 0;
		
		// Event call
		BattleStateChangeEvent bscEventCrystals = new BattleStateChangeEvent(dragonBattle, dragon, BattleState.DRAGON_DEAD, BattleState.CRYSTALS_SPAWNING);
		Bukkit.getPluginManager().callEvent(bscEventCrystals);
	}

	@Override
	public void run() {
		if (this.secondsUntilRespawn > 0) {
			if (announceRespawn) {
				if (this.currentMessage >= announceMessages.size()) this.currentMessage = 0;
				
				// Show actionbar messages
				String message = announceMessages.get(currentMessage++)
						.replace("%time%", String.valueOf(secondsUntilRespawn))
						.replace("%formatted-time%", this.getFormattedTime(secondsUntilRespawn));
				plugin.getNMSAbstract().broadcastActionBar(message, worldWrapper.getWorld());
			}

			this.secondsUntilRespawn--;
			return;
		}
		
		// Only respawn if a Player is in the World
		World world = this.worldWrapper.getWorld();
		if (world.getPlayers().size() <= 0) return;
		
		// Start respawn process
		PortalCrystal crystalPos = PortalCrystal.values()[currentCrystal++];
		Location crystalLocation = crystalPos.getRelativeToPortal(world);
		World crystalWorld = crystalLocation.getWorld();
		
		Chunk crystalChunk = crystalWorld.getChunkAt(crystalLocation);
		if (!crystalChunk.isLoaded())
			crystalChunk.load();
		
		// Remove any existing crystal
		EnderCrystal existingCrystal = crystalPos.get(world);
		if (existingCrystal != null) existingCrystal.remove();
		
		crystalPos.spawn(world);
		crystalWorld.createExplosion(crystalLocation.getX(), crystalLocation.getY(), crystalLocation.getZ(), 0F, false, false);
		crystalWorld.spawnParticle(Particle.EXPLOSION_HUGE, crystalLocation, 0);
		
		// All crystals respawned
		if (currentCrystal >= 4) {
			
			// If dragon already exists, cancel the respawn process
			if (crystalWorld.getEntitiesByClass(EnderDragon.class).size() >= 1) {
				plugin.getLogger().warning("An EnderDragon is already present in world " + crystalWorld.getName() + ". Dragon respawn cancelled");
				this.nmsAbstract.broadcastActionBar(ChatColor.RED + "Dragon respawn abandonned! Dragon already exists! Slay it!", crystalWorld);
				
				// Destroy all crystals
				for (PortalCrystal portalCrystal : PortalCrystal.values()) {
					Location location = portalCrystal.getRelativeToPortal(world);
					
					portalCrystal.get(world).remove();
					
					crystalWorld.getPlayers().forEach(p -> p.playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 1000, 1));
					crystalWorld.createExplosion(location.getX(), location.getY(), location.getZ(), 0F, false, false);
				}
				
				this.cancel();
				return;
			}
			
			this.dragonBattle.respawnEnderDragon();
			RespawnSafeguardRunnable.newTimeout(plugin, worldWrapper.getWorld(), dragonBattle);
			
			BattleStateChangeEvent bscEventRespawning = new BattleStateChangeEvent(dragonBattle, dragon, BattleState.CRYSTALS_SPAWNING, BattleState.DRAGON_RESPAWNING);
			Bukkit.getPluginManager().callEvent(bscEventRespawning);
			
			this.worldWrapper.stopRespawn();
			this.cancel();
		}
	}

	/**
	 * Get the amount of time remaining (in seconds) until the dragon respawns.
	 * 
	 * @return the remaining time
	 */
	public int getSecondsUntilRespawn() {
		return secondsUntilRespawn;
	}
	
	private String getFormattedTime(int timeInSeconds) {
		StringBuilder resultTime = new StringBuilder();
		
		if (timeInSeconds >= 3600) { // Hours
			int hours = (int) (Math.floor(timeInSeconds / 3600));
			resultTime.append(hours + " hours, ");
			timeInSeconds -= hours * 3600;
		}
		if (timeInSeconds >= 60) { // Minutes
			int minutes = (int) (Math.floor(timeInSeconds / 60));
			resultTime.append(minutes + " minutes, ");
			timeInSeconds -= minutes * 60;
		}
		if (timeInSeconds >= 1) resultTime.append(timeInSeconds + " seconds, ");
		
		return resultTime.substring(0, resultTime.length() - (resultTime.length() < 2 ? 0 : 2));
	}
}
