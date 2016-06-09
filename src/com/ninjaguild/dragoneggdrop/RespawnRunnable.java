package com.ninjaguild.dragoneggdrop;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_10_R1.EnderDragonBattle;

public class RespawnRunnable implements Runnable {

	private final DragonEggDrop plugin;
	private final Location eggLocation;

	public RespawnRunnable(final DragonEggDrop plugin, final Location eggLocation) {
		this.plugin = plugin;
		this.eggLocation = eggLocation;
	}

	@Override
	public void run() {
		//start respawn process
		Location[] crystalLocs = new Location[] {
				eggLocation.clone().add(3, -3, 0),
				eggLocation.clone().add(0, -3, 3),
				eggLocation.clone().add(-3, -3, 0),
				eggLocation.clone().add(0, -3, -3)
		};

		EnderDragonBattle dragonBattle = plugin.getDEDManager().getEnderDragonBattleFromWorld(eggLocation.getWorld());

		for (int i = 0; i < crystalLocs.length; i++) {
			Location cLoc = crystalLocs[i];
			new BukkitRunnable() {
				@Override
				public void run() {
					Chunk crystalChunk = eggLocation.getWorld().getChunkAt(cLoc);
					if (!crystalChunk.isLoaded()) {
						crystalChunk.load();
					}
					//kill any existing entities at this location
					for (Entity ent : cLoc.getWorld().getNearbyEntities(cLoc, 1, 1, 1)) {
						ent.remove();
					}
					EnderCrystal crystal = (EnderCrystal)eggLocation.getWorld().spawnEntity(cLoc, EntityType.ENDER_CRYSTAL);
					crystal.setShowingBottom(false);
					crystal.setInvulnerable(true);

					cLoc.getWorld().createExplosion(cLoc.getX(), cLoc.getY(), cLoc.getZ(), 0F, false, false);
					cLoc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, cLoc, 0);

//					dragonBattle.e();
					
					//HACKY AF!
					if (cLoc.equals(crystalLocs[crystalLocs.length - 1])) {
						dragonBattle.e();
						plugin.getDEDManager().setRespawnInProgress(true);
					}
				}

			}.runTaskLater(plugin, (i + 1) * 22);
		}
	}

}
