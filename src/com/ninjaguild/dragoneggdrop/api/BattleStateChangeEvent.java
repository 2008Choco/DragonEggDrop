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

import com.ninjaguild.dragoneggdrop.versions.DragonBattle;

import org.bukkit.entity.EnderDragon;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when the state of the EnderDragon battle changes.
 * 
 * @author Parker Hawke - 2008Choco
 */
public class BattleStateChangeEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();
	
	private final DragonBattle dragonBattle;
	private final EnderDragon dragon;
	
	private final BattleState previousState, newState;
	
	/**
	 * Construct a new BattleStateChangeEvent.
	 * 
	 * @param dragonBattle an instance of the EnderDragonBattle
	 * @param dragon an instance of the dragon
	 * @param previousState the previous state of the battle
	 * @param newState the new state of the battle
	 */
	public BattleStateChangeEvent(DragonBattle dragonBattle, EnderDragon dragon, BattleState previousState, BattleState newState) {
		this.dragonBattle = dragonBattle;
		this.dragon = dragon;
		this.previousState = previousState;
		this.newState = newState;
	}
	
	/**
	 * Get an instance of the EnderDragonBattle involved in this event.
	 * 
	 * @return the involved EnderDragonBattle. Can return null
	 */
	public DragonBattle getDragonBattle() {
		return dragonBattle;
	}
	
	/**
	 * Get an instance of the EnderDragon involved in this event.
	 * 
	 * @return the involved dragon. Can return null
	 */
	public EnderDragon getDragon() {
		return dragon;
	}
	
	/**
	 * Get the state that the battle was in prior to this change.
	 * 
	 * @return the previous battle state
	 */
	public BattleState getPreviousState() {
		return previousState;
	}
	
	/**
	 * Get the new state of the battle.
	 * 
	 * @return the new battle state
	 */
	public BattleState getNewState() {
		return newState;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}