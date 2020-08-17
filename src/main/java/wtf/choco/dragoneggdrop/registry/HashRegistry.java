package wtf.choco.dragoneggdrop.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

import org.apache.commons.lang.StringUtils;

/**
 * A simple {@link Registry} implementation backed by a {@link HashMap}.
 *
 * @param <T> the type of registered object
 *
 * @author Parker Hawke - Choco
 */
public class HashRegistry<T extends Registerable> implements Registry<T> {

    private final Map<String, T> values = new HashMap<>();

    @Override
    public void register(T value) {
        Preconditions.checkArgument(value != null, "value must not be null");
        Preconditions.checkArgument(!StringUtils.isEmpty(value.getId()), "key must not be null or empty");

        this.values.put(value.getId(), value);
    }

    @Override
    public T unregister(String key) {
        return values.remove(key);
    }

    @Override
    public boolean unregisterValue(T value) {
        return values.remove(value.getId()) != null;
    }

    @Override
    public T get(String key) {
        return values.get(key);
    }

    @Override
    public boolean isRegistered(String key) {
        return values.containsKey(key);
    }

    @Override
    public Set<String> keys() {
        return Collections.unmodifiableSet(values.keySet());
    }

    @Override
    public Collection<T> values() {
        return Collections.unmodifiableCollection(values.values());
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public void clear() {
        this.values.clear();
    }

}
