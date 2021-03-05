package wtf.choco.dragoneggdrop.particle.condition;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a condition to be checked before evaluating equations.
 *
 * @author Parker Hawke - Choco
 */
public interface EquationCondition {

    /**
     * Check whether or not this condition has been met given the provided context.
     *
     * @param context the condition context
     *
     * @return true if met, false otherwise
     */
    public boolean isMet(@NotNull ConditionContext context);

}
