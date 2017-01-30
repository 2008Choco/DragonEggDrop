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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.utils.MathUtils;
import com.ninjaguild.dragoneggdrop.utils.MathUtils.MathExpression;
import com.ninjaguild.dragoneggdrop.utils.manager.DEDManager.RespawnType;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents a BukkitRunnable that handles the generation and particle display
 * of the loot after the Ender Dragon's death
 */
public class DragonDeathRunnable extends BukkitRunnable {
	
	private final Map<String, Double> variables = new HashMap<>();
	private final MathExpression xExpression, zExpression;
	
	private final DragonEggDrop plugin;
	
	private final World world;
	private final boolean placeEgg;
	
	private Particle particleType = null;
	private int particleAmount = 0;
	private double particleExtra = 0D;
	private double particleMultiplier = 1D;
	private int particleStreamInterval = 360;
	private double xOffset, yOffset, zOffset;
	private long particleInterval = 0L;
	private int lightningAmount;

	private boolean respawnDragon = false;
	private String rewardType;
	
	private Location location;
	private double animationTime = 0;
	private double theta = 0;
	private double currentY;

	/**
	 * Construct a new DragonDeathRunnable object
	 * 
	 * @param plugin - An instance of the DragonEggDrop plugin
	 * @param world - The world in which the dragon death is taking place
	 * @param prevKilled - Whether the dragon was previously killed or not
	 */
	public DragonDeathRunnable(final DragonEggDrop plugin, final World world, boolean prevKilled) {
		this.plugin = plugin;
		this.world = world;
		this.placeEgg = prevKilled;
		
		FileConfiguration config = plugin.getConfig();
		this.currentY = config.getDouble("Particles.egg-start-y");
		this.location = new Location(world, 0.5, this.currentY, 0.5);
		this.particleType = Particle.valueOf(config.getString("type", "FLAME").toUpperCase());
		this.particleAmount = config.getInt("Particles.amount", 4);
		this.particleExtra = config.getDouble("Particles.extra", 0.0D);
		this.particleMultiplier = config.getDouble("Particles.speed-multiplier", 0.0D);
		this.particleStreamInterval = 360 / Math.max(1, config.getInt("Particles.stream-count"));
		this.xOffset = config.getDouble("Particles.xOffset");
		this.yOffset = config.getDouble("Particles.yOffset");
		this.zOffset = config.getDouble("Particles.zOffset");
		this.particleInterval = config.getLong("Particles.interval", 1L);
		this.lightningAmount = config.getInt("lightning-amount");
		this.rewardType = config.getString("drop-type");
		
		this.variables.put("x", 0.0);
		this.variables.put("z", 0.0);
		this.variables.put("theta", theta);
		this.variables.put("t", animationTime);
		
		// Expression parsing
		String shape = config.getString("Particles.Advanced.preset-shape");
		String xCoordExpressionString = config.getString("Particles.Advanced.x-coord-expression");
		String zCoordExpressionString = config.getString("Particles.Advanced.z-coord-expression");
		
		if (shape.equalsIgnoreCase("BALL")) {
			this.plugin.getLogger().info("Using Ball particle effect");
			this.xExpression = MathUtils.parseExpression("x", variables);
			this.zExpression = MathUtils.parseExpression("z", variables);
		}
		else if (shape.equalsIgnoreCase("HELIX")) {
			this.plugin.getLogger().info("Using Helix particle effect");
			this.xExpression = MathUtils.parseExpression("cos(theta) * 1.2", variables);
			this.zExpression = MathUtils.parseExpression("sin(theta) * 1.2", variables);
		}
		else if (shape.equalsIgnoreCase("OPEN_END_HELIX")) {
			this.plugin.getLogger().info("Using Open End Helix particle effect");
			this.particleStreamInterval = 360 / 6;
			this.xExpression = MathUtils.parseExpression("cos(theta) * (100 / t)", variables);
			this.zExpression = MathUtils.parseExpression("sin(theta) * (100 / t)", variables);
		}
		else { // CUSTOM or default
			this.plugin.getLogger().info("Using custom particle effect");
			this.xExpression = MathUtils.parseExpression(xCoordExpressionString, variables);
			this.zExpression = MathUtils.parseExpression(zCoordExpressionString, variables);
		}

		this.respawnDragon = config.getBoolean("respawn", false);
		
		this.runTaskTimer(plugin, 0, this.particleInterval);
	}

	@Override
	public void run() {
		this.animationTime++;
		this.theta += 5;
		
		location.subtract(0, 1 / particleMultiplier, 0);
		if (this.particleStreamInterval < 360) {
			for (int i = 0; i < 360; i += this.particleStreamInterval){
				theta += this.particleStreamInterval;
				
				this.variables.put("x", location.getX());
				this.variables.put("z", location.getZ());
				this.variables.put("theta", theta);
				this.variables.put("t", animationTime);
				
				double x = this.xExpression.evaluate(), z = this.zExpression.evaluate();
				
				location.add(x, 0, z);
				this.world.spawnParticle(particleType, location, particleAmount, xOffset, yOffset, zOffset, particleExtra, null);
				location.subtract(x, 0, z);
			}
		} else {
			this.variables.put("x", location.getX());
			this.variables.put("z", location.getZ());
			this.variables.put("theta", theta);
			this.variables.put("t", animationTime);
			
			double x = this.xExpression.evaluate(), z = this.zExpression.evaluate();
			
			location.add(x, -1 / particleMultiplier, z);
			this.world.spawnParticle(particleType, location, particleAmount, xOffset, yOffset, zOffset, particleExtra, null);
			location.subtract(x, 0, z);
		}
		
		// Particles finished, place reward
		if (this.location.getBlock().getType() == Material.BEDROCK) {
			this.location.add(0, 1, 0);
			
			// Summon Zeus!
			for (int i = 0; i < this.lightningAmount; i++)
				this.world.strikeLightning(location);
			
			// Place the reward
			if (placeEgg) {
				if (this.rewardType.equalsIgnoreCase("CHEST")) {
					//spawn a loot chest
					plugin.getDEDManager().getLootManager().placeChest(location);
				}
				else if (rewardType.equalsIgnoreCase("CHANCE")) {
					double chance = plugin.getConfig().getInt("chest-spawn-chance", 20) / 100D;
					if (ThreadLocalRandom.current().nextInt(100) <= chance) {
						plugin.getDEDManager().getLootManager().placeChest(location);
					}
					else {
						world.getBlockAt(location).setType(Material.DRAGON_EGG);
					}
				}
				else if (rewardType.equalsIgnoreCase("ALL")) {
					plugin.getDEDManager().getLootManager().placeChestAll(location);
				}
				else {
					world.getBlockAt(location).setType(Material.DRAGON_EGG);
				}
			}

			if (respawnDragon && world.getPlayers().size() > 0) {
				plugin.getDEDManager().startRespawn(location, RespawnType.DEATH);
			}
			
			this.cancel();
		}
	}

}