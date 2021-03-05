package wtf.choco.dragoneggdrop.utils;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class providing command-related utility methods.
 *
 * @author Parker Hawke - Choco
 */
public final class CommandUtils {

    private CommandUtils() { }

    /**
     * Add strings to a {@literal List<String>} if the provided sender has the given permission.
     *
     * @param sender the sender to check
     * @param permission the permission to check
     * @param suggestionList the list to which the suggestions should be added
     * @param suggestions the suggestions to add
     */
    public static void addIfHasPermission(@NotNull CommandSender sender, @NotNull String permission, @NotNull List<@NotNull String> suggestionList, @NotNull String @NotNull... suggestions) {
        if (sender.hasPermission(permission)) {
            for (String suggestion : suggestions) {
                suggestionList.add(suggestion);
            }
        }
    }

    /**
     * Add a String to a {@literal List<String>} if the provided sender has the given permission.
     *
     * @param sender the sender to check
     * @param permission the permission to check
     * @param suggestionList the list to which the suggestions should be added
     * @param suggestion the suggestion to add
     */
    public static void addIfHasPermission(@NotNull CommandSender sender, @NotNull String permission, @NotNull List<@NotNull String> suggestionList, @NotNull String suggestion) {
        if (sender.hasPermission(permission)) {
            suggestionList.add(suggestion);
        }
    }

}
