package wtf.choco.dragoneggdrop.utils;

import com.google.common.base.Enums;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

public final class ConfigUtils {

    private ConfigUtils() { }

    /**
     * Get an array of {@link TimeUnit} instances by name according to the list of strings.
     *
     * @param timeUnitStrings the time unit names
     *
     * @return the time units
     */
    @NotNull
    public static TimeUnit[] getTimeUnits(@NotNull List<@NotNull String> timeUnitStrings) {
        if (timeUnitStrings == null || timeUnitStrings.isEmpty()) {
            return new TimeUnit[0];
        }

        return timeUnitStrings.stream()
                .map(string -> Enums.getIfPresent(TimeUnit.class, string.trim().toUpperCase()).orNull())
                .filter(unit -> unit != null)
                .distinct().toArray(TimeUnit[]::new);
    }

}
