package wtf.choco.dragoneggdrop.placeholder;

import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Represents a provider for placeholder strings from configuration and loot tables.
 *
 * @author Parker Hawke - Choco
 */
public interface PlaceholderProvider {

    /**
     * Inject placeholders into the provided string given an optional player context.
     *
     * @param player the player context or null if none
     * @param string the string to inject
     *
     * @return the injected string
     */
    public String inject(OfflinePlayer player, String string);

    /**
     * Inject placeholders into the Strings from the provided item given an optional
     * player context.
     *
     * @param player the player context or null if none
     * @param item the item to inject
     */
    public default void inject(OfflinePlayer player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        // Placeholder injection
        ItemMeta meta = item.getItemMeta();

        if (meta.hasDisplayName()) {
            meta.setDisplayName(inject(player, meta.getDisplayName()));
        }

        if (meta.hasLore()) {
            meta.setLore(meta.getLore().stream().map(s -> inject(player, s)).collect(Collectors.toList()));
        }

        item.setItemMeta(meta);
    }

    /**
     * Inject placeholders into the Strings from the provided item given an optional
     * player context. The passed ItemStack is final and will not be modified. Instead,
     * a copy of the ItemStack (which has been injected) is returned.
     *
     * @param player the player context or null if none
     * @param item the item to inject
     *
     * @return the injected ItemStack
     */
    public default ItemStack injectCopy(OfflinePlayer player, final ItemStack item) {
        ItemStack copy = item.clone();
        this.inject(player, copy);
        return copy;
    }

}
