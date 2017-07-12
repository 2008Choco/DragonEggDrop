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

package com.ninjaguild.dragoneggdrop.loot;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an entry in the LootManager which can determine the weight
 * of an item when distributing loot after the death of the Ender Dragon
 */
public class LootEntry implements ConfigurationSerializable {

	private final double weight;
	private final ItemStack item;
	
	/**
	 * Construct a new LootEntry with the specified random weight associated
	 * to the specified item
	 * 
	 * @param weight the weighted randomness of the item
	 * @param item the associated item
	 */
	public LootEntry(final double weight, final ItemStack item) {
		this.weight = weight;
		this.item = item;
	}
	
	/**
	 * Construct a new LootEntry based on existing data. Used for
	 * serialization purposes
	 * 
	 * @param data configuration data
	 */
	public LootEntry(Map<String, Object> data) {
		this.weight = (double) data.get("weight");
		this.item = (ItemStack) data.get("item");
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> data = new HashMap<>();
		data.put("weight", weight);
		data.put("item", item);
		return data;
	}
	
	/**
	 * Get the random weight for the LootEntry
	 * 
	 * @return the random weight
	 */
	public double getWeight() {
		return weight;
	}
	
	/**
	 * Get the item associated with this loot entry
	 * 
	 * @return the associated item
	 */
	public ItemStack getItem() {
		return item;
	}
	
    @Override
    public int hashCode() {
    	return new HashCodeBuilder(17, 31).append(weight).append(item).hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (!(obj instanceof LootEntry)) return false;
        if (this == obj) return true;
    	
        LootEntry entry = (LootEntry)obj;
    	return new EqualsBuilder().append(weight, entry.weight).append(item, entry.item).isEquals();
    }
    
}