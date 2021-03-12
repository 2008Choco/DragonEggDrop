package wtf.choco.dragoneggdrop.world;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.time.Instant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import wtf.choco.dragoneggdrop.DragonEggDrop;
import wtf.choco.dragoneggdrop.dragon.DamageHistory;
import wtf.choco.dragoneggdrop.dragon.DragonTemplate;
import wtf.choco.dragoneggdrop.dragon.loot.DragonLootTable;
import wtf.choco.dragoneggdrop.utils.JsonUtils;

/**
 * Represents a recorded battle state from a previous dragon battle.
 *
 * @author Parker Hawke - Choco
 */
public class DragonBattleRecord {

    private final EndWorldWrapper world;
    private final DragonTemplate template;
    private final DragonLootTable lootTable;
    private final DamageHistory damageHistory;
    private final long deathTimestamp;

    /**
     * Construct a new {@link DragonBattleRecord}.
     *
     * @param world the world in which the battle occurred
     * @param template the dragon template in this battle
     * @param damageHistory the damage history instance from this battle
     * @param deathTimestamp the timestamp at which the dragon was slain
     * @param lootTable the loot table used in this battle (may be null)
     */
    public DragonBattleRecord(@NotNull EndWorldWrapper world, @NotNull DragonTemplate template, @NotNull DamageHistory damageHistory, long deathTimestamp, @Nullable DragonLootTable lootTable) {
        Preconditions.checkArgument(world != null, "world must not be null");
        Preconditions.checkArgument(template != null, "template must not be null");
        Preconditions.checkArgument(damageHistory != null, "damageHistory must not be null");
        Preconditions.checkArgument(deathTimestamp >= 0, "deathTimestamp must be positive or 0");

        this.world = world;
        this.template = template;
        this.damageHistory = damageHistory;
        this.deathTimestamp = deathTimestamp;
        this.lootTable = lootTable != null ? lootTable : template.getLootTable();
    }

    /**
     * Construct a new {@link DragonBattleRecord}.
     *
     * @param world the world in which the battle occurred
     * @param template the dragon template in this battle
     * @param damageHistory the damage history instance from this battle
     * @param deathTimestamp the timestamp at which the dragon was slain
     */
    public DragonBattleRecord(@NotNull EndWorldWrapper world, @NotNull DragonTemplate template, @NotNull DamageHistory damageHistory, long deathTimestamp) {
        this(world, template, damageHistory, deathTimestamp, template.getLootTable());
    }

    /**
     * Get the {@link EndWorldWrapper} in which this battle occurred.
     *
     * @return the world
     */
    @NotNull
    public EndWorldWrapper getWorld() {
        return world;
    }

    /**
     * Get the {@link DragonTemplate} that was used in this battle.
     *
     * @return the template
     */
    @NotNull
    public DragonTemplate getTemplate() {
        return template;
    }

    /**
     * Get the {@link DragonLootTable} that was used in this battle. If a loot table override
     * was not used, this method will return the result of {@link DragonTemplate#getLootTable()}
     * for convenience, meaning that this method follows the same nullability standards of
     * the aforementioned method.
     *
     * @return the loot table
     */
    @Nullable
    public DragonLootTable getLootTable() {
        return lootTable;
    }

    /**
     * Get the {@link DamageHistory} instance of the dragon from this battle.
     *
     * @return the damage history
     */
    @NotNull
    public DamageHistory getDamageHistory() {
        return damageHistory;
    }

    /**
     * Get the timestamp at which the dragon was slain (relative to epoch time).
     *
     * @return the death timestamp
     *
     * @see #getDeathInstant()
     */
    public long getDeathTimestamp() {
        return deathTimestamp;
    }

    /**
     * Get the death timestamp wrapped as an {@link Instant}.
     *
     * @return the death instant
     *
     * @see #getDeathTimestamp()
     */
    @NotNull
    public Instant getDeathInstant() {
        return Instant.ofEpochMilli(deathTimestamp);
    }

    /**
     * Serialize this battle record as a {@link JsonObject}. This serialized object does not contain
     * the world ({@link #getWorld()}). When deserialized with {@link #fromJson(EndWorldWrapper, JsonObject)},
     * an {@link EndWorldWrapper} will need to be passed.
     *
     * @return the json object
     */
    @NotNull
    public JsonObject toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("template", template.getId());

        if (lootTable != null) {
            object.addProperty("lootTable", lootTable.getId());
        }

        // TODO: Add damage history
        object.addProperty("deathTimestamp", deathTimestamp);

        return object;
    }

    /**
     * Deserialize a {@link DragonBattleRecord} from the given {@link JsonObject}.
     *
     * @param world the world to which the battle record should be associated
     * @param object the json object
     *
     * @return the deserialized dragon battle record
     */
    @NotNull
    public static DragonBattleRecord fromJson(@NotNull EndWorldWrapper world, @NotNull JsonObject object) {
        Preconditions.checkArgument(world != null, "world must not be null");
        Preconditions.checkArgument(object != null, "object must not be null");

        String templateId = JsonUtils.getRequiredField(object, "template", JsonElement::getAsString);
        String lootTableId = JsonUtils.getOptionalField(object, "lootTable", JsonElement::getAsString, "");
        long deathTimestamp = JsonUtils.getOptionalField(object, "deathTimestamp", JsonElement::getAsLong, 0L);
        // TODO: Damage history

        DragonEggDrop plugin = DragonEggDrop.getInstance();

        DragonTemplate template = plugin.getDragonTemplateRegistry().get(templateId);
        if (template == null) {
            throw new JsonParseException("Unknown dragon template with id " + templateId);
        }

        DragonLootTable lootTable = plugin.getLootTableRegistry().get(lootTableId); // Can be null

        return new DragonBattleRecord(world, template, DamageHistory.dummy(), deathTimestamp, lootTable); // TODO: Damage history
    }

}
