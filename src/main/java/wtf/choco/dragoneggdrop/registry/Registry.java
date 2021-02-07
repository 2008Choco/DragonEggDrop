package wtf.choco.dragoneggdrop.registry;

import java.util.Collection;
import java.util.Set;

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
    public void register(T value);

    /**
     * Unregister a key and its value from this registry.
     *
     * @param key the key to unregister
     *
     * @return the unregistered value. null if no registry entry existed
     */
    public T unregister(String key);

    /**
     * Unregister a value from this registry.
     *
     * @param value the value to unregister
     *
     * @return true if unregistered, false if was not registered
     */
    public boolean unregisterValue(T value);

    /**
     * Get a value according to its key in this registry.
     *
     * @param key the key of the value to fetch
     *
     * @return the value
     */
    public T get(String key);

    /**
     * Check whether or not an entry has already been registered with the given key.
     *
     * @param key the key to check
     *
     * @return true if registered, false otherwise
     */
    public boolean isRegistered(String key);

    /**
     * Get an unmodifiable Set of the keys registered in this registry.
     *
     * @return all registered keys
     */
    public Set<String> keys();

    /**
     * Get an unmodifiable collection of the values registered in this registry.
     *
     * @return all registered values
     */
    public Collection<T> values();

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
