package com.ninjaguild.dragoneggdrop.versions.v1_9;

import java.util.Arrays;

import com.ninjaguild.dragoneggdrop.versions.DragonBattle;
import com.ninjaguild.dragoneggdrop.versions.NMSAbstract;

import net.minecraft.server.v1_9_R1.EnderDragonBattle;
import net.minecraft.server.v1_9_R1.EntityEnderDragon;
import net.minecraft.server.v1_9_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_9_R1.PacketPlayOutChat;
import net.minecraft.server.v1_9_R1.WorldProvider;
import net.minecraft.server.v1_9_R1.WorldProviderTheEnd;

import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.block.CraftChest;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

/**
 * An abstract implementation of necessary net.minecraft.server and
 * org.bukkit.craftbukkit methods that vary between versions causing
 * version dependencies. Allows for version independency through
 * abstraction per Bukkit/Spigot release
 * <p>
 * <b><i>Supported Minecraft Versions:</i></b> 1.9.0, 1.9.1, 1.9.2 and 1.9.3
 * 
 * @author Parker Hawke - 2008Choco
 */
public class NMSAbstract1_9_R1 implements NMSAbstract {
	
	@Override
	public DragonBattle getEnderDragonBattleFromWorld(World world) {
		if (world == null) return null;
		
		CraftWorld craftWorld = (CraftWorld) world;
		WorldProvider worldProvider = craftWorld.getHandle().worldProvider;
		
		if (!(worldProvider instanceof WorldProviderTheEnd)) return null;
		return new DragonBattle1_9_R1(((WorldProviderTheEnd) worldProvider).s());
	}

	@Override
	public DragonBattle getEnderDragonBattleFromDragon(EnderDragon dragon) {
		if (dragon == null) return null;
		
		EntityEnderDragon nmsDragon = ((CraftEnderDragon) dragon).getHandle();
		return new DragonBattle1_9_R1(nmsDragon.cU());
	}
	
	@Override
	public boolean hasBeenPreviouslyKilled(EnderDragon dragon) {
		if (dragon == null) return false;
		
		EnderDragonBattle battle = ((DragonBattle1_9_R1) this.getEnderDragonBattleFromDragon(dragon)).getHandle();
		return battle.d();
	}
	
	@Override
	public int getEnderDragonDeathAnimationTime(EnderDragon dragon) {
		if (dragon == null) return -1;
		
		EntityEnderDragon nmsDragon = ((CraftEnderDragon) dragon).getHandle();
		return nmsDragon.bF;
	}

	@Override
	public void setChestName(Chest chest, String name) {
		if (chest == null || name == null) return;
		
		CraftChest craftChest = (CraftChest) chest;
		craftChest.getTileEntity().a(name);
	}

	@Override
	public void sendActionBar(String message, Player... players) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2);
		Arrays.stream(players).forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
	}

	@Override
	public void broadcastActionBar(String message, World world) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2);
		world.getPlayers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
	}
}