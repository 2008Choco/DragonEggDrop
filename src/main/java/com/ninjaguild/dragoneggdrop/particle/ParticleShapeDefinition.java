package com.ninjaguild.dragoneggdrop.particle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.google.common.base.Enums;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.particle.condition.EquationCondition;
import com.ninjaguild.dragoneggdrop.particle.condition.EquationConditionAlwaysTrue;
import com.ninjaguild.dragoneggdrop.particle.condition.EquationConditionValueComparison;
import com.ninjaguild.dragoneggdrop.utils.math.MathUtils;

import org.bukkit.Particle;
import org.bukkit.World;

/**
 * Represents a defined particle shape. Shape definitions may be defined in JSON files in
 * the plugin's particles directory.
 *
 * @author Parker Hawke - Choco
 */
public class ParticleShapeDefinition {

    public static final File PARTICLES_FOLDER = new File(DragonEggDrop.getInstance().getDataFolder(), "particles/");

    private double startY;
    private List<ConditionalEquationData> equationData = new ArrayList<>();

    private final String id;

    /**
     * Construct and parse a new particle shape definition from the given file.
     *
     * @param file the file from which to read definition data
     */
    public ParticleShapeDefinition(File file) {
        Preconditions.checkArgument(file != null, "file cannot be null");

        this.id = file.getName().substring(0, file.getName().lastIndexOf('.')).replace(' ', '_');
        this.readFromFile(file);
    }

    /**
     * Get the string that identifies this shape definition.
     *
     * @return the unique shape definition identifier
     */
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
     * Create an aninated particle session unique to the specified world and coordinates. The created
     * session will represent this shape definition.
     *
     * @param world the world in which to create the session
     * @param x the x coordinate at which the animation should originate
     * @param z the z coordinate at which the animation should originate
     *
     * @return the animated particle session ready to be run
     */
    public AnimatedParticleSession createSession(World world, double x, double z) {
        Preconditions.checkArgument(world != null, "world must not be null");
        return new AnimatedParticleSession(this, equationData, world, x, z);
    }

    private void readFromFile(File file) {
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

        if (root == null) {
            throw new JsonParseException("Invalid root element");
        }

        this.startY = getRequiredField(root, "start_y", JsonElement::getAsDouble);

        JsonObject argumentsObject = getRequiredField(root, "arguments", JsonElement::getAsJsonObject);

        String particleName = getRequiredField(argumentsObject, "particle", JsonElement::getAsString).toUpperCase();
        Particle particle = Enums.getIfPresent(Particle.class, particleName).orNull();
        if (particle == null) {
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

        JsonArray equationsArray = getRequiredField(root, "equations", JsonElement::getAsJsonArray);
        for (JsonElement equationElement : equationsArray) {
            if (!equationElement.isJsonObject()) {
                throw new JsonParseException("Invalid equation element. Expected object, got " + equationElement.getClass().getSimpleName());
            }

            JsonObject equationObject = equationElement.getAsJsonObject();

            String xExpressionString = getRequiredField(equationObject, "x", JsonElement::getAsString);
            String zExpressionString = getRequiredField(equationObject, "z", JsonElement::getAsString);

            ConditionalEquationData equationData = new ConditionalEquationData(MathUtils.parseExpression(xExpressionString), MathUtils.parseExpression(zExpressionString));

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
            particleName = getOptionalField(equationArgumentsRoot, "particle", JsonElement::getAsString, particle.name()).toUpperCase();
            Particle equationParticle = Enums.getIfPresent(Particle.class, particleName).orNull();
            if (equationParticle == null) {
                throw new JsonParseException("Unexpected particle. Given \"" + particleName + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html");
            }

            equationData.particle = equationParticle;
            equationData.particleAmount = getOptionalField(equationArgumentsRoot, "particle_amount", JsonElement::getAsInt, particleAmount);
            equationData.particleExtra = getOptionalField(equationArgumentsRoot, "particle_extra", JsonElement::getAsDouble, particleExtra);
            equationData.particleOffsetX = getOptionalField(equationArgumentsRoot, "particle_offset_x", JsonElement::getAsFloat, particleOffsetX);
            equationData.particleOffsetY = getOptionalField(equationArgumentsRoot, "particle_offset_y", JsonElement::getAsFloat, particleOffsetY);
            equationData.particleOffsetZ = getOptionalField(equationArgumentsRoot, "particle_offset_z", JsonElement::getAsFloat, particleOffsetZ);
            equationData.particleStreams = getOptionalField(equationArgumentsRoot, "particle_streams", JsonElement::getAsInt, particleStreams);

            equationData.speedMultiplier = getOptionalField(equationArgumentsRoot, "speed_multiplier", JsonElement::getAsDouble, speedMultiplier);
            equationData.frameIntervalTicks = getOptionalField(equationArgumentsRoot, "frame_interval_ticks", JsonElement::getAsInt, frameIntervalTicks);
            equationData.thetaIncrement = getOptionalField(equationArgumentsRoot, "theta_increment", JsonElement::getAsInt, thetaIncrement);

            this.equationData.add(equationData);
        }
    }

    private EquationCondition parseCondition(JsonObject conditionObject) {
        String name = getRequiredField(conditionObject, "name", JsonElement::getAsString);

        if (name.equalsIgnoreCase("always_true")) {
            return EquationConditionAlwaysTrue.INSTANCE;
        }
        else if (name.equalsIgnoreCase("y_position")) {
            String operation = getRequiredField(conditionObject, "operation", JsonElement::getAsString);

            BiPredicate<Double, Double> predicate = null;
            if (operation.equalsIgnoreCase("less_than")) {
                predicate = (queried, value) -> queried < value;
            } else if (operation.equalsIgnoreCase("greater_than")) {
                predicate = (queried, value) -> queried > value;
            } else if (operation.equalsIgnoreCase("equal_to")) {
                predicate = (queried, value) -> queried == value;
            } else {
                throw new JsonParseException("Unexpected operation, " + "\"" + operation + "\"");
            }

            JsonObject argumentsObject = getRequiredField(conditionObject, "arguments", JsonElement::getAsJsonObject);
            double value = getRequiredField(argumentsObject, "value", JsonElement::getAsDouble);

            return new EquationConditionValueComparison<>(context -> context.getVariables().getY(), value, predicate);
        }

        throw new JsonParseException("Unexpected condition name, \"" + name + "\"");
    }

    private <T> T getRequiredField(JsonObject root, String name, Function<JsonElement, T> getter) {
        if (!root.has(name)) {
            throw new JsonParseException("Missing element \"" + name + "\". This element is required.");
        }

        return getter.apply(root.get(name));
    }

    private <T> T getOptionalField(JsonObject root, String name, Function<JsonElement, T> getter, T defaultValue) {
        if (!root.has(name)) {
            return defaultValue;
        }

        return getter.apply(root.get(name));
    }

}
