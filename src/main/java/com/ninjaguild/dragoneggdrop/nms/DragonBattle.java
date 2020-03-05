package com.ninjaguild.dragoneggdrop.nms;

import java.lang.reflect.Field;
import java.util.UUID;

import com.google.common.base.Enums;
import com.google.common.base.Optional;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.BossBattle;
import net.minecraft.server.v1_15_R1.BossBattleServer;
import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.EnderDragonBattle;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.PacketPlayOutBoss;
import net.minecraft.server.v1_15_R1.WorldServer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EnderDragon;

public class DragonBattle {

	private final EnderDragonBattle battle;

	protected DragonBattle(EnderDragonBattle battle) {
		this.battle = battle;
	}

	public void setBossBarTitle(String title) {
		if (title == null) return;

		BossBattleServer battleServer = battle.bossBattle;
		if (battleServer == null) return;

		battleServer.title = new ChatMessage(title);
		battleServer.sendUpdate(PacketPlayOutBoss.Action.UPDATE_NAME);
	}

	public boolean setBossBarStyle(BarStyle style, BarColor colour) {
		BossBattleServer battleServer = battle.bossBattle;
		if (battleServer == null) return false;

		if (style != null) {
			Optional<BossBattle.BarStyle> nmsStyle = Enums.getIfPresent(BossBattle.BarStyle.class, style.name().contains("SEGMENTED") ? style.name().replace("SEGMENTED", "NOTCHED") : "PROGRESS");
			if (nmsStyle.isPresent()) {
				battleServer.style = nmsStyle.get();
			}
		}
		if (colour != null) {
			battleServer.color = BossBattle.BarColor.valueOf(colour.name());
		}

		battleServer.sendUpdate(PacketPlayOutBoss.Action.UPDATE_STYLE);
		return true;
	}

	public EnderDragon getEnderDragon() {
		EnderDragon dragon = null;

		try {
			Field fieldWorldServer = EnderDragonBattle.class.getDeclaredField("d");
			Field fieldDragonUUID = EnderDragonBattle.class.getDeclaredField("m");
			fieldWorldServer.setAccessible(true);
			fieldDragonUUID.setAccessible(true);

			WorldServer world = (WorldServer) fieldWorldServer.get(battle);
			UUID dragonUUID = (UUID) fieldDragonUUID.get(battle);

			if (world == null || dragonUUID == null)
				return null;

			Entity dragonEntity = world.getEntity(dragonUUID);
			if (dragonEntity == null) return null;
			dragon = (EnderDragon) dragonEntity.getBukkitEntity();

			fieldWorldServer.setAccessible(false);
			fieldDragonUUID.setAccessible(false);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}

		return dragon;
	}

	public void respawnEnderDragon() {
		this.battle.e();
	}

	public Location getEndPortalLocation() {
		Location portalLocation = null;

		try {
			Field fieldExitPortalLocation = EnderDragonBattle.class.getDeclaredField("o");
			Field fieldWorldServer = EnderDragonBattle.class.getDeclaredField("d");
			fieldExitPortalLocation.setAccessible(true);
			fieldWorldServer.setAccessible(true);

			WorldServer worldServer = (WorldServer) fieldWorldServer.get(battle);
			BlockPosition position = (BlockPosition) fieldExitPortalLocation.get(battle);
			if (worldServer != null && position != null) {
				World world = worldServer.getWorld();
				portalLocation = new Location(world, Math.floor(position.getX()) + 0.5, position.getY() + 4, Math.floor(position.getZ()) + 0.5);
			}

			fieldWorldServer.setAccessible(false);
			fieldExitPortalLocation.setAccessible(false);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}

		return portalLocation;
	}

	public void resetBattleState() {
		try {
			Field fieldDragonBattleState = EnderDragonBattle.class.getDeclaredField("p");
			Field fieldDragonKilled = EnderDragonBattle.class.getDeclaredField("k");
			fieldDragonBattleState.setAccessible(true);
			fieldDragonKilled.setAccessible(true);

			fieldDragonBattleState.set(battle, null);
			fieldDragonKilled.set(battle, true);

			fieldDragonBattleState.setAccessible(false);
			fieldDragonKilled.setAccessible(false);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}

		this.battle.f();
	}

	/**
	 * Get the net.minecraft.server implementation of DragonBattle
	 *
	 * @return the wrapped battle
	 */
	public EnderDragonBattle getHandle() {
		return battle;
	}

}