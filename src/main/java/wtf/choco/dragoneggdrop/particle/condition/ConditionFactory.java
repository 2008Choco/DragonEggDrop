package wtf.choco.dragoneggdrop.particle.condition;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.dragoneggdrop.particle.ParticleShapeDefinition;

/**
 * A factory in which {@link EquationCondition} instances may be created. Factory methods must
 * be registered here alongside unique names in order for DragonEggDrop to recognize them when
 * parsing a {@link ParticleShapeDefinition} from a JSON file.
 *
 * @author Parker Hawke
 */
public final class ConditionFactory {

    private static final Map<@NotNull String, @NotNull Function<@NotNull JsonObject, @Nullable ? extends EquationCondition>> FACTORIES = new HashMap<>();

    private ConditionFactory() { }

    /**
     * Register a condition factory method.
     *
     * @param name the condition's unique name
     * @param factoryMethod the method used to create an instance
     */
    public static void registerCondition(@NotNull String name, @NotNull Function<@NotNull JsonObject, @Nullable ? extends EquationCondition> factoryMethod) {
        FACTORIES.put(name, factoryMethod);
    }

    /**
     * Create an {@link EquationCondition} given the JsonObject for the condition.
     *
     * @param name the name of the condition to create
     * @param object the json data for the condition
     *
     * @return the created equation. null if no condition was registered with the given name
     */
    @Nullable
    public static EquationCondition create(@NotNull String name, @NotNull JsonObject object) {
        Preconditions.checkArgument(name != null, "name must not be null");
        Preconditions.checkArgument(object != null, "object must not be null");

        Function<@NotNull JsonObject, @Nullable ? extends EquationCondition> factoryMethod = FACTORIES.get(name);
        return (factoryMethod != null) ? factoryMethod.apply(object) : null;
    }

    /**
     * Clear the condition factory's factory methods
     */
    public static void clear() {
        FACTORIES.clear();
    }

}
