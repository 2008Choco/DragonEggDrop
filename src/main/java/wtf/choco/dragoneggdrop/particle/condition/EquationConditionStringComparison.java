package wtf.choco.dragoneggdrop.particle.condition;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import wtf.choco.dragoneggdrop.utils.JsonUtils;

/**
 * An {@link EquationCondition} implementation. Compares a String value fetched from a {@link ConditionContext}
 * against that of a hard-coded value. Whether or not this condition is met will depend on the result
 * of the predicate passed at this object's construction.
 *
 * @author Parker Hawke - Choco
 */
public class EquationConditionStringComparison implements EquationCondition {

    private final Function<@NotNull ConditionContext, @NotNull String> query;
    private final String value;

    /**
     * Construct a value comparison condition.
     *
     * @param query the function to query a value from a condition context
     * @param value the value against which the queried value should be compared
     */
    public EquationConditionStringComparison(@NotNull Function<@NotNull ConditionContext, @NotNull String> query, @NotNull String value) {
        this.query = query;
        this.value = value;
    }

    @Override
    public boolean isMet(@NotNull ConditionContext context) {
        return query.apply(context).equals(value);
    }

    @NotNull
    public static EquationConditionStringComparison create(@NotNull JsonObject object, @NotNull Function<@NotNull ConditionContext, @NotNull String> query) {
        Preconditions.checkArgument(object != null, "object must not be null");
        Preconditions.checkArgument(query != null, "query must not be null");

        JsonObject argumentsObject = JsonUtils.getRequiredField(object, "arguments", JsonElement::getAsJsonObject);
        String value = JsonUtils.getRequiredField(argumentsObject, "value", JsonElement::getAsString);

        return new EquationConditionStringComparison(query, value);
    }

}
