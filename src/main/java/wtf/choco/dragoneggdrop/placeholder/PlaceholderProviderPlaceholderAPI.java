package wtf.choco.dragoneggdrop.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.dragoneggdrop.DragonEggDrop;

final class PlaceholderProviderPlaceholderAPI extends PlaceholderExpansion implements PlaceholderProvider {

    private final DragonEggDrop plugin;

    PlaceholderProviderPlaceholderAPI(@NotNull DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    // PlaceholderExpansion

    @NotNull
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return plugin.getDescription().getName().toLowerCase(); // dragoneggdrop
    }

    @NotNull
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Nullable
    @Override
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String placeholder) {
        return DragonEggDropPlaceholders.replacePlaceholder(player, placeholder);
    }

    // PlaceholderProvider

    @NotNull
    @Override
    public String inject(@Nullable OfflinePlayer player, @NotNull String string) {
        return PlaceholderAPI.setPlaceholders(player, string);
    }

}
