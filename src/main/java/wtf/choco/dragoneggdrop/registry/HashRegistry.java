package wtf.choco.dragoneggdrop.registry;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple {@link Registry} implementation backed by a {@link HashMap}.
 *
 * @author Parker Hawke - Choco
 *
 * @param <T> the type of registered object
 */
public class HashRegistry<T extends Registerable> implements Registry<T> {

    private final Map<@NotNull String, @NotNull T> values = new HashMap<>();

    @Override
    public void register(@NotNull T value) {
        Preconditions.checkArgument(value != null, "value must not be null");
        Preconditions.checkArgument(!StringUtils.isEmpty(value.getId()), "key must not be null or empty");

        this.values.put(value.getId(), value);
    }

    @Nullable
    @Override
    public T unregister(@Nullable String key) {
        return values.remove(key);
    }

    @Override
    public boolean unregisterValue(@Nullable T value) {
        return value != null && values.remove(value.getId()) != null;
    }

    @Nullable
    @Override
    public T get(@Nullable String key) {
        return values.get(key);
    }

    @Override
    public boolean isRegistered(@Nullable String key) {
        return values.containsKey(key);
    }

    @NotNull
    @Override
    public Set<@NotNull String> keys() {
        return Collections.unmodifiableSet(values.keySet());
    }

    @NotNull
    @Override
    public Collection<@NotNull T> values() {
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
