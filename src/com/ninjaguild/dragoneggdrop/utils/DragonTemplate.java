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

package com.ninjaguild.dragoneggdrop.utils;

import java.util.ArrayList;
import java.util.List;

import com.ninjaguild.dragoneggdrop.utils.versions.NMSAbstract;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EnderDragon;

public class DragonTemplate {
	
	private final String name;
	private final BarStyle barStyle;
	private final BarColor barColour;
	
	public DragonTemplate(String name, BarStyle barStyle, BarColor barColour) {
		this.name = (name != null ? ChatColor.translateAlternateColorCodes('&', name) : null);
		this.barStyle = barStyle;
		this.barColour = barColour;
	}
	
	public String getName() {
		return name;
	}
	
	public BarStyle getBarStyle() {
		return barStyle;
	}
	
	public BarColor getBarColor() {
		return barColour;
	}
	
	public void applyToBattle(NMSAbstract nmsAbstract, EnderDragon dragon, Object battle) {
		if (name != null) {
			dragon.setCustomName(name);
			nmsAbstract.setDragonBossBarTitle(name, battle);
		}
		nmsAbstract.setBattleBossBarStyle(battle, barStyle, barColour);
	}
	
	public static List<DragonTemplate> loadTemplates(List<String> data) {
		List<DragonTemplate> templates = new ArrayList<>();
		
		for (String dragonData : data) {
			String[] splitData = dragonData.split("\\|");
			
			String name = null;
			BarStyle style = null;
			BarColor colour = null;
			
			// Here for legacy purposes
			if (splitData.length == 1) {
				name = dragonData;
				templates.add(new DragonTemplate(name, null, null));
				continue;
			}
			
			for (String dragonInfo : splitData) {
				String[] splitInfo = dragonInfo.split("=");
				if (splitInfo.length != 2) continue;
				
				String dataType = splitInfo[0];
				String value = splitInfo[1];
				
				if (dataType.equalsIgnoreCase("name")) {
					name = value;
				}
				else if (dataType.equalsIgnoreCase("style")) {
					value = value.toUpperCase();
					
					if (!EnumUtils.isValidEnum(BarStyle.class, value)) continue;
					style = BarStyle.valueOf(value);
				}
				else if (dataType.equalsIgnoreCase("color") || dataType.equalsIgnoreCase("colour")) {
					value = value.toUpperCase();
					
					if (!EnumUtils.isValidEnum(BarColor.class, value)) continue;
					colour = BarColor.valueOf(value);
				}
			}
			
			templates.add(new DragonTemplate(name, style, colour));
		}
		
		return templates;
	}
}