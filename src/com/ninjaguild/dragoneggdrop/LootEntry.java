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

package com.ninjaguild.dragoneggdrop;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class LootEntry implements ConfigurationSerializable {

	private final double weight;
	private final ItemStack item;
	
	public LootEntry(final double weight, final ItemStack item) {
		this.weight = weight;
		this.item = item;
	}
	
	public LootEntry(Map<String, Object> data) {
		this.weight = (double)data.get("weight");
		this.item = (ItemStack)data.get("item");
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> data = new HashMap<>();
		data.put("weight", weight);
		data.put("item", item);
		return data;
	}
	
	protected double getWeight() {
		return weight;
	}
	
	protected ItemStack getItem() {
		return item;
	}
	
    @Override
    public int hashCode() {
    	return new HashCodeBuilder(17, 31).
    			append(getWeight()).
    			append(getItem()).
    			hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LootEntry)) {
            return false;
        }
    	
        LootEntry entry = (LootEntry)obj;
    	return new EqualsBuilder().
    			append(getWeight(), entry.getWeight()).
    			append(getItem(), entry.getItem()).
    			isEquals();
    }

}
