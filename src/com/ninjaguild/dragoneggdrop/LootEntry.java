package com.ninjaguild.dragoneggdrop;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class LootEntry implements ConfigurationSerializable {

	private double weight = 0;
	private ItemStack item = null;
	
	public LootEntry(double weight, ItemStack item) {
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
	
	protected final double getWeight() {
		return weight;
	}
	
	protected final ItemStack getItem() {
		return item;
	}
	
    @Override
    public final int hashCode() {
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
