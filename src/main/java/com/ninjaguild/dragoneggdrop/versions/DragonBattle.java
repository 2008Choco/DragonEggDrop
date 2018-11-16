package com.ninjaguild.dragoneggdrop.versions;

import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EnderDragon;

/**
 * Represents a dragon battle in the End worlds.
 * 
 * @author Parker Hawke - 2008Choco
 */
public interface DragonBattle {
	
	/**
	 * Set the title of the boss bar to a specific name.
	 * 
	 * @param title the title to set
	 */
	public void setBossBarTitle(String title);
	
	/**
	 * Set the style and colour of the boss bar.
	 * 
	 * @param style the style of the boss bar
	 * @param colour the colour of the boss bar
	 * 
	 * @return true if the style change was successful
	 */
	public boolean setBossBarStyle(BarStyle style, BarColor colour);
	
	/**
	 * Get the associated EnderDragon participating in this battle.
	 * 
	 * @return the dragon
	 */
	public EnderDragon getEnderDragon();
	
	/**
	 * Set the state of the battle to its respawn state, and restart the battle
	 * once again.
	 */
	public void respawnEnderDragon();
	
	/**
	 * Get the Location of the end portal to the overworld.
	 * 
	 * @return the portal location
	 */
	public Location getEndPortalLocation();
	
	/**
	 * Reset the end world's battle state.
	 * <br><b>NOTE:</b> This is to be used internally and is for emergency cases ONLY.
	 * Mojang's code breaks rather frequently when attempting to respawn the dragon
	 * and will occasionally prevent dragons from spawning at all.
	 */
	public void resetBattleState();
	
}