package com.ninjaguild.dragoneggdrop;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class DragonDeathRunnable implements Runnable {

	private DragonEggDrop plugin = null;

	private int particleAmount = 0;
	private double particleLength = 0D;
	private double particleExtra = 0D;
	private long particleInterval = 0L;
	private double oX = 0D;
	private double oY = 0D;
	private double oZ = 0D;
	private Particle particleType = null;

	private World world = null;

	private boolean placeEgg = false;

	public DragonDeathRunnable(DragonEggDrop plugin, World world, boolean prevKilled) {
		this.plugin = plugin;
		this.world = world;

		this.placeEgg = prevKilled;

		particleAmount = plugin.getConfig().getInt("particle-amount", 4);
		particleLength = plugin.getConfig().getDouble("particle-length", 6.0D);
		particleExtra = plugin.getConfig().getDouble("particle-extra", 0.0D);
		particleInterval = plugin.getConfig().getLong("particle-interval", 1L);
		oX = plugin.getConfig().getDouble("particle-offset-x", 0.25D);
		oY = plugin.getConfig().getDouble("particle-offset-y", 0D);
		oZ = plugin.getConfig().getDouble("particle-offset-z", 0.25D);
		particleType = Particle.valueOf(plugin.getConfig().getString("particle-type", "FLAME").toUpperCase());
	}

	@Override
	public void run() {
		double startY = plugin.getConfig().getDouble("egg-start-y", 180D);

		new BukkitRunnable()
		{
			double currentY = startY;
			Location pLoc = new Location(world, 0.5D, currentY, 0.5D, 0f, 90f);

			@Override
			public void run() {
				currentY -= 1D;
				pLoc.setY(currentY);

				for (double d = 0; d < particleLength; d+=0.1D) {
					world.spawnParticle(particleType, pLoc.clone().add(pLoc.getDirection().normalize().multiply(d * -1)),
							particleAmount, oX, oY, oZ, particleExtra, null);
				}

				Block currentBlock = world.getBlockAt(pLoc);
				if (currentBlock.getType() == Material.BEDROCK) {
					cancel();

					double eX = pLoc.getX();
					double eY = pLoc.getY();
					double eZ = pLoc.getZ();

					new BukkitRunnable()
					{
						@Override
						public void run() {
							world.createExplosion(eX, eY, eZ, 0f, false, false);

							int lightningAmount = plugin.getConfig().getInt("lightning-amount", 4);
							world.strikeLightningEffect(pLoc);
							if (lightningAmount > 1) {
								for (int i = 0; i < (lightningAmount - 1); i++) {
									world.spigot().strikeLightningEffect(pLoc, true);
								}
							}

							if (placeEgg) {
								world.getBlockAt(new Location(world, 0.5D, 63D, 0.5D)).setType(Material.DRAGON_EGG);
							}
						}

					}.runTask(plugin);
				}
			}

		}.runTaskTimerAsynchronously(plugin, 0L, particleInterval);
	}

}
