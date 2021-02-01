package wtf.choco.dragoneggdrop.utils;

import com.google.common.base.Preconditions;

import java.util.Locale;
import java.util.regex.Pattern;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

/**
 * A utility class to better handle {@link NamespacedKey} objects.
 *
 * @author Parker Hawke - Choco
 */
public final class NamespacedKeyUtil {

    // Pulled from Bukkit's NamespacedKey
    private static final Pattern VALID_KEY = Pattern.compile("[a-z0-9/._-]+");

    private NamespacedKeyUtil() { }

    /**
     * Get a NamespacedKey from the supplied string with a default namespace if
     * a namespace is not defined. This is a utility method meant to fetch a
     * NamespacedKey from user input. Please note that casing does matter and any
     * instance of uppercase characters will be considered invalid. The input contract
     * is as follows:
     * <pre>
     * fromString("foo", plugin) -> "plugin:foo"
     * fromString("foo:bar", plugin) -> "foo:bar"
     * fromString("foo", null) -> "minecraft:foo"
     * fromString("Foo", plugin) -> null
     * fromString(":fOO", plugin) -> null
     * fromString("foo:bar:bazz", plugin) -> null
     * fromString("", plugin) -> null
     * </pre>
     *
     * @param key the key to convert to a NamespacedKey
     * @param defaultNamespace the default namespace to use if none was supplied.
     * If null, the minecraft namespace will be used
     *
     * @return the created NamespacedKey. null if invalid key
     */
    @SuppressWarnings("deprecation")
    public static NamespacedKey fromString(String key, Plugin defaultNamespace) {
        Preconditions.checkArgument(key != null && !key.isEmpty(), "Input string must not be empty or null");

        String[] components = key.split(":", 3);
        if (components.length > 2 || (components.length == 1 && !VALID_KEY.matcher(key).matches())) {
            return null;
        }

        if (!key.contains(":")) {
            return (defaultNamespace != null) ? new NamespacedKey(defaultNamespace, key) : NamespacedKey.minecraft(key);
        }

        String namespace = components[0].toLowerCase(Locale.ROOT);
        if (namespace.isEmpty()) {
            return (defaultNamespace != null) ? new NamespacedKey(defaultNamespace, components[1]) : NamespacedKey.minecraft(key);
        }

        return new NamespacedKey(namespace, components[1].toLowerCase(Locale.ROOT));
    }

    /**
     * Check whether or not the provided string is a valid key for a
     * {@link NamespacedKey}.
     *
     * @param string the string to check
     *
     * @return true if valid, false otherwise
     */
    public static boolean isValidKey(String string) {
        return VALID_KEY.matcher(string).matches();
    }

}
