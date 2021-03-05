package wtf.choco.dragoneggdrop.registry;

import java.util.Collection;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a key-value registry.
 *
 * @author Parker Hawke - Choco
 *
 * @param <T> the type of registered object
 */
public interface Registry<T extends Registerable> {

    /**
     * Register a key-value pair.
     *
     * @param value the value to register
     */
    public void register(@NotNull T value);

    /**
     * Unregister a key and its value from this registry.
     *
     * @param key the key to unregister
     *
     * @return the unregistered value. null if no registry entry existed
     */
    @Nullable
    public T unregister(@Nullable String key);

    /**
     * Unregister a value from this registry.
     *
     * @param value the value to unregister
     *
     * @return true if unregistered, false if was not registered
     */
    public boolean unregisterValue(@Nullable T value);

    /**
     * Get a value according to its key in this registry.
     *
     * @param key the key of the value to fetch
     *
     * @return the value
     */
    @Nullable
    public T get(@Nullable String key);

    /**
     * Check whether or not an entry has already been registered with the given key.
     *
     * @param key the key to check
     *
     * @return true if registered, false otherwise
     */
    public boolean isRegistered(@Nullable String key);

    /**
     * Get an unmodifiable Set of the keys registered in this registry.
     *
     * @return all registered keys
     */
    @NotNull
    public Set<@NotNull String> keys();

    /**
     * Get an unmodifiable collection of the values registered in this registry.
     *
     * @return all registered values
     */
    @NotNull
    public Collection<@NotNull T> values();

    /**
     * Get the amount of registered elements in this registry.
     *
     * @return the registry size
     */
    public int size();

    /**
     * Clear this registry.
     */
    public void clear();

}
