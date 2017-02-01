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

package com.ninjaguild.dragoneggdrop.api;

/**
 * Various states capable of being processed during an EnderDragonBattle
 * 
 * @author Parker Hawke - 2008Choco
 */
public enum BattleState {
	
	/**
	 * The dragon is dead, awaiting to be respawned
	 */
	DRAGON_DEAD,
	
	/**
	 * The 4 crystals are starting to spawn on the portal
	 */
	CRYSTALS_SPAWNING,
	
	/**
	 * The dragon has started its respawn process, including the spawning
	 * of the crystals on the pillars
	 */
	DRAGON_RESPAWNING,
	
	/**
	 * The battle has initiated, and the dragon is free to roam
	 */
	BATTLE_COMMENCED,
	
	/**
	 * The battle has ended and the dragon has been slain. Its death
	 * animation is playing
	 */
	BATTLE_END,
	
	/**
	 * The particles have started descending to drop the loot (if any
	 * at all)
	 */
	PARTICLES_START,
	
	/**
	 * The loot has been spawned on the portal
	 */
	LOOT_SPAWN;
	
}