package wtf.choco.dragoneggdrop.listeners;

import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.DragonBattle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import wtf.choco.commons.util.MathUtil;
import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.utils.ConfigUtils;
import wtf.choco.dragoneggdrop.utils.DEDConstants;
import wtf.choco.dragoneggdrop.world.DragonRespawnData;
import wtf.choco.dragoneggdrop.world.EndWorldWrapper;

public final class PortalClickListener implements Listener {

    private final DragonEggDrop plugin;

    public PortalClickListener(@NotNull DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onClickEndPortalFrame(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        Player player = event.getPlayer();
        World world = player.getWorld();
        if (plugin.getConfig().getStringList(DEDConstants.CONFIG_DISABLED_WORLDS).contains(world.getName())) {
            return;
        }

        PlayerInventory inventory = player.getInventory();

        if (event.getMaterial() != Material.BEDROCK || world.getEnvironment() != World.Environment.THE_END || event.getHand() != EquipmentSlot.HAND
                || (inventory.getItemInMainHand().getType() != Material.AIR || inventory.getItemInOffHand().getType() != Material.AIR)) {
            return;
        }

        DragonBattle dragonBattle = world.getEnderDragonBattle();
        if (dragonBattle == null) {
            return;
        }

        Location endPortalLocation = dragonBattle.getEndPortalLocation();
        if (endPortalLocation == null) {
            return;
        }

        Location portalLocation = endPortalLocation.add(0, 4, 0);
        if (clickedBlock.getLocation().distanceSquared(portalLocation) > 36) { // 6 blocks
            return;
        }

        EndWorldWrapper endWorld = EndWorldWrapper.of(world);
        DragonRespawnData respawnData = endWorld.getDragonRespawnData();
        if (respawnData == null || respawnData.isReady()) {
            return;
        }

        FileConfiguration config = plugin.getConfig();
        boolean condensed = config.getBoolean(DEDConstants.CONFIG_RESPAWN_MESSAGES_CONDENSED);
        TimeUnit[] omitions = ConfigUtils.getTimeUnits(config.getStringList(DEDConstants.CONFIG_RESPAWN_MESSAGES_OMIT_TIME_UNITS));

        DragonEggDrop.sendMessage(player, "Dragon will respawn in " + ChatColor.YELLOW + MathUtil.getFormattedTime(respawnData.getRemainingTime(TimeUnit.SECONDS), TimeUnit.SECONDS, condensed, omitions));
    }

}
