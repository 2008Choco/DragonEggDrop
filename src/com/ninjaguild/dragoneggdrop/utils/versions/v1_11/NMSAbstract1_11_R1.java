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

package com.ninjaguild.dragoneggdrop.utils.versions.v1_11;

import com.ninjaguild.dragoneggdrop.utils.versions.NMSAbstract;

import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

/**
 * An abstract implementation of necessary net.minecraft.server and
 * org.bukkit.craftbukkit methods that vary between versions causing
 * version dependencies. Allows for version independency through
 * abstraction per Bukkit/Spigot release
 * <p>
 * <b><i>Supported Minecraft Versions:</i></b> 1.11.0, 1.11.1 and 1.11.2
 * 
 * @author Parker Hawke - 2008Choco
 */
public class NMSAbstract1_11_R1 implements NMSAbstract {

	@Override
	public void setDragonBossBarTitle(String title, Object battle) {
		
	}

	@Override
	public Object getEnderDragonBattleFromWorld(World world) {
		return null;
	}

	@Override
	public Object getEnderDragonBattleFromDragon(EnderDragon dragon) {
		return null;
	}

	@Override
	public void respawnEnderDragon() {
		
	}

	@Override
	public boolean hasBeenPreviouslyKilled(EnderDragon dragon) {
		return false;
	}

	@Override
	public void setChestName(Chest chest, String name) {
		
	}

	@Override
	public void sendActionBar(String message, Player... players) {
		
	}

	@Override
	public void broadcastActionBar(String message, World world) {
		
	}
}