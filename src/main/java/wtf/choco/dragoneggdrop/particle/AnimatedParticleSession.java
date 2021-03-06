package wtf.choco.dragoneggdrop.particle;

import com.google.common.base.Preconditions;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.commons.util.MathUtil;
import wtf.choco.dragoneggdrop.particle.condition.ConditionContext;

/**
 * Represents a set of tickable equation data capable of animating a {@link ParticleShapeDefinition}
 * in the world. Stated data for a specific animation.
 *
 * @author Parker Hawke - Choco
 *
 * @see ParticleShapeDefinition#createSession(World, double, double)
 */
public class AnimatedParticleSession {

    private int animationTick = 0;
    private double theta = 0.0;
    private int wait = 0;

    private final ParticleShapeDefinition shape;
    private final List<@NotNull ConditionalEquationData> equationData;

    private final World world;
    private final Location currentLocation;
    private final ParticleVariables variables;
    private final ConditionContext equationContext;

    AnimatedParticleSession(@NotNull ParticleShapeDefinition definition, @NotNull List<@NotNull ConditionalEquationData> equationData, @NotNull World world, double x, double y, double z) {
        Preconditions.checkArgument(definition != null, "definition must not be null");
        Preconditions.checkArgument(equationData != null, "equationData must not be null");
        Preconditions.checkArgument(world != null, "world must not be null");

        this.shape = definition;
        this.equationData = equationData;

        this.world = world;
        this.currentLocation = new Location(world, x, y, z);
        this.variables = new ParticleVariables();
        this.equationContext = new ConditionContext(variables, world);
    }

    /**
     * Tick this animation.
     */
    public void tick() {
        ConditionalEquationData equationData = getEquationDataForCurrentContext();
        if (equationData == null) {
            return;
        }

        if (++wait < equationData.getFrameIntervalTicks()) {
            return;
        }

        this.animationTick++;
        this.theta += equationData.getThetaIncrement();
        this.variables.update(currentLocation.getX(), currentLocation.getY(), currentLocation.getZ(), animationTick, 0.0);

        double streamSeparationDegrees = 360.0 / Math.max(equationData.getParticleStreams(), 1);
        if (streamSeparationDegrees < 360) { // If there is more than one stream...
            for (int i = 0; i <= 360; i += streamSeparationDegrees) {
                this.displayParticles(equationData);
                this.variables.update(currentLocation.getX(), currentLocation.getY(), currentLocation.getZ(), animationTick, theta += streamSeparationDegrees);
            }
        }
        else {
            this.displayParticles(equationData);
        }

        this.currentLocation.subtract(0.0D, MathUtil.clamp(equationData.getSpeedMultiplier(), 0.1, 2.0), 0.0D);
        this.wait = 0;
    }

    /**
     * Check whether or not this particle session should stop animating. Note that despite the
     * result of this method, this session may still be ticked. The stopping of the animation should
     * be handled by the class calling upon this session.
     *
     * @return true if should stop, false otherwise
     */
    public boolean shouldStop() {
        return currentLocation.getBlock().getType() == Material.BEDROCK || equationData.isEmpty();
    }

    /**
     * Get the {@link Location} at which this session is currently animating.
     *
     * @return the current location
     */
    @NotNull
    public Location getCurrentLocation() {
        return currentLocation.clone();
    }

    /**
     * Get the shape definition being animated by this session.
     *
     * @return the shape definition
     */
    @NotNull
    public ParticleShapeDefinition getShape() {
        return shape;
    }

    @Nullable
    private ConditionalEquationData getEquationDataForCurrentContext() {
        for (ConditionalEquationData equation : equationData) {
            if (equation.isMet(equationContext)) {
                return equation;
            }
        }

        return null;
    }

    private void displayParticles(@NotNull ConditionalEquationData equationData) {
        double x = equationData.getXExpression().evaluate(variables), z = equationData.getZExpression().evaluate(variables);

        this.currentLocation.add(x, 0.0, z);
        this.world.spawnParticle(equationData.getParticle(), currentLocation, equationData.getParticleAmount(), equationData.getParticleOffsetX(), equationData.getParticleOffsetY(), equationData.getParticleOffsetZ(), equationData.getParticleExtra(), null, true);
        this.currentLocation.subtract(x, 0.0, z);
    }

}
