package com.ninjaguild.dragoneggdrop.particle.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ninjaguild.dragoneggdrop.utils.JsonUtils;
import com.ninjaguild.dragoneggdrop.utils.function.DoubleBiPredicate;
import com.ninjaguild.dragoneggdrop.utils.function.DoubleProvider;

/**
 * An {@link EquationCondition} implementation. Compares a double value fetched from a {@link ConditionContext}
 * against that of a hard-coded value. Whether or not this condition is met will depend on the result
 * of the predicate passed at this object's construction.
 *
 * @author Parker Hawke - Choco
 */
public class EquationConditionDoubleComparison implements EquationCondition {

    private final DoubleProvider<ConditionContext> query;
    private final double value;
    private final DoubleBiPredicate predicate;

    /**
     * Construct a value comparison condition.
     *
     * @param query the function to query a value from a condition context
     * @param value the value against which the queried value should be compared
     * @param predicate the comparison predicate
     */
    public EquationConditionDoubleComparison(DoubleProvider<ConditionContext> query, double value, DoubleBiPredicate predicate) {
        this.query = query;
        this.value = value;
        this.predicate = predicate;
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return predicate.test(query.get(context), value);
    }

    public static EquationConditionDoubleComparison create(JsonObject object, DoubleProvider<ConditionContext> query) {
        String operation = JsonUtils.getRequiredField(object, "operation", JsonElement::getAsString);

        DoubleBiPredicate predicate = null;
        if (operation.equalsIgnoreCase("less_than")) {
            predicate = (queried, value) -> queried < value;
        } else if (operation.equalsIgnoreCase("greater_than")) {
            predicate = (queried, value) -> queried > value;
        } else if (operation.equalsIgnoreCase("equal_to")) {
            predicate = (queried, value) -> queried == value;
        } else {
            throw new JsonParseException("Unexpected operation, " + "\"" + operation + "\"");
        }

        JsonObject argumentsObject = JsonUtils.getRequiredField(object, "arguments", JsonElement::getAsJsonObject);
        double value = JsonUtils.getRequiredField(argumentsObject, "value", JsonElement::getAsDouble);

        return new EquationConditionDoubleComparison(query, value, predicate);
    }

}
