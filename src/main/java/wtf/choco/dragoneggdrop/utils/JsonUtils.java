package wtf.choco.dragoneggdrop.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.function.Function;

/**
 * Various utility methods used to fetch values from a {@link JsonObject}.
 *
 * @author Parker Hawke - Choco
 */
public final class JsonUtils {

    private JsonUtils() { }

    /**
     * Get a field with the given name from the provided {@link JsonObject} and cast it using the
     * provided function. If a value with the required name is not present, a {@link JsonParseException}
     * will be thrown.
     *
     * @param root the object from which to fetch the value
     * @param name the name of the value
     * @param caster the casting function
     *
     * @param <T> the type of object to return
     *
     * @return the fetched value.
     *
     * @throws JsonParseException if the value does not exist
     */
    public static <T> T getRequiredField(JsonObject root, String name, Function<JsonElement, T> caster) {
        if (!root.has(name)) {
            throw new JsonParseException("Missing element \"" + name + "\". This element is required.");
        }

        return caster.apply(root.get(name));
    }

    /**
     * Get a field with the given name from the provided {@link JsonObject} and cast it using the
     * provided function. If a value with the required name is not present, the provided default
     * value will be returned.
     *
     * @param root the object from which to fetch the value
     * @param name the name of the value
     * @param caster the casting function
     * @param defaultValue the value to return if an entry with the given name is not present
     *
     * @param <T> the type of object to return
     *
     * @return the fetched value.
     *
     * @throws JsonParseException if the value does not exist
     */
    public static <T> T getOptionalField(JsonObject root, String name, Function<JsonElement, T> caster, T defaultValue) {
        if (!root.has(name)) {
            return defaultValue;
        }

        return caster.apply(root.get(name));
    }

}
