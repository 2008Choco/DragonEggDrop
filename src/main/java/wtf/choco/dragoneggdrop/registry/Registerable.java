package wtf.choco.dragoneggdrop.registry;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an object capable of being registered to a {@link Registry}.
 *
 * @author Parker Hawke - Choco
 */
public interface Registerable {

    /**
     * Get the unique id for this registerable object.
     *
     * @return the unique id
     */
    @NotNull
    public String getId();

}
