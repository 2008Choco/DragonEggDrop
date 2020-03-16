package com.ninjaguild.dragoneggdrop.listeners;

import com.ninjaguild.dragoneggdrop.dragon.DamageHistory;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public final class DamageHistoryListener implements Listener {

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

}
