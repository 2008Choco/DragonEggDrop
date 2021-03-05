package wtf.choco.dragoneggdrop.placeholder;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @NotNull
    public String inject(@Nullable OfflinePlayer player, @NotNull String string);

    /**
     * Inject placeholders into the Strings from the provided item given an optional
     * player context.
     *
     * @param player the player context or null if none
     * @param item the item to inject
     */
    @SuppressWarnings("null") // Eclipse is high
    public default void inject(@Nullable OfflinePlayer player, @NotNull ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        // Placeholder injection
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        if (meta.hasDisplayName()) {
            meta.setDisplayName(inject(player, meta.getDisplayName()));
        }

        List<@NotNull String> lore = meta.getLore();
        if (lore != null) {
            meta.setLore(lore.stream().map(s -> inject(player, s)).collect(Collectors.toList()));
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
    @NotNull
    public default ItemStack injectCopy(@Nullable OfflinePlayer player, @NotNull final ItemStack item) {
        Preconditions.checkArgument(item != null, "item must not be null");

        ItemStack copy = item.clone();
        this.inject(player, copy);
        return copy;
    }

}
