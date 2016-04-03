package com.ninjaguild.dragoneggdrop;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class DragonDeathRunnable implements Runnable {

	private DragonEggDrop plugin = null;
	private World world = null;

	private int particleAmount = 0;
	private double particleLength = 0D;
	private double particleExtra = 0D;
	private long particleInterval = 0L;
	private double oX = 0D;
	private double oY = 0D;
	private double oZ = 0D;
	private Particle particleType = null;

	//private Item egg = null;

	private static final double DROP_START_HEIGHT = 180D;

	public DragonDeathRunnable(DragonEggDrop plugin, World world) {
		this.plugin = plugin;
		this.world = world;

		particleAmount = plugin.getConfig().getInt("particle-amount", 4);
		particleLength = plugin.getConfig().getDouble("particle-length", 6.0D);
		particleExtra = plugin.getConfig().getDouble("particle-extra", 0.0D);
		particleInterval = plugin.getConfig().getLong("particle-interval", 1L);
		oX = plugin.getConfig().getDouble("particle-offset-x", 0.25D);
		oY = plugin.getConfig().getDouble("particle-offset-y", 0D);
		oZ = plugin.getConfig().getDouble("particle-offset-z", 0.25D);
		particleType = Particle.valueOf(plugin.getConfig().getString("particle-type", "FLAME").toUpperCase());

//		egg = world.dropItem(new Location(world, 0.5D, DragonDeathRunnable.DROP_START_HEIGHT, 0.5D), new ItemStack(Material.DRAGON_EGG));
//		egg.setPickupDelay(Integer.MAX_VALUE);
	}

	@Override
	public void run() {
		Item egg = world.dropItem(new Location(world, 0.5D, DragonDeathRunnable.DROP_START_HEIGHT, 0.5D), new ItemStack(Material.DRAGON_EGG));
		egg.setPickupDelay(Integer.MAX_VALUE);
		
		new BukkitRunnable()
		{
			double currentY = DROP_START_HEIGHT;
			
			@Override
			public void run() {
				//don't know why I keep having to tp it back to correct x,z :/
				egg.teleport(new Location(egg.getWorld(), 0.5D, currentY, 0.5D));
				currentY -= 1D;

				for (double d = 0; d < particleLength; d+=0.1D) {
					Location particleLoc = egg.getLocation().clone().add(egg.getVelocity().normalize().multiply(d * -1));
					egg.getWorld().spawnParticle(particleType, particleLoc, particleAmount, oX, oY, oZ, particleExtra, null);
				}

				if (egg == null || egg.isOnGround() || egg.isDead()) {
					cancel();

					if (egg.isOnGround()) {
						new BukkitRunnable()
						{
							@Override
							public void run() {
								Location eggLoc = egg.getLocation();
								egg.remove();
								double eX = eggLoc.getX();
								double eY = eggLoc.getY();
								double eZ = eggLoc.getZ();
								eggLoc.getWorld().createExplosion(eX, eY, eZ, 0f, false, false);

								int lightningAmount = plugin.getConfig().getInt("lightning-amount", 1);
								eggLoc.getWorld().strikeLightningEffect(eggLoc);
								if (lightningAmount > 1) {
									for (int i = 0; i < (lightningAmount - 1); i++) {
										eggLoc.getWorld().spigot().strikeLightningEffect(eggLoc, true);
									}
								}

								eggLoc.getWorld().getBlockAt(egg.getLocation()).setType(Material.DRAGON_EGG);
								//egg.remove();
							}

						}.runTask(plugin);
					}
				}
			}

		}.runTaskTimerAsynchronously(plugin, 300L, particleInterval);
	}
}
