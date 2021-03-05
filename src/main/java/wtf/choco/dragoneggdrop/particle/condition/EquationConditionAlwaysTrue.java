package wtf.choco.dragoneggdrop.particle.condition;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

/**
 * An {@link EquationCondition} implementation. Will always be met.
 *
 * @author Parker Hawke - Choco
 */
public final class EquationConditionAlwaysTrue implements EquationCondition {

    /**
     * The singleton instance for this condition.
     */
    public static final EquationConditionAlwaysTrue INSTANCE = new EquationConditionAlwaysTrue();

    private EquationConditionAlwaysTrue() { }

    @Override
    public boolean isMet(@NotNull ConditionContext context) {
        return true;
    }

    @NotNull
    public static EquationConditionAlwaysTrue create(@SuppressWarnings("unused") @NotNull JsonObject object) {
        return INSTANCE;
    }

}
