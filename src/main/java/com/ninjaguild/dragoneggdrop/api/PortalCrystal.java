package com.ninjaguild.dragoneggdrop.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.versions.DragonBattle;
import com.ninjaguild.dragoneggdrop.versions.NMSAbstract;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents all possible locations in which an end crystal may be spawned
 * in regards to the End Portal found in The End.
 * 
 * @author Parker Hawke - 2008Choco
 */
public enum PortalCrystal {
	
	/**
	 * The End Crystal located on the north side of the portal (negative Z axis).
	 */
	NORTH_CRYSTAL(0, -3),
	
	/**
	 * The End Crystal located on the east side of the portal (positive X axis).
	 */
	EAST_CRYSTAL(3, 0),
	
	/**
	 * The End Crystal located on the south side of the portal (positive Z axis).
	 */
	SOUTH_CRYSTAL(0, 3),
	
	/**
	 * The End Crystal located on the west side of the portal (negative X axis).
	 */
	WEST_CRYSTAL(-3, 0);
	
	
	private static final NMSAbstract NMS_ABSTRACT = JavaPlugin.getPlugin(DragonEggDrop.class).getNMSAbstract();
	
	private final int xOffset, zOffset;
	
	private PortalCrystal(int xOffset, int zOffset) {
		this.xOffset = xOffset;
		this.zOffset = zOffset;
	}
	
	/**
	 * Get the offset on the x axis to be applied for this crystal
	 * 
	 * @return the x offset
	 */
	public int getXOffset() {
		return xOffset;
	}
	
	/**
	 * Get the offset on the z axis to be applied for this crystal
	 * 
	 * @return the z offset
	 */
	public int getZOffset() {
		return zOffset;
	}
	
	/**
	 * Get a {@link Location} representing the crystal's expected location relative
	 * to a given location. The passed location assumes the top of the portal (3 blocks
	 * above the base of the portal). See {@link DragonBattle#getEndPortalLocation()}.
	 * 
	 * @param location the location starting point
	 * 
	 * @return the relative crystal location
	 */
	public Location getRelativeTo(Location location) {
		return location.add(xOffset, -3, zOffset);
	}
	
	/**
	 * Get a {@link Location} representing the crystal's expected location relative
	 * to the provided world's portal.
	 * 
	 * @param world the world containing the portal
	 * 
	 * @return the relative crystal location. null if world is not (@link Environment#THE_END)
	 */
	public Location getRelativeToPortal(World world) {
		if (world.getEnvironment() != Environment.THE_END) return null;
		
		Location portal = NMS_ABSTRACT.getEnderDragonBattleFromWorld(world).getEndPortalLocation();
		return getRelativeTo(portal);
	}
	
	/**
	 * Spawn a crystal on the portal in the given world and optionally set its
	 * invulnerability state.
	 * 
	 * @param world the world to spawn the crystal in
	 * @param invulnerable the crystal's invulnerable state
	 * 
	 * @return the spawned crystal. null if unsuccessfully spawned
	 */
	public EnderCrystal spawn(World world, boolean invulnerable) {
		if (world.getEnvironment() != Environment.THE_END) return null;
		
		// (Cloned from #isPresent() only because "location" is required)
		DragonBattle battle = NMS_ABSTRACT.getEnderDragonBattleFromWorld(world);
		Location location = getRelativeTo(battle.getEndPortalLocation());
		
		// Check for existing crystal
		Collection<Entity> entities = world.getNearbyEntities(location, 1, 1, 1);
		return (EnderCrystal) Iterables.find(entities, e -> e instanceof EnderCrystal, world.spawn(location, EnderCrystal.class, e -> {
			e.setInvulnerable(invulnerable);
			e.setShowingBottom(false);
		}));
	}
	
	/**
	 * Spawn a crystal on the portal in the given world and set it as invulnerable.
	 * 
	 * @param world the world to spawn the crystal in
	 * @return the spawned crystal. null if unsuccessfully spawned
	 * 
	 * @see #spawn(World, boolean)
	 */
	public EnderCrystal spawn(World world) {
		return spawn(world, true);
	}
	
	/**
	 * Get the current crystal relative to the world's portal location.
	 * 
	 * @param world the world to reference
	 * 
	 * @return the crystal positioned at the crystal location. null if none
	 */
	public EnderCrystal get(World world) {
		DragonBattle battle = NMS_ABSTRACT.getEnderDragonBattleFromWorld(world);
		Location location = getRelativeTo(battle.getEndPortalLocation());
		
		Collection<Entity> entities = world.getNearbyEntities(location, 1, 1, 1);
		return (EnderCrystal) Iterables.find(entities, e -> e instanceof EnderCrystal, null);
	}
	
	/**
	 * Check whether this crystal is spawned on the portal or not.
	 * 
	 * @param world the world to check
	 * 
	 * @return true if a crystal is spawned. false otherwise
	 */
	public boolean isPresent(World world) {
		DragonBattle battle = NMS_ABSTRACT.getEnderDragonBattleFromWorld(world);
		Location location = getRelativeTo(battle.getEndPortalLocation());
		
		// Check for existing crystal
		Collection<Entity> entities = world.getNearbyEntities(location, 1, 1, 1);
		return Iterables.tryFind(entities, e -> e instanceof EnderCrystal).isPresent();
	}
	
	/**
	 * Get all crystals that have been spawned on the portal in the given world.
	 * 
	 * @param world the world to check
	 * 
	 * @return all spawned ender crystals
	 */
	public static Set<EnderCrystal> getAllSpawnedCrystals(World world) {
		Set<EnderCrystal> crystals = new HashSet<>();
		
		for (PortalCrystal portalCrystal : values()) {
			EnderCrystal crystal = portalCrystal.get(world);
			if (crystal == null) continue;
			
			crystals.add(crystal);
		}
		
		return crystals;
	}
	
}