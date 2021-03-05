package wtf.choco.dragoneggdrop.particle.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.jetbrains.annotations.NotNull;

import wtf.choco.commons.function.DoubleBiPredicate;
import wtf.choco.commons.function.DoubleProvider;
import wtf.choco.dragoneggdrop.utils.JsonUtils;

/**
 * An {@link EquationCondition} implementation. Compares a double value fetched from a {@link ConditionContext}
 * against that of a hard-coded value. Whether or not this condition is met will depend on the result
 * of the predicate passed at this object's construction.
 *
 * @author Parker Hawke - Choco
 */
public class EquationConditionDoubleComparison implements EquationCondition {

    private final DoubleProvider<@NotNull ConditionContext> query;
    private final double value;
    private final DoubleBiPredicate predicate;

    /**
     * Construct a value comparison condition.
     *
     * @param query the function to query a value from a condition context
     * @param value the value against which the queried value should be compared
     * @param predicate the comparison predicate
     */
    public EquationConditionDoubleComparison(@NotNull DoubleProvider<@NotNull ConditionContext> query, double value, @NotNull DoubleBiPredicate predicate) {
        this.query = query;
        this.value = value;
        this.predicate = predicate;
    }

    @Override
    public boolean isMet(@NotNull ConditionContext context) {
        return predicate.test(query.get(context), value);
    }

    @NotNull
    public static EquationConditionDoubleComparison create(@NotNull JsonObject object, @NotNull DoubleProvider<@NotNull ConditionContext> query) {
        String operation = JsonUtils.getRequiredField(object, "operation", JsonElement::getAsString);

        DoubleBiPredicate predicate = null;
        if (operation.equalsIgnoreCase("less_than") || operation.equals("<")) {
            predicate = (queried, value) -> queried < value;
        } else if (operation.equalsIgnoreCase("greater_than") || operation.equals(">")) {
            predicate = (queried, value) -> queried > value;
        } else if (operation.equalsIgnoreCase("equal_to") || operation.equals("=")) {
            predicate = (queried, value) -> queried == value;
        } else {
            throw new JsonParseException("Unexpected operation, " + "\"" + operation + "\"");
        }

        JsonObject argumentsObject = JsonUtils.getRequiredField(object, "arguments", JsonElement::getAsJsonObject);
        double value = JsonUtils.getRequiredField(argumentsObject, "value", JsonElement::getAsDouble);

        return new EquationConditionDoubleComparison(query, value, predicate);
    }

}
