package wtf.choco.dragoneggdrop.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.OfflinePlayer;

import wtf.choco.dragoneggdrop.DragonEggDrop;

final class PlaceholderProviderPlaceholderAPI extends PlaceholderExpansion implements PlaceholderProvider {

    private final DragonEggDrop plugin;

    PlaceholderProviderPlaceholderAPI(DragonEggDrop plugin) {
        this.plugin = plugin;
    }

    // PlaceholderExpansion

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return plugin.getDescription().getName().toLowerCase(); // dragoneggdrop
    }

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

    @Override
    public String onRequest(OfflinePlayer player, String placeholder) {
        return DragonEggDropPlaceholders.replacePlaceholder(player, placeholder);
    }

    // PlaceholderProvider

    @Override
    public String inject(OfflinePlayer player, String string) {
        return PlaceholderAPI.setPlaceholders(player, string);
    }

}
