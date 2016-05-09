package com.ninjaguild.dragoneggdrop;

import java.util.HashMap;
import java.util.Map;

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

}
