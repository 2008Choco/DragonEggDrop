package com.ninjaguild.dragoneggdrop.particle.condition;

import com.google.gson.JsonObject;

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
    public boolean isMet(ConditionContext context) {
        return true;
    }

    public static EquationConditionAlwaysTrue create(@SuppressWarnings("unused") JsonObject object) {
        return INSTANCE;
    }

}
