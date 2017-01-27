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

package com.ninjaguild.dragoneggdrop.utils.runnables;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_11_R1.EnderDragonBattle;

/**
 * Represents a BukkitRunnable that handles the respawning of the 
 * Ender Dragon after it has been slain
 */
public class RespawnRunnable extends BukkitRunnable {

	private final DragonEggDrop plugin;
	private final Location eggLocation;

	/**
	 * Construct a new RespawnRunnable object
	 * 
	 * @param plugin - An instance of the DragonEggDrop plugin
	 * @param eggLocation - The location in which the egg is located
	 */
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

		// FIXME: NMS-Dependent
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
