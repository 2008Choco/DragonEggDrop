package wtf.choco.dragoneggdrop.particle.condition;

import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import wtf.choco.dragoneggdrop.utils.JsonUtils;

/**
 * An {@link EquationCondition} implementation. Compares a String value fetched from a {@link ConditionContext}
 * against that of a hard-coded value. Whether or not this condition is met will depend on the result
 * of the predicate passed at this object's construction.
 *
 * @author Parker Hawke - Choco
 */
public class EquationConditionStringComparison implements EquationCondition {

    private final Function<ConditionContext, String> query;
    private final String value;

    /**
     * Construct a value comparison condition.
     *
     * @param query the function to query a value from a condition context
     * @param value the value against which the queried value should be compared
     */
    public EquationConditionStringComparison(Function<ConditionContext, String> query, String value) {
        this.query = query;
        this.value = value;
    }

    @Override
    public boolean isMet(ConditionContext context) {
        return query.apply(context).equals(value);
    }

    public static EquationConditionStringComparison create(JsonObject object, Function<ConditionContext, String> query) {
        JsonObject argumentsObject = JsonUtils.getRequiredField(object, "arguments", JsonElement::getAsJsonObject);
        String value = JsonUtils.getRequiredField(argumentsObject, "value", JsonElement::getAsString);

        return new EquationConditionStringComparison(query, value);
    }

}
