package wtf.choco.dragoneggdrop.placeholder;

import org.bukkit.OfflinePlayer;

final class PlaceholderProviderDefault implements PlaceholderProvider {

    private static final String PLACEHOLDER_PREFIX = "dragoneggdrop_";

    PlaceholderProviderDefault() { }

    @Override
    public String inject(OfflinePlayer player, String string) {
        StringBuilder injected = new StringBuilder();

        boolean buffering = false;
        StringBuilder placeholderBuffer = new StringBuilder();

        for (char character : string.toCharArray()) {
            if (character == '%') {
                if (!(buffering = !buffering)) {
                    String placeholder = placeholderBuffer.toString();
                    if (placeholder.startsWith(PLACEHOLDER_PREFIX)) {
                        placeholder = placeholder.substring(PLACEHOLDER_PREFIX.length());
                    }

                    String replacement = DragonEggDropPlaceholders.replacePlaceholder(player, placeholder);

                    if (replacement != null) {
                        injected.append(replacement);
                    } else {
                        injected.append('%').append(placeholderBuffer).append('%');
                    }

                    placeholderBuffer.delete(0, placeholderBuffer.length()).trimToSize();
                }

                continue;
            }

            if (buffering) {
                placeholderBuffer.append(character);
            } else {
                injected.append(character);
            }
        }

        return injected.toString();
    }

}
