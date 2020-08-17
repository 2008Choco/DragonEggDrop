package wtf.choco.dragoneggdrop.particle.condition;

import org.bukkit.World;

import wtf.choco.dragoneggdrop.particle.ParticleVariables;

/**
 * Represents various pieces of data required as context for an {@link EquationCondition}.
 *
 * @author Parker Hawke - Choco
 */
public class ConditionContext {

    private final ParticleVariables variables;
    private final World world;

    /**
     * Construct a new context.
     *
     * @param variables the particle variables
     * @param world the world
     */
    public ConditionContext(ParticleVariables variables, World world) {
        this.variables = variables;
        this.world = world;
    }

    /**
     * Get the particle variables.
     *
     * @return the particle variables
     */
    public ParticleVariables getVariables() {
        return variables;
    }

    /**
     * Get the {@link World} instance.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

}
