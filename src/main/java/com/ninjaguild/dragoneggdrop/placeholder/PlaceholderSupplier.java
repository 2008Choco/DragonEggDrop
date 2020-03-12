package com.ninjaguild.dragoneggdrop.placeholder;

import org.bukkit.OfflinePlayer;

@FunctionalInterface
public interface PlaceholderSupplier {

    public String get(OfflinePlayer player);

}
