package com.ninjaguild.dragoneggdrop.particle.condition;

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
    public boolean isMet(ConditionContext context);

}
