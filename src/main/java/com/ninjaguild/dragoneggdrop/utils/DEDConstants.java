package com.ninjaguild.dragoneggdrop.utils;

/**
 * A utility class holding constants to important Strings in DragonEggDrop
 *
 * @author Parker Hawke - Choco
 */
public final class DEDConstants {

    // Configuration paths
    public static final String CONFIG_METRICS = "metrics";
    public static final String CONFIG_PERFORM_UPDATE_CHECKS = "perform-update-checks";

    public static final String CONFIG_PARTICLES_START_Y = "Particles.egg-start-y";
    public static final String CONFIG_PARTICLES_TYPE = "Particles.type";
    public static final String CONFIG_PARTICLES_AMOUNT = "Particles.amount";
    public static final String CONFIG_PARTICLES_EXTRA = "Particles.extra";
    public static final String CONFIG_PARTICLES_SPEED_MULTIPLIER = "Particles.speed-multiplier";
    public static final String CONFIG_PARTICLES_STREAM_COUNT = "Particles.stream-count";
    public static final String CONFIG_PARTICLES_X_OFFSET = "Particles.xOffset";
    public static final String CONFIG_PARTICLES_Y_OFFSET = "Particles.yOffset";
    public static final String CONFIG_PARTICLES_Z_OFFSET = "Particles.zOffset";
    public static final String CONFIG_PARTICLES_INTERVAL = "Particles.interval";
    public static final String CONFIG_PARTICLES_ADVANCED_PRESET_SHAPE = "Particles.Advanced.preset-shape";
    public static final String CONFIG_PARTICLES_ADVANCED_X_COORD_EXPRESSION = "Particles.Advanced.x-coord-expression";
    public static final String CONFIG_PARTICLES_ADVANCED_Z_COORD_EXPRESSION = "Particles.Advanced.z-coord-expression";

    public static final String CONFIG_LIGHTNING_AMOUNT = "lightning-amount";
    public static final String CONFIG_ALLOW_CRYSTAL_RESPAWNS = "allow-crystal-respawns";
    public static final String CONFIG_STRICT_COUNTDOWN = "strict-countdown";
    public static final String CONFIG_RESPAWN_ON_JOIN = "respawn-on-join";
    public static final String CONFIG_JOIN_RESPAWN_DELAY = "join-respawn-delay";
    public static final String CONFIG_RESPAWN_ON_DEATH = "respawn-on-death";
    public static final String CONFIG_DEATH_RESPAWN_DELAY = "death-respawn-delay";
    public static final String CONFIG_ANNOUNCE_MESSAGES = "announce-messages";
    public static final String CONFIG_ANNOUNCE_MESSAGES_RADIUS = "announce-messages-radius";

    // Template configuration paths
    public static final String TEMPLATE_DRAGON_NAME = "dragon-name";
    public static final String TEMPLATE_BAR_STYLE = "bar-style";
    public static final String TEMPLATE_BAR_COLOR = "bar-color";
    public static final String TEMPLATE_LOOT = "loot";
    public static final String TEMPLATE_SPAWN_WEIGHT = "spawn-weight";
    public static final String TEMPLATE_SPAWN_ANNOUNCEMENT = "spawn-announcement";
    public static final String TEMPLATE_ATTRIBUTES = "attributes";

    // Permission nodes
    public static final String PERMISSION_COMMAND_RELOAD = "dragoneggdrop.command.reload";
    public static final String PERMISSION_COMMAND_RESPAWN_STOP = "dragoneggdrop.command.respawn.stop";
    public static final String PERMISSION_COMMAND_RESPAWN_START = "dragoneggdrop.command.respawn.start";
    public static final String PERMISSION_COMMAND_RESPAWN_TEMPLATE = "dragoneggdrop.command.respawn.template";
    public static final String PERMISSION_COMMAND_TEMPLATE_GENERATELOOT = "dragoneggdrop.command.template.generateloot";
    public static final String PERMISSION_COMMAND_TEMPLATE_INFO = "dragoneggdrop.command.template.info";
    public static final String PERMISSION_COMMAND_TEMPLATE_LIST = "dragoneggdrop.command.template.list";
    public static final String PERMISSION_OVERRIDE_CRYSTALS = "dragoneggdrop.overridecrystals";

    private DEDConstants() { }

}
