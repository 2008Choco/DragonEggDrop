package wtf.choco.dragoneggdrop.listeners;

import java.util.List;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import wtf.choco.dragoneggdrop.dragon.DragonTemplate;
import wtf.choco.dragoneggdrop.dragon.loot.elements.DragonLootElementEgg;
import wtf.choco.dragoneggdrop.placeholder.DragonEggDropPlaceholders;
import wtf.choco.dragoneggdrop.world.EndWorldWrapper;

public final class LootListeners implements Listener {

    @EventHandler
    private void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        ItemStack stack = item.getItemStack();
        World world = item.getWorld();

        if (world.getEnvironment() != Environment.THE_END || stack.getType() != Material.DRAGON_EGG || stack.hasItemMeta()) {
            return;
        }

        DragonTemplate dragon = EndWorldWrapper.of(world).getPreviousTemplate();
        if (dragon == null) {
            return;
        }

        DragonLootElementEgg egg = dragon.getLootTable().getEgg();
        if (egg == null) {
            return;
        }

        ItemMeta meta = stack.getItemMeta();
        String name = egg.getName();
        if (name != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name.replace("%dragon%", dragon.getName())));
        }

        List<String> lore = egg.getLore();
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line.replace("%dragon%", dragon.getName()))).collect(Collectors.toList()));
        }

        stack.setItemMeta(meta);

        List<Player> players = world.getPlayers();
        if (players.size() >= 1) { // Only need it for the world anyways
            DragonEggDropPlaceholders.inject(players.get(0), stack);
        }

        item.setItemStack(stack);
    }

}
