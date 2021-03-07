package wtf.choco.dragoneggdrop.utils;

/**
 * A utility class holding constants to important Strings in DragonEggDrop
 *
 * @author Parker Hawke - Choco
 */
public final class DEDConstants {

    // Configuration paths
    public static final String CONFIG_METRICS = "metrics";
    public static final String CONFIG_PERFORM_UPDATE_CHECKS = "perform-update-checks";

    public static final String CONFIG_LIGHTNING_AMOUNT = "lightning.amount";
    public static final String CONFIG_LIGHTNING_DAMAGES_ENTITIES = "lightning.damages-entities";
    public static final String CONFIG_RESPAWN_ON_JOIN = "respawn-on-join";
    public static final String CONFIG_JOIN_RESPAWN_DELAY = "join-respawn-delay";
    public static final String CONFIG_RESPAWN_ON_DEATH = "respawn-on-death";
    public static final String CONFIG_DEATH_RESPAWN_DELAY = "death-respawn-delay";

    public static final String CONFIG_ALLOW_CRYSTAL_RESPAWNS = "allow-crystal-respawns";
    public static final String CONFIG_STRICT_COUNTDOWN = "strict-countdown";

    public static final String CONFIG_DISABLED_WORLDS = "disabled-worlds";

    public static final String CONFIG_RESPAWN_MESSAGES_MESSAGES = "respawn-messages.messages";
    public static final String CONFIG_RESPAWN_MESSAGES_CONDENSED = "respawn-messages.condensed";
    public static final String CONFIG_RESPAWN_MESSAGES_OMIT_TIME_UNITS = "respawn-messages.omit-time-units";
    public static final String CONFIG_RESPAWN_MESSAGES_RADIUS = "respawn-messages.radius";

    // Template configuration paths
    public static final String TEMPLATE_DRAGON_NAME = "dragon-name";
    public static final String TEMPLATE_BAR_STYLE = "bar-style";
    public static final String TEMPLATE_BAR_COLOR = "bar-color";
    public static final String TEMPLATE_SPAWN_WEIGHT = "spawn-weight";
    public static final String TEMPLATE_SPAWN_ANNOUNCEMENT = "spawn-announcement";
    public static final String TEMPLATE_ATTRIBUTES = "attributes";
    public static final String TEMPLATE_PARTICLES = "particles";
    public static final String TEMPLATE_LOOT = "loot";

    // Permission nodes
    public static final String PERMISSION_COMMAND_RELOAD = "dragoneggdrop.command.reload";
    public static final String PERMISSION_COMMAND_RESPAWN_STOP = "dragoneggdrop.command.respawn.stop";
    public static final String PERMISSION_COMMAND_RESPAWN_START = "dragoneggdrop.command.respawn.start";
    public static final String PERMISSION_COMMAND_RESPAWN_TEMPLATE = "dragoneggdrop.command.respawn.template";
    public static final String PERMISSION_COMMAND_TEMPLATE_GENERATELOOT = "dragoneggdrop.command.template.generateloot";
    public static final String PERMISSION_COMMAND_TEMPLATE_INFO = "dragoneggdrop.command.template.info";
    public static final String PERMISSION_COMMAND_TEMPLATE_LIST = "dragoneggdrop.command.template.list";
    public static final String PERMISSION_OVERRIDE_CRYSTALS = "dragoneggdrop.overridecrystals";

    // Metadata constants
    public static final String METADATA_LOOT_LIGHTNING = "loot_lightning";

    private DEDConstants() { }

}
