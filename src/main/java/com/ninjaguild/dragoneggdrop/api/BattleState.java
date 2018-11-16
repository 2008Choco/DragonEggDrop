package com.ninjaguild.dragoneggdrop.api;

/**
 * Various states capable of being processed during an Ender Dragon battle.
 * 
 * @author Parker Hawke - 2008Choco
 */
public enum BattleState {
	
	/**
	 * The dragon is dead, awaiting to be respawned.
	 */
	DRAGON_DEAD,
	
	/**
	 * The 4 crystals are starting to spawn on the portal.
	 */
	CRYSTALS_SPAWNING,
	
	/**
	 * The dragon has started its respawn process, including the spawning
	 * of the crystals on the pillars.
	 */
	DRAGON_RESPAWNING,
	
	/**
	 * The battle has initiated, and the dragon is free to roam.
	 */
	BATTLE_COMMENCED,
	
	/**
	 * The battle has ended and the dragon has been slain. Its death
	 * animation is playing.
	 */
	BATTLE_END,
	
	/**
	 * The particles have started descending to drop the loot (if any
	 * at all).
	 */
	PARTICLES_START,
	
	/**
	 * The loot has been spawned on the portal.
	 */
	LOOT_SPAWN;
	
}