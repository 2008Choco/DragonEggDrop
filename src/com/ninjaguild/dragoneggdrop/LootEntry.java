package com.ninjaguild.dragoneggdrop;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

public class LootEntry implements ConfigurationSerializable {

	private int weight = 0;
	private ItemStack item = null;
	
	public LootEntry(int weight, ItemStack item) {
		this.weight = weight;
		this.item = item;
	}
	
	public LootEntry(Map<String, Object> data) {
		this.weight = (int)data.get("weight");
		this.item = (ItemStack)data.get("item");
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> data = new HashMap<>();
		data.put("weight", weight);
		data.put("item", item);
		return data;
	}
	
	protected final int getWeight() {
		return weight;
	}
	
	protected final ItemStack getItem() {
		return item;
	}

}
