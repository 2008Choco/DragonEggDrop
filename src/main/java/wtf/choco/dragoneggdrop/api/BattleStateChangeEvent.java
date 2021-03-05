package wtf.choco.dragoneggdrop.api;

import com.google.common.base.Preconditions;

import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when the state of the EnderDragon battle changes.
 *
 * @author Parker Hawke - Choco
 */
public class BattleStateChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final DragonBattle dragonBattle;
    private final EnderDragon dragon;
    private final BattleState previousState, newState;

    /**
     * Construct a new BattleStateChangeEvent.
     *
     * @param dragonBattle an instance of the EnderDragonBattle
     * @param dragon an instance of the dragon
     * @param previousState the previous state of the battle
     * @param newState the new state of the battle
     */
    public BattleStateChangeEvent(@Nullable DragonBattle dragonBattle, @Nullable EnderDragon dragon, @NotNull BattleState previousState, @NotNull BattleState newState) {
        Preconditions.checkArgument(previousState != null, "previousState must not be null");
        Preconditions.checkArgument(newState != null, "newState must not be null");

        this.dragonBattle = dragonBattle;
        this.dragon = dragon;
        this.previousState = previousState;
        this.newState = newState;
    }

    /**
     * Get an instance of the EnderDragonBattle involved in this event.
     *
     * @return the involved EnderDragonBattle. Can return null
     */
    @Nullable
    public DragonBattle getDragonBattle() {
        return dragonBattle;
    }

    /**
     * Get an instance of the EnderDragon involved in this event.
     *
     * @return the involved dragon. Can return null
     */
    @Nullable
    public EnderDragon getDragon() {
        return dragon;
    }

    /**
     * Get the state that the battle was in prior to this change.
     *
     * @return the previous battle state
     */
    @NotNull
    public BattleState getPreviousState() {
        return previousState;
    }

    /**
     * Get the new state of the battle.
     *
     * @return the new battle state
     */
    @NotNull
    public BattleState getNewState() {
        return newState;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
