package wtf.choco.dragoneggdrop.particle;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.particle.condition.ConditionFactory;
import wtf.choco.dragoneggdrop.particle.condition.EquationCondition;
import wtf.choco.dragoneggdrop.particle.condition.EquationConditionAlwaysTrue;
import wtf.choco.dragoneggdrop.particle.condition.EquationConditionDoubleComparison;
import wtf.choco.dragoneggdrop.particle.condition.EquationConditionStringComparison;
import wtf.choco.dragoneggdrop.registry.Registerable;
import wtf.choco.dragoneggdrop.utils.math.ExpressionUtils;

import static wtf.choco.dragoneggdrop.utils.JsonUtils.getOptionalField;
import static wtf.choco.dragoneggdrop.utils.JsonUtils.getRequiredField;

/**
 * Represents a defined particle shape. Shape definitions may be defined in JSON files in
 * the plugin's particles directory.
 *
 * @author Parker Hawke - Choco
 */
public class ParticleShapeDefinition implements Registerable {

    static {
        ConditionFactory.registerCondition("always_true", EquationConditionAlwaysTrue::create);
        ConditionFactory.registerCondition("x_position", json -> EquationConditionDoubleComparison.create(json, context -> context != null ? context.getVariables().getX() : 0.0));
        ConditionFactory.registerCondition("y_position", json -> EquationConditionDoubleComparison.create(json, context -> context != null ? context.getVariables().getY() : 0.0));
        ConditionFactory.registerCondition("z_position", json -> EquationConditionDoubleComparison.create(json, context -> context != null ? context.getVariables().getZ() : 0.0));
        ConditionFactory.registerCondition("t", json -> EquationConditionDoubleComparison.create(json, context -> context != null ? context.getVariables().getT() : 0.0));
        ConditionFactory.registerCondition("theta", json -> EquationConditionDoubleComparison.create(json, context -> context != null ? context.getVariables().getTheta() : 0.0));
        ConditionFactory.registerCondition("world", json -> EquationConditionStringComparison.create(json, context -> context.getWorld().getName()));
    }

    private double startY;
    private List<@NotNull ConditionalEquationData> equationData = new ArrayList<>();

    private final String id;

    /**
     * Construct a new {@link ParticleShapeDefinition}.
     *
     * @param id the unique id of this shape definition
     * @param startY the starting y coordinate of this shape definition
     * @param equationData this shape definition's equation data
     */
    public ParticleShapeDefinition(@NotNull String id, double startY, @NotNull List<@NotNull ConditionalEquationData> equationData) {
        Preconditions.checkArgument(id != null, "id cannot be null");
        Preconditions.checkArgument(startY >= 0, "startY must be >= 0");
        Preconditions.checkArgument(equationData != null, "equationData must not be null");

        this.id = id;
        this.startY = startY;
        this.equationData = new ArrayList<>(equationData);
    }

    /**
     * Construct a new {@link ParticleShapeDefinition} with empty equation data.
     *
     * @param id the unique id of this shape definition
     * @param startY the starting y coordinate of this shape definition
     */
    public ParticleShapeDefinition(@NotNull String id, double startY) {
        this(id, startY, new ArrayList<>());
    }

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    /**
     * Get the y coordinate at which this shape should start.
     *
     * @return the starting y coordinate
     */
    public double getStartY() {
        return startY;
    }

    /**
     * Create an animated particle session unique to the specified world and coordinates. The created
     * session will represent this shape definition.
     *
     * @param location the location at which the animation should originate
     *
     * @return the animated particle session ready to be run
     */
    @NotNull
    public AnimatedParticleSession createSession(@NotNull Location location) {
        Preconditions.checkArgument(location != null, "location must not be null");
        Preconditions.checkArgument(location.getWorld() != null, "world must not be null");

        World world = location.getWorld();
        assert world != null;

        return createSession(world, location.getX(), location.getY(), location.getZ());
    }

    /**
     * Create an animated particle session unique to the specified world and coordinates. The created
     * session will represent this shape definition.
     *
     * @param world the world in which to create the session
     * @param x the x coordinate at which the animation should originate
     * @param y the y coordinate at which the animation should originate
     * @param z the z coordinate at which the animation should originate
     *
     * @return the animated particle session ready to be run
     */
    @NotNull
    public AnimatedParticleSession createSession(@NotNull World world, double x, double y, double z) {
        Preconditions.checkArgument(world != null, "world must not be null");

        return new AnimatedParticleSession(this, equationData, world, x, y, z);
    }

    /**
     * Create an animated particle session unique to the specified world and coordinates. The created
     * session will represent this shape definition. This session will start at {@link #getStartY()}.
     *
     * @param world the world in which to create the session
     * @param x the x coordinate at which the animation should originate
     * @param z the z coordinate at which the animation should originate
     *
     * @return the animated particle session ready to be run
     */
    @NotNull
    public AnimatedParticleSession createSession(@NotNull World world, double x, double z) {
        return createSession(world, x, getStartY(), z);
    }

    /**
     * Load and create a {@link ParticleShapeDefinition} from a JSON {@link File}.
     *
     * @param file the file from which to parse a shape definition
     *
     * @return the shape definition
     */
    @NotNull
    public static ParticleShapeDefinition fromFile(@NotNull File file) {
        Preconditions.checkArgument(file != null, "file must not be null");

        String fileName = file.getName();
        if (!fileName.endsWith(".json")) {
            throw new IllegalArgumentException("Expected .json file. Got " + fileName.substring(fileName.lastIndexOf('.')) + " instead");
        }

        JsonObject root = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            root = DragonEggDrop.GSON.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            throw new JsonParseException(e.getMessage(), e.getCause());
        }

        String id = fileName.substring(0, fileName.lastIndexOf('.')).replace(' ', '_');
        double startY = getRequiredField(root, "start_y", JsonElement::getAsDouble);

        JsonObject argumentsObject = getRequiredField(root, "arguments", JsonElement::getAsJsonObject);

        String particleName = getRequiredField(argumentsObject, "particle", JsonElement::getAsString).toUpperCase();
        Optional<@NotNull Particle> particle = Enums.getIfPresent(Particle.class, particleName);
        if (!particle.isPresent()) {
            throw new JsonParseException("Unexpected particle. Given \"" + particleName + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html");
        }

        int particleAmount = getRequiredField(argumentsObject, "particle_amount", JsonElement::getAsInt);
        double particleExtra = getRequiredField(argumentsObject, "particle_extra", JsonElement::getAsDouble);
        float particleOffsetX = getRequiredField(argumentsObject, "particle_offset_x", JsonElement::getAsFloat);
        float particleOffsetY = getRequiredField(argumentsObject, "particle_offset_y", JsonElement::getAsFloat);
        float particleOffsetZ = getRequiredField(argumentsObject, "particle_offset_z", JsonElement::getAsFloat);
        int particleStreams = getRequiredField(argumentsObject, "particle_streams", JsonElement::getAsInt);

        double speedMultiplier = getRequiredField(argumentsObject, "speed_multiplier", JsonElement::getAsDouble);
        int frameIntervalTicks = getRequiredField(argumentsObject, "frame_interval_ticks", JsonElement::getAsInt);
        int thetaIncrement = getRequiredField(argumentsObject, "theta_increment", JsonElement::getAsInt);

        List<ConditionalEquationData> equationDataList = new ArrayList<>();

        JsonArray equationsArray = getRequiredField(root, "equations", JsonElement::getAsJsonArray);
        for (JsonElement equationElement : equationsArray) {
            if (!equationElement.isJsonObject()) {
                throw new JsonParseException("Invalid equation element. Expected object, got " + equationElement.getClass().getSimpleName());
            }

            JsonObject equationObject = equationElement.getAsJsonObject();

            String xExpressionString = getRequiredField(equationObject, "x", JsonElement::getAsString);
            String zExpressionString = getRequiredField(equationObject, "z", JsonElement::getAsString);

            ConditionalEquationData equationData = new ConditionalEquationData(ExpressionUtils.parseExpression(xExpressionString), ExpressionUtils.parseExpression(zExpressionString));

            if (equationObject.has("conditions")) {
                JsonElement conditionsElement = equationObject.get("conditions");
                if (!conditionsElement.isJsonArray()) {
                    throw new JsonParseException("Invalid conditions element. Expected array, got " + conditionsElement.getClass().getSimpleName());
                }

                for (JsonElement conditionElement : conditionsElement.getAsJsonArray()) {
                    if (!conditionElement.isJsonObject()) {
                        throw new JsonParseException("Invalid condition element. Expected object, got " + conditionElement.getClass().getSimpleName());
                    }

                    equationData.addCondition(parseCondition(conditionElement.getAsJsonObject()));
                }
            }

            // Kind of cheating here. If we don't have an "arguments" tag in our equation data, use the argumentsObject from root instead
            JsonElement equationArgumentsElement = getOptionalField(equationObject, "arguments", JsonElement::getAsJsonObject, argumentsObject);
            if (!equationArgumentsElement.isJsonObject()) {
                throw new JsonParseException("Invalid arguments element. Expected object, got " + equationArgumentsElement.getClass().getSimpleName());
            }

            JsonObject equationArgumentsRoot = equationArgumentsElement.getAsJsonObject();
            particleName = getOptionalField(equationArgumentsRoot, "particle", JsonElement::getAsString, particle.get().name()).toUpperCase();
            Optional<@NotNull Particle> equationParticle = Enums.getIfPresent(Particle.class, particleName);
            if (!equationParticle.isPresent()) {
                throw new JsonParseException("Unexpected particle. Given \"" + particleName + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html");
            }

            equationData.particle = equationParticle.get();
            equationData.particleAmount = getOptionalField(equationArgumentsRoot, "particle_amount", JsonElement::getAsInt, particleAmount);
            equationData.particleExtra = getOptionalField(equationArgumentsRoot, "particle_extra", JsonElement::getAsDouble, particleExtra);
            equationData.particleOffsetX = getOptionalField(equationArgumentsRoot, "particle_offset_x", JsonElement::getAsFloat, particleOffsetX);
            equationData.particleOffsetY = getOptionalField(equationArgumentsRoot, "particle_offset_y", JsonElement::getAsFloat, particleOffsetY);
            equationData.particleOffsetZ = getOptionalField(equationArgumentsRoot, "particle_offset_z", JsonElement::getAsFloat, particleOffsetZ);
            equationData.particleStreams = getOptionalField(equationArgumentsRoot, "particle_streams", JsonElement::getAsInt, particleStreams);

            equationData.speedMultiplier = getOptionalField(equationArgumentsRoot, "speed_multiplier", JsonElement::getAsDouble, speedMultiplier);
            equationData.frameIntervalTicks = getOptionalField(equationArgumentsRoot, "frame_interval_ticks", JsonElement::getAsInt, frameIntervalTicks);
            equationData.thetaIncrement = getOptionalField(equationArgumentsRoot, "theta_increment", JsonElement::getAsInt, thetaIncrement);

            equationDataList.add(equationData);
        }

        return new ParticleShapeDefinition(id, startY, equationDataList);
    }

    private static EquationCondition parseCondition(JsonObject conditionObject) {
        String name = getRequiredField(conditionObject, "name", JsonElement::getAsString);

        EquationCondition condition = ConditionFactory.create(name, conditionObject);
        if (condition == null) {
            throw new JsonParseException("Unexpected condition name, \"" + name + "\". (Names are case sensitive)");
        }

        return condition;
    }

}
