package wtf.choco.dragoneggdrop.particle;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import wtf.choco.dragoneggdrop.particle.condition.ConditionContext;
import wtf.choco.dragoneggdrop.utils.math.MathUtils;

/**
 * Represents a set of tickable equation data capable of animating a {@link ParticleShapeDefinition}
 * in the world. Stated data for a specific animation.
 *
 * @see ParticleShapeDefinition#createSession(World, double, double)
 *
 * @author Parker Hawke - Choco
 */
public class AnimatedParticleSession {

    private int animationTick = 0;
    private double theta = 0.0;
    private int wait = 0;

    private final ParticleShapeDefinition shape;
    private final List<ConditionalEquationData> equationData;

    private final Location currentLocation;
    private final ParticleVariables variables;
    private final ConditionContext equationContext;

    AnimatedParticleSession(ParticleShapeDefinition definition, List<ConditionalEquationData> equationData, World world, double x, double y, double z) {
        this.shape = definition;
        this.equationData = equationData;

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

        this.currentLocation.subtract(0.0D, MathUtils.clamp(equationData.getSpeedMultiplier(), 0.1, 2.0), 0.0D);
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
    public Location getCurrentLocation() {
        return currentLocation.clone();
    }

    /**
     * Get the shape definition being animated by this session.
     *
     * @return the shape definition
     */
    public ParticleShapeDefinition getShape() {
        return shape;
    }

    private ConditionalEquationData getEquationDataForCurrentContext() {
        for (ConditionalEquationData equation : equationData) {
            if (equation.isMet(equationContext)) {
                return equation;
            }
        }

        return null;
    }

    private void displayParticles(ConditionalEquationData equationData) {
        double x = equationData.getXExpression().evaluate(variables), z = equationData.getZExpression().evaluate(variables);

        this.currentLocation.add(x, 0.0, z);
        this.currentLocation.getWorld().spawnParticle(equationData.getParticle(), currentLocation, equationData.getParticleAmount(), equationData.getParticleOffsetX(), equationData.getParticleOffsetY(), equationData.getParticleOffsetZ(), equationData.getParticleExtra(), null, true);
        this.currentLocation.subtract(x, 0.0, z);
    }

}
