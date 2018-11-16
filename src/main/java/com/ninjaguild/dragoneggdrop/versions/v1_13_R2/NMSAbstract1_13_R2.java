package com.ninjaguild.dragoneggdrop.versions.v1_13_R2;

import java.util.Arrays;

import com.ninjaguild.dragoneggdrop.versions.DragonBattle;
import com.ninjaguild.dragoneggdrop.versions.NMSAbstract;

import net.minecraft.server.v1_13_R2.ChatMessageType;
import net.minecraft.server.v1_13_R2.EnderDragonBattle;
import net.minecraft.server.v1_13_R2.EntityEnderDragon;
import net.minecraft.server.v1_13_R2.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_13_R2.PacketPlayOutChat;
import net.minecraft.server.v1_13_R2.WorldProvider;
import net.minecraft.server.v1_13_R2.WorldProviderTheEnd;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

/**
 * An abstract implementation of necessary net.minecraft.server and
 * org.bukkit.craftbukkit methods that vary between versions causing
 * version dependencies. Allows for version independency through
 * abstraction per Bukkit/Spigot release
 * <p>
 * <b><i>Supported Minecraft Versions:</i></b> 1.12.0
 * 
 * @author Parker Hawke - 2008Choco
 */
public class NMSAbstract1_13_R2 implements NMSAbstract {
	
	@Override
	public DragonBattle getEnderDragonBattleFromWorld(World world) {
		if (world == null) return null;
		
		CraftWorld craftWorld = (CraftWorld) world;
		WorldProvider worldProvider = craftWorld.getHandle().worldProvider;
		
		if (!(worldProvider instanceof WorldProviderTheEnd)) return null;
		return new DragonBattle1_13_R2(((WorldProviderTheEnd) worldProvider).r());
	}

	@Override
	public DragonBattle getEnderDragonBattleFromDragon(EnderDragon dragon) {
		if (dragon == null) return null;
		
		EntityEnderDragon nmsDragon = ((CraftEnderDragon) dragon).getHandle();
		return new DragonBattle1_13_R2(nmsDragon.ds());
	}
	
	@Override
	public boolean hasBeenPreviouslyKilled(EnderDragon dragon) {
		if (dragon == null) return false;
		
		EnderDragonBattle battle = ((DragonBattle1_13_R2) this.getEnderDragonBattleFromDragon(dragon)).getHandle();
		return battle.d();
	}
	
	@Override
	public int getEnderDragonDeathAnimationTime(EnderDragon dragon) {
		if (dragon == null) return -1;
		
		EntityEnderDragon nmsDragon = ((CraftEnderDragon) dragon).getHandle();
		return nmsDragon.bO;
	}

	@Override
	@Deprecated
	public void setChestName(Chest chest, String name) {
		chest.setCustomName(name);
	}

	@Override
	public void sendActionBar(String message, Player... players) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), ChatMessageType.GAME_INFO);
		Arrays.stream(players).forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
	}

	@Override
	public void broadcastActionBar(String message, World world) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), ChatMessageType.GAME_INFO);
		world.getPlayers().forEach(p -> ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet));
	}
	
	@Override
	public void spawnParticle(Particle particle, Location location, int count, double xOffset, double yOffset, double zOffset, double speed) {
		location.getWorld().spawnParticle(particle, location, count, xOffset, yOffset, zOffset, speed, null, true);
	}
}