package wtf.choco.dragoneggdrop.registry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.commons.collection.RandomCollection;
import wtf.choco.dragoneggdrop.dragon.DragonTemplate;

/**
 * A special {@link Registry} implementation for {@link DragonTemplate DragonTemplates}.
 * This registry also maintains a weighted collection of templates and provides access
 * to {@link #getRandomTemplate()} to fetch a random template according to its weighted
 * value.
 *
 * @author Parker Hawke - Choco
 */
public final class DragonTemplateRegistry extends HashRegistry<DragonTemplate> {

    private final RandomCollection<@NotNull DragonTemplate> weightedTemplates = new RandomCollection<>();

    @Override
    public void register(@NotNull DragonTemplate value) {
        super.register(value);
        this.weightedTemplates.add(value.getSpawnWeight(), value);
    }

    @Nullable
    @Override
    public DragonTemplate unregister(@Nullable String key) {
        DragonTemplate template = super.unregister(key);

        if (template != null) {
            this.weightedTemplates.remove(template);
        }

        return template;
    }

    @Override
    public void clear() {
        super.clear();
        this.weightedTemplates.clear();
    }

    /**
     * Get a random template while taking into consideration its weighted value.
     *
     * @return a random template
     */
    @Nullable
    public DragonTemplate getRandomTemplate() {
        return weightedTemplates.next();
    }

}
