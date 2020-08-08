package com.ninjaguild.dragoneggdrop.listeners;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.utils.DEDConstants;
import com.ninjaguild.dragoneggdrop.world.EndWorldWrapper;
import com.ninjaguild.dragoneggdrop.world.RespawnReason;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public final class RespawnListeners implements Listener {

    private final DragonEggDrop plugin;

    public RespawnListeners(DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onPlayerSwitchWorlds(PlayerChangedWorldEvent event) {
        World world = event.getPlayer().getWorld();
        if (world.getEnvironment() != Environment.THE_END) {
            return;
        }

        EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);

        // If there's a regular dragon but no active battle, try to template it
        DragonBattle battle = world.getEnderDragonBattle();
        DragonTemplate activeTemplate = worldWrapper.getActiveTemplate();
        if (battle.getEnderDragon() != null && activeTemplate == null) {
            worldWrapper.setActiveTemplate(activeTemplate = plugin.getDragonTemplateRegistry().getRandomTemplate());
            activeTemplate.applyToBattle(battle.getEnderDragon(), battle);
        }

        // Start the respawn countdown if joining an empty world
        if (plugin.getConfig().getBoolean(DEDConstants.CONFIG_RESPAWN_ON_JOIN, false)) {
            if (world.getPlayers().size() > 1 || worldWrapper.isRespawnInProgress() || world.getEntitiesByClass(EnderDragon.class).size() == 0) {
                return;
            }

            EndWorldWrapper.of(world).startRespawn(RespawnReason.JOIN);
        }

        // Reset end crystal states just in case something went awry
        if (!worldWrapper.isRespawnInProgress()) {
            world.getEntitiesByClass(EnderCrystal.class).forEach(e -> {
                e.setInvulnerable(false);
                e.setBeamTarget(null);
            });
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        World world = event.getPlayer().getWorld();
        if (world.getEnvironment() != Environment.THE_END) {
            return;
        }

        EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);

        // Reset end crystal states just in case something went awry
        if (!worldWrapper.isRespawnInProgress()) {
            world.getEntitiesByClass(EnderCrystal.class).forEach(e -> {
                e.setInvulnerable(false);
                e.setBeamTarget(null);
            });
        }

        // Dragon respawn logic
        if (!plugin.getConfig().getBoolean(DEDConstants.CONFIG_RESPAWN_ON_JOIN, false)) {
            return;
        }

        if (!world.getPlayers().isEmpty() || EndWorldWrapper.of(world).isRespawnInProgress() || world.getEntitiesByClass(EnderDragon.class).size() == 0) {
            return;
        }

        EndWorldWrapper.of(world).startRespawn(RespawnReason.JOIN);
    }

}
