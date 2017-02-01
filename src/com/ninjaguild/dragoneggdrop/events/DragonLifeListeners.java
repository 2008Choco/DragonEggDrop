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

package com.ninjaguild.dragoneggdrop.events;

import java.util.List;
import java.util.Random;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.api.BattleState;
import com.ninjaguild.dragoneggdrop.api.BattleStateChangeEvent;
import com.ninjaguild.dragoneggdrop.utils.runnables.DragonDeathRunnable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class DragonLifeListeners implements Listener {
	
	private final DragonEggDrop plugin;
	private final Random random;
	
	public DragonLifeListeners(DragonEggDrop plugin) {
		this.plugin = plugin;
		this.random = new Random();
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (!(event.getEntity() instanceof EnderDragon)) return;
		
		EnderDragon dragon = (EnderDragon) event.getEntity();
		Object dragonBattle = plugin.getNMSAbstract().getEnderDragonBattleFromDragon(dragon);
		plugin.getDEDManager().setRespawnInProgress(false);
		
		List<String> dragonNames = plugin.getDEDManager().getDragonNames();
		if (!dragonNames.isEmpty()) {
			String name = ChatColor.translateAlternateColorCodes('&', dragonNames.get(random.nextInt(dragonNames.size())));
			dragon.setCustomName(name);
			
			plugin.getDEDManager().setDragonBossBarTitle(name, plugin.getDEDManager().getEnderDragonBattleFromDragon(dragon));
		}
		
		BattleStateChangeEvent bscEventCrystals = new BattleStateChangeEvent(dragonBattle, dragon, BattleState.DRAGON_RESPAWNING, BattleState.BATTLE_COMMENCED);
		Bukkit.getPluginManager().callEvent(bscEventCrystals);
	}
	
	@EventHandler
	public void onDragonDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof EnderDragon)) return;
		
		EnderDragon dragon = (EnderDragon) event.getEntity();
		Object dragonBattle = plugin.getNMSAbstract().getEnderDragonBattleFromDragon(dragon);
		boolean prevKilled = this.plugin.getNMSAbstract().hasBeenPreviouslyKilled(dragon); // PreviouslyKilled
		World world = event.getEntity().getWorld();
		
		BattleStateChangeEvent bscEventCrystals = new BattleStateChangeEvent(dragonBattle, dragon, BattleState.BATTLE_COMMENCED, BattleState.BATTLE_END);
		Bukkit.getPluginManager().callEvent(bscEventCrystals);

		new BukkitRunnable() {
			@Override
			public void run() {
				if (plugin.getNMSAbstract().getEnderDragonDeathAnimationTime(dragon)>= 185) { // Dragon is dead at 200
					this.cancel();
					new DragonDeathRunnable(plugin, world, dragon, prevKilled);
				}
			}
		}.runTaskTimer(plugin, 0L, 1L);
		
		
	}
}