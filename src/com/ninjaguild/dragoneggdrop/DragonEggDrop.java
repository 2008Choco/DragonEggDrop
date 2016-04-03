package com.ninjaguild.dragoneggdrop;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DragonEggDrop extends JavaPlugin implements Listener {
	
	private static DragonEggDrop instance;
	private static final double DROP_START_HEIGHT = 180D;
	
	private int particleAmount = 0;
	private double particleLength = 0D;
	private double particleExtra = 0D;
	private double oX = 0D;
	private double oY = 0D;
	private double oZ = 0D;
	private Particle particleType = null;
	
	public void onEnable() {
		instance = this;
		
		saveDefaultConfig();
		
		particleAmount = getConfig().getInt("particle-amount", 4);
		particleLength = getConfig().getDouble("particle-length", 6.0D);
		particleExtra = getConfig().getDouble("particle-extra", 0.0D);
		oX = getConfig().getDouble("particle-offset-x", 0.25D);
		oY = getConfig().getDouble("particle-offset-y", 0D);
		oZ = getConfig().getDouble("particle-offset-z", 0.25D);
		particleType = null;
		try {
			particleType = Particle.valueOf(getConfig().getString("particle-type", "FLAME").toUpperCase());
		} catch (IllegalArgumentException ex) {
			getLogger().log(Level.WARNING, "INVALID PARTICLE TYPE SPECIFIED! DISABLING...");
			getServer().getPluginManager().disablePlugin(this);
			getLogger().log(Level.INFO, "PLUGIN DISABLED");
			return;
		}
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable() {
	}
	
	private static DragonEggDrop getInstance() {
		return instance;
	}
	
	@EventHandler
	private void onDragonDeath(EntityDeathEvent e) {
		if (e.getEntityType() == EntityType.ENDER_DRAGON) {
			new BukkitRunnable()
			{
				double currentY = DROP_START_HEIGHT;
				
				@Override
				public void run() {
					Item egg = e.getEntity().getWorld().dropItem(new Location(e.getEntity().getWorld(), 0.5D, currentY, 0.5D), new ItemStack(Material.DRAGON_EGG));
					egg.setPickupDelay(Integer.MAX_VALUE);

					new BukkitRunnable()
					{
						@Override
						public void run() {
							//don't know why I keep having to tp it back to correct x,z :/
							egg.teleport(new Location(egg.getWorld(), 0.5D, currentY, 0.5D));
							currentY -= 1D;
						    
							for (double d = 0; d < particleLength; d+=0.1D) {
								Location particleLoc = egg.getLocation().clone().add(egg.getVelocity().normalize().multiply(d * -1));
								egg.getWorld().spawnParticle(particleType, particleLoc, particleAmount, oX, oY, oZ, particleExtra, particleType.getDataType());
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
                                            
									    	int lightningAmount = DragonEggDrop.getInstance().getConfig().getInt("lightning-amount", 1);
									    	eggLoc.getWorld().strikeLightningEffect(eggLoc);
									    	if (lightningAmount > 1) {
									    		for (int i = 0; i < (lightningAmount - 1); i++) {
									    			eggLoc.getWorld().spigot().strikeLightningEffect(eggLoc, true);
									    		}
									    	}

									    	eggLoc.getWorld().getBlockAt(egg.getLocation()).setType(Material.DRAGON_EGG);
									    	//egg.remove();
										}

								    }.runTask(DragonEggDrop.getInstance());
								}
							}
						}
						
					}.runTaskTimerAsynchronously(DragonEggDrop.getInstance(), 0L, getConfig().getLong("particle-interval", 1L));
				}

			}.runTaskLater(this, 300L);
		}
	}
	
}
