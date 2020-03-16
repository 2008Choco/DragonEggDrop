package com.ninjaguild.dragoneggdrop.listeners;

import java.util.List;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.api.BattleState;
import com.ninjaguild.dragoneggdrop.api.BattleStateChangeEvent;
import com.ninjaguild.dragoneggdrop.api.PortalCrystal;
import com.ninjaguild.dragoneggdrop.dragon.DragonTemplate;
import com.ninjaguild.dragoneggdrop.nms.DragonBattle;
import com.ninjaguild.dragoneggdrop.nms.NMSUtils;
import com.ninjaguild.dragoneggdrop.tasks.DragonDeathRunnable;
import com.ninjaguild.dragoneggdrop.world.EndWorldWrapper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public final class DragonLifeListeners implements Listener {

    private static final ItemStack END_CRYSTAL_ITEM = new ItemStack(Material.END_CRYSTAL);

    private final DragonEggDrop plugin;

    public DragonLifeListeners(DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) {
            return;
        }

        EnderDragon dragon = (EnderDragon) event.getEntity();
        if (dragon.getWorld().getEnvironment() != Environment.THE_END) {
            return;
        }

        DragonBattle dragonBattle = NMSUtils.getEnderDragonBattleFromDragon(dragon);
        EndWorldWrapper world = EndWorldWrapper.of(dragon.getWorld());

        DragonTemplate template = world.getRespawningTemplate();
        System.out.println("Respawning template: " + (template != null ? template.getId() : "null"));
        if (plugin.getConfig().getBoolean("strict-countdown") && world.isRespawnInProgress()) {
            world.stopRespawn();
        }

        world.setActiveTemplate(template);
        world.setRespawningTemplate(null);

        if (template != null) {
            template.applyToBattle(dragon, dragonBattle);

            if (template.shouldAnnounceSpawn()) {
                // Cannot use p::sendMessage here. Compile-error with Maven. Compiler assumes the wrong method overload
                Bukkit.getOnlinePlayers().forEach(p -> template.getSpawnAnnouncement().forEach(m -> p.sendMessage(m)));
            }
        }

        BattleStateChangeEvent bscEventCrystals = new BattleStateChangeEvent(dragonBattle, dragon, BattleState.DRAGON_RESPAWNING, BattleState.BATTLE_COMMENCED);
        Bukkit.getPluginManager().callEvent(bscEventCrystals);
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) {
            return;
        }

        EnderDragon dragon = (EnderDragon) event.getEntity();
        DragonBattle dragonBattle = NMSUtils.getEnderDragonBattleFromDragon(dragon);

        World world = event.getEntity().getWorld();
        EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);

        BattleStateChangeEvent bscEventCrystals = new BattleStateChangeEvent(dragonBattle, dragon, BattleState.BATTLE_COMMENCED, BattleState.BATTLE_END);
        Bukkit.getPluginManager().callEvent(bscEventCrystals);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (NMSUtils.getEnderDragonDeathAnimationTime(dragon) >= 185) { // Dragon is dead at 200
                    new DragonDeathRunnable(plugin, worldWrapper, dragon);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @EventHandler
    public void onAttemptRespawn(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.END_CRYSTAL || plugin.getConfig().getBoolean("allow-crystal-respawns")) {
            return;
        }

        World world = player.getWorld();
        EndWorldWrapper worldWrapper = EndWorldWrapper.of(world);
        if (worldWrapper.isRespawnInProgress() || !world.getEntitiesByClass(EnderDragon.class).isEmpty()) {
            List<EnderCrystal> crystals = PortalCrystal.getAllSpawnedCrystals(world);

            // Check for 3 crystals because PlayerInteractEvent is fired first
            if (crystals.size() < 3) {
                return;
            }

            for (EnderCrystal crystal : crystals) {
                Location location = crystal.getLocation();
                location.getBlock().setType(Material.AIR); // Remove fire
                world.dropItem(location, END_CRYSTAL_ITEM);
                crystal.remove();
            }

            NMSUtils.sendActionBar(ChatColor.RED + "You cannot manually respawn a dragon!", player);
            player.sendMessage(ChatColor.RED + "You cannot manually respawn a dragon!");
            event.setCancelled(true);
        }
    }

}
