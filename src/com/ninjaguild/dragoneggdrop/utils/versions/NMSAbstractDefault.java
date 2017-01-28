package com.ninjaguild.dragoneggdrop.utils.versions;

import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

public class NMSAbstractDefault implements NMSAbstract {
	
//	private static Method methodSetCustomName;
//	
//	private static Method methodCEDGetEnderDragonBattle;
//	
//	private static Field fieldAnimationTimestamp;
//	
//	private static Method methodWPTEGetEnderDragonBattle;
//	
//	private static Method methodInitiateRestartPhase;
//	private static Method methodWasPreviouslyKilled;
//	private static Field fieldBossBattleServer;

	@Override
	public void init(String version) {
		ReflectionUtils.init(version);
		
		if (version != null) return;
		ReflectionUtils.init(version);
		
		// TODO
//		methodSetCustomName = ReflectionUtils.getMethod("a", classTileEntityChest, String.class);
//		
//		methodCEDGetEnderDragonBattle = ReflectionUtils.getMethod("eD", classCraftEnderDragon);
//		
//		fieldAnimationTimestamp = ReflectionUtils.getField("bG", classEntityEnderDragon);
//		
//		methodWPTEGetEnderDragonBattle = ReflectionUtils.getMethod("t", classWorldProviderTheEnd);
//		
//		methodInitiateRestartPhase = ReflectionUtils.getMethod("e", classEnderDragonBattle);
//		methodWasPreviouslyKilled = ReflectionUtils.getMethod("d", classEnderDragonBattle);
//		fieldBossBattleServer = ReflectionUtils.getField("c", classEnderDragonBattle);
	}

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
	public void respawnEnderDragon(Object dragonBattle) {
		
	}

	@Override
	public boolean hasBeenPreviouslyKilled(EnderDragon dragon) {
		return false;
	}
	
	@Override
	public int getEnderDragonDeathAnimationTime(EnderDragon dragon) {
		return 0;
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