package com.ninjaguild.dragoneggdrop.listeners;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.dragon.DamageHistory;
import com.ninjaguild.dragoneggdrop.utils.DEDConstants;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.projectiles.ProjectileSource;

public final class DamageHistoryListener implements Listener {

    private final DragonEggDrop plugin;

    public DamageHistoryListener(DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageDragon(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity(), damager = event.getDamager();
        if (damaged.getType() != EntityType.ENDER_DRAGON) {
            return;
        }

        // Only record damage from a projectile's shooter, not the projectile itself
        if (damager instanceof Projectile) {
            ProjectileSource source = ((Projectile) damager).getShooter();
            if (!(source instanceof Entity)) { // Ignore non-entity projectile sources (i.e. dispensers)
                return;
            }

            damager = (Entity) source;
        }

        DamageHistory.forEntity(damaged).recordDamage(damager, event.getFinalDamage());
    }

    @EventHandler
    public void onEntityDamagedByLightning(EntityDamageByEntityEvent event) {
        if (event.getCause() != DamageCause.LIGHTNING || plugin.getConfig().getBoolean(DEDConstants.CONFIG_LIGHTNING_DAMAGES_ENTITIES, false)) {
            return;
        }

        Entity damager = event.getDamager();
        if (!damager.hasMetadata(DEDConstants.METADATA_LOOT_LIGHTNING)) {
            return;
        }

        event.setCancelled(true);
    }

}
