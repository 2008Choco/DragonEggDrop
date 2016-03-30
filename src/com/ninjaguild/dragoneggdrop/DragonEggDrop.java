package com.ninjaguild.dragoneggdrop;

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
	
	public void onEnable() {
		instance = this;
		
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
							egg.teleport(new Location(egg.getWorld(), 0.5D, currentY, 0.5D));
							currentY -= 1D;
							
							for (int i = 0; i < 10; i++) {
								Location particleLoc = egg.getLocation().clone().add(egg.getVelocity().normalize().multiply(i * -1));
								egg.getWorld().spawnParticle(Particle.FLAME, particleLoc, 1, 0D, 0D, 0D, 0D, 0);
							}

							if (egg == null || egg.isOnGround() || egg.isDead())
							{
								cancel();

								if (egg.isOnGround())
								{
								    new BukkitRunnable()
									{
									    @Override
										public void run()
										{
									    	Location eggLoc = egg.getLocation();
									    	double eX = eggLoc.getX();
									    	double eY = eggLoc.getY();
									    	double eZ = eggLoc.getZ();
									    	egg.getWorld().createExplosion(eX, eY, eZ, 0f, false, false);
                                            
									    	int lightningAmount = DragonEggDrop.getInstance().getConfig().getInt("lightning-amount");
									    	for (int i = 0; i < lightningAmount; i++) {
									    	    egg.getWorld().strikeLightningEffect(eggLoc);
									    	}

									    	egg.getWorld().getBlockAt(egg.getLocation()).setType(Material.DRAGON_EGG);
									    	egg.remove();
										}

								    }.runTask(DragonEggDrop.getInstance());
								}
							}
						}
						
					}.runTaskTimerAsynchronously(DragonEggDrop.getInstance(), 0L, 1L);
				}

			}.runTaskLater(this, 300L);
		}
	}
	
}
