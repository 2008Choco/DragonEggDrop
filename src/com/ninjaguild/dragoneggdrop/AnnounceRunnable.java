package com.ninjaguild.dragoneggdrop;

import org.bukkit.ChatColor;
import org.bukkit.World;

public class AnnounceRunnable implements Runnable {

	private final DragonEggDrop plugin;
	private final World world;
	private int delay;

	private String color1;
	private String color2;
	
	public AnnounceRunnable(final DragonEggDrop plugin, final World world, final int delay) {
		this.plugin = plugin;
		this.world = world;
		this.delay = delay;
		
		color1 = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("announce-color-one", ChatColor.GOLD.toString()));
		color2 = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("announce-color-two", ChatColor.YELLOW.toString()));
	}
	
	@Override
	public void run() {
		String temp = color1;
		color1 = color2;
		color2 = temp;
		
		String message = color1 + "Dragon Respawn In " + color2 + (delay--) + color1 + " Seconds";
		ActionBar.sendToSome(world.getPlayers(), message);
		
		if (delay == 0) {
			plugin.getDEDManager().cancelAnnounce();
		}
	}

}
