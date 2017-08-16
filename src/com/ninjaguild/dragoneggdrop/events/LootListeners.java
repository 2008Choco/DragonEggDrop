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
import java.util.stream.Collectors;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonLoot;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;

import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LootListeners implements Listener {
	
	private final DragonEggDrop plugin;
	
	public LootListeners(final DragonEggDrop plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		Item item = event.getEntity();
		ItemStack stack = item.getItemStack();
		
		if (item.getWorld().getEnvironment() != Environment.THE_END || stack.getType() != Material.DRAGON_EGG
				|| stack.hasItemMeta()) return;
		
		DragonTemplate dragon = plugin.getDEDManager().getCurrentBattle();
		DragonLoot loot = dragon.getLoot();
		
		String eggName = loot.getEggName().replace("%dragon%", dragon.getName());
		List<String> eggLore = loot.getEggLore().stream()
				.map(s -> s.replace("%dragon%", dragon.getName()))
				.collect(Collectors.toList());

		ItemMeta eggMeta = stack.getItemMeta();
		
		if (eggName != null && !eggName.isEmpty()) {
			eggMeta.setDisplayName(eggName);
		}
		if (eggLore != null && !eggLore.isEmpty()) {
			eggMeta.setLore(eggLore);
		}
		
		stack.setItemMeta(eggMeta);
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		
		if (!(entity instanceof EnderCrystal) || event.getEntity().getWorld().getEnvironment() != Environment.THE_END
				|| !entity.isInvulnerable()) return;
		
		event.setCancelled(true);
	}
}