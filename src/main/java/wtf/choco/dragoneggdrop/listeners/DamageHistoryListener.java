package wtf.choco.dragoneggdrop.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.dragon.DamageHistory;
import wtf.choco.dragoneggdrop.utils.DEDConstants;

public final class DamageHistoryListener implements Listener {

    private final DragonEggDrop plugin;

    public DamageHistoryListener(@NotNull DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onDamageDragon(EntityDamageByEntityEvent event) {
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

        double finalDamage = event.getFinalDamage();
        if (finalDamage <= 0.0) {
            return;
        }

        DamageHistory.forEntity(damaged).recordDamage(damager, event.getFinalDamage());
    }

    @EventHandler
    private void onEntityDamagedByLightning(EntityDamageByEntityEvent event) {
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
