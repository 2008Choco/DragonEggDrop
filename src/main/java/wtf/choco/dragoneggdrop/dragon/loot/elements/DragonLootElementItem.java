package wtf.choco.dragoneggdrop.dragon.loot.elements;

import com.google.common.base.Enums;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Chest;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.boss.DragonBattle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import wtf.choco.dragoneggdrop.dragon.DragonTemplate;
import wtf.choco.dragoneggdrop.placeholder.DragonEggDropPlaceholders;
import wtf.choco.dragoneggdrop.utils.IntegerRange;
import wtf.choco.dragoneggdrop.utils.NamespacedKeyUtil;

/**
 * An implementation of {@link IDragonLootElement} to represent an item.
 *
 * @author Parker Hawke - Choco
 */
public class DragonLootElementItem implements IDragonLootElement {

    private IntegerRange amount, damage;
    private Map<Enchantment, IntegerRange> enchantments;

    private final ItemStack item;
    private final double weight;

    /**
     * Create a {@link DragonLootElementCommand}.
     *
     * @param item the item to generate
     * @param weight this element's weight in the loot pool
     */
    private DragonLootElementItem(ItemStack item, double weight) {
        this.item = item;
        this.weight = weight;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public void generate(DragonBattle battle, DragonTemplate template, Player killer, Random random, Chest chest) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (chest == null) {
            return;
        }

        Inventory inventory = chest.getInventory();
        if (inventory.firstEmpty() == -1) {
            return;
        }

        int slot;
        do {
            slot = random.nextInt(inventory.getSize());
        } while (inventory.getItem(slot) != null);

        ItemStack generated = DragonEggDropPlaceholders.injectCopy(killer, item);
        ItemMeta meta = generated.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(generated.getType());
        }

        // Apply %dragon% placeholder
        if (meta.hasDisplayName()) {
            meta.setDisplayName(meta.getDisplayName().replace("%dragon%", template.getName()));
        }
        if (meta.hasLore()) {
            meta.setLore(meta.getLore().stream().map(s -> s.replace("%dragon%", template.getName())).collect(Collectors.toList()));
        }

        // Generate enchantments
        for (Map.Entry<Enchantment, IntegerRange> enchantmentEntry : enchantments.entrySet()) {
            int level = enchantmentEntry.getValue().getRandomValue(random);
            if (level <= 0) {
                return;
            }

            if (meta instanceof EnchantmentStorageMeta) {
                ((EnchantmentStorageMeta) meta).addStoredEnchant(enchantmentEntry.getKey(), level, true);
            } else {
                meta.addEnchant(enchantmentEntry.getKey(), level, true);
            }
        }

        // Apply damage
        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(damage.getRandomValue(random));
        }

        generated.setItemMeta(meta);
        generated.setAmount(amount.getRandomValue(random));
        inventory.setItem(slot, generated);
    }

    /**
     * Parse a {@link DragonLootElementItem} instance from a {@link JsonObject}.
     *
     * @param root the root element that represents this element
     *
     * @return the created instance
     *
     * @throws JsonParseException if parsing the object has failed
     */
    public static DragonLootElementItem fromJson(JsonObject root) throws JsonParseException {
        double weight = root.has("weight") ? Math.max(root.get("weight").getAsDouble(), 0.0) : 1.0;

        DragonLootElementItemBuilder elementBuilder = new DragonLootElementItemBuilder();

        if (!root.has("type")) {
            throw new JsonParseException("Could not find \"type\" for item in loot pool");
        }

        Material type = Material.matchMaterial(root.get("type").getAsString());
        if (type == null) {
            throw new JsonParseException("Could not create item of type \"" + root.get("type").getAsString() + "\" for item loot pool. Does it exist?");
        }

        ItemStack item = new ItemStack(type);

        // Base meta (ItemMeta)
        if (root.has("amount")) {
            elementBuilder.amount(parseRange(root, "amount"));
        }

        // This should only happen if the item type is air, at which point no other types of meta applies
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new JsonParseException("Could not create item of type \"air\". This is an illegal item.");
        }

        if (root.has("name")) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', root.get("name").getAsString()));
        }

        if (root.has("lore")) {
            JsonElement loreElement = root.get("lore");
            if (!loreElement.isJsonArray()) {
                throw new JsonParseException("Element \"lore\" is of unexpected type. Expected array, got " + loreElement.getClass().getSimpleName());
            }

            List<String> lore = new ArrayList<>();

            for (JsonElement element : loreElement.getAsJsonArray()) {
                if (!element.isJsonPrimitive()) {
                    throw new JsonParseException("Malformated lore in item loot pool. Expected string, got " + element.getClass().getSimpleName());
                }

                lore.add(ChatColor.translateAlternateColorCodes('&', element.getAsString()));
            }

            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
        }

        if (root.has("enchantments")) {
            JsonElement enchantmentsElement = root.get("enchantments");
            if (!enchantmentsElement.isJsonObject()) {
                throw new JsonParseException("Element \"enchantments\" is of unexpected type. Expected object, got " + enchantmentsElement.getClass().getSimpleName());
            }

            JsonObject enchantmentsObject = enchantmentsElement.getAsJsonObject();
            for (Entry<String, JsonElement> enchantmentElement : enchantmentsObject.entrySet()) {
                Enchantment enchantment = Enchantment.getByKey(NamespacedKeyUtil.fromString(enchantmentElement.getKey(), null));
                if (enchantment == null) {
                    throw new JsonParseException("Could not find enchantment with id \"" + enchantmentElement.getKey() + "\" for item loot pool. Does it exist?");
                }

                elementBuilder.enchantment(enchantment, parseRange(enchantmentsObject, enchantmentElement.getKey()));
            }
        }

        if (root.has("damage") && meta instanceof Damageable) {
            IntegerRange damageRange = parseRange(root, "damage");
            if (damageRange.getMax() > type.getMaxDurability()) {
                throw new JsonParseException("Element \"damage\" has a maximum value greater than its type's maximum durability (" + damageRange.getMax() + " > " + type.getMaxDurability() + ")");
            }
            else if (damageRange.getMin() < 0) {
                throw new JsonParseException("Element \"damage\" has a minimum value less than 0");
            }

            elementBuilder.damage(damageRange);
        }

        if (root.has("unbreakable")) {
            meta.setUnbreakable(root.get("unbreakable").getAsBoolean());
        }

        if (root.has("custom_model_data")) {
            meta.setCustomModelData(root.get("custom_model_data").getAsInt());
        }

        if (root.has("attribute_modifiers")) {
            JsonElement modifiersElement = root.get("attribute_modifiers");
            if (!modifiersElement.isJsonObject()) {
                throw new JsonParseException("Element \"attribute_modifiers\" is of unexpected type. Expected object, got " + modifiersElement.getClass().getSimpleName());
            }

            JsonObject modifiersRoot = modifiersElement.getAsJsonObject();
            for (Entry<String, JsonElement> modifierEntry : modifiersRoot.entrySet()) {
                Attribute attribute = Enums.getIfPresent(Attribute.class, modifierEntry.getKey().toUpperCase()).orNull();
                if (attribute == null) {
                    throw new JsonParseException("Unexpected attribute modifier key. Given \"" + modifierEntry.getKey() + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html");
                }

                JsonElement modifierElement = modifierEntry.getValue();
                if (!modifierElement.isJsonObject()) {
                    throw new JsonParseException("Element \"" + modifierEntry.getKey() + "\" is of unexpected type. Expected object, got " + modifiersElement.getClass().getSimpleName());
                }

                JsonObject modifierRoot = modifierElement.getAsJsonObject();
                UUID uuid = modifierRoot.has("uuid") ? UUID.fromString(modifierRoot.get("uuid").getAsString()) : UUID.randomUUID();
                EquipmentSlot slot = modifierRoot.has("slot") ? Enums.getIfPresent(EquipmentSlot.class, modifierRoot.get("slot").getAsString().toUpperCase()).orNull() : null;

                if (!modifierRoot.has("name")) {
                    throw new JsonParseException("Attribute modifier missing element \"name\".");
                }
                if (!modifierRoot.has("value")) {
                    throw new JsonParseException("Attribute modifier missing element \"value\".");
                }
                if (!modifierRoot.has("operation")) {
                    throw new JsonParseException("Attribute modifier missing element \"operation\". Expected \"add_number\", \"add_scalar\" or \"multiply_scalar_1\"");
                }

                String name = modifierRoot.get("name").getAsString();
                double value = modifierRoot.get("value").getAsDouble();
                AttributeModifier.Operation operation = Enums.getIfPresent(AttributeModifier.Operation.class, modifierRoot.get("operation").getAsString().toUpperCase()).orNull();
                if (operation == null) {
                    throw new JsonParseException("Unknown operation for attribute modifier \"" + modifierEntry.getKey() + "\". Expected \"add_number\", \"add_scalar\" or \"multiply_scalar_1\"");
                }

                AttributeModifier modifier = (slot != null) ? new AttributeModifier(uuid, name, value, operation, slot) : new AttributeModifier(uuid, name, value, operation);
                meta.addAttributeModifier(attribute, modifier);
            }
        }

        if (root.has("item_flags")) {
            JsonElement flagsElement = root.get("item_flags");
            if (!flagsElement.isJsonArray()) {
                throw new JsonParseException("Element \"item_flags\" is of unexpected type. Expected array, got " + flagsElement.getClass().getSimpleName());
            }

            flagsElement.getAsJsonArray().forEach(e -> {
                // Guava's Optionals don't have #ifPresent() >:[
                ItemFlag flag = Enums.getIfPresent(ItemFlag.class, e.getAsString().toUpperCase()).orNull();
                if (flag != null) {
                    meta.addItemFlags(flag);
                }
            });
        }

        // Banner meta (BannerMeta)
        if (meta instanceof BannerMeta) {
            BannerMeta metaSpecific = (BannerMeta) meta;

            if (root.has("patterns")) {
                JsonElement patternsElement = root.get("patterns");
                if (!patternsElement.isJsonArray()) {
                    throw new JsonParseException("Element \"patterns\" is of unexpected type. Expected array, got " + patternsElement.getClass().getSimpleName());
                }

                for (JsonElement patternElement : patternsElement.getAsJsonArray()) {
                    if (!patternElement.isJsonObject()) {
                        throw new JsonParseException("Element \"patterns\" has an unexpected type. Expected object, got " + patternElement.getClass().getSimpleName());
                    }

                    JsonObject patternRoot = patternElement.getAsJsonObject();
                    if (!patternRoot.has("color")) {
                        throw new JsonParseException("Pattern missing element \"color\".");
                    }
                    if (!patternRoot.has("pattern")) {
                        throw new JsonParseException("Pattern missing element \"pattern\".");
                    }

                    DyeColor colour = Enums.getIfPresent(DyeColor.class, patternRoot.get("color").getAsString().toUpperCase()).or(DyeColor.WHITE);
                    PatternType pattern = Enums.getIfPresent(PatternType.class, patternRoot.get("pattern").getAsString().toUpperCase()).orNull();
                    if (pattern == null) {
                        throw new JsonParseException("Unexpected value for \"pattern\". Given \"" + root.get("pattern").getAsString() + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/banner/PatternType.html");
                    }

                    metaSpecific.addPattern(new Pattern(colour, pattern));
                }
            }
        }

        // Book meta (BookMeta)
        if (meta instanceof BookMeta) {
            BookMeta metaSpecific = (BookMeta) meta;

            if (root.has("author")) {
                metaSpecific.setAuthor(root.get("author").getAsString());
            }

            if (root.has("title")) {
                metaSpecific.setTitle(root.get("title").getAsString());
            }

            if (root.has("generation")) {
                BookMeta.Generation generation = Enums.getIfPresent(BookMeta.Generation.class, root.get("generation").getAsString().toUpperCase()).orNull();
                if (generation == null) {
                    throw new JsonParseException("Unexpected value for \"generation\". Given \"" + root.get("generation").getAsString() + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/meta/BookMeta.Generation.html");
                }

                metaSpecific.setGeneration(generation);
            }

            if (root.has("pages") && root.get("pages").isJsonArray()) {
                root.getAsJsonArray("pages").forEach(p -> metaSpecific.addPage(p.getAsString()));
            }
        }

        // Firework star meta (FireworkEffectMeta)
        if (meta instanceof FireworkEffectMeta) {
            FireworkEffectMeta metaSpecific = (FireworkEffectMeta) meta;

            if (!root.has("effect")) {
                throw new JsonParseException("Firework effect missing element \"effect\".");
            }

            FireworkEffect.Builder effectBuilder = FireworkEffect.builder();

            FireworkEffect.Type effectType = Enums.getIfPresent(FireworkEffect.Type.class, root.get("effect").getAsString().toUpperCase()).orNull();
            if (effectType == null) {
                throw new JsonParseException("Unexpected value for \"effect\". Given \"" + root.get("effect").getAsString() + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/FireworkEffect.Type.html");
            }

            effectBuilder.with(effectType);
            effectBuilder.flicker(root.has("flicker") ? root.get("flicker").getAsBoolean() : false);
            effectBuilder.flicker(root.has("trail") ? root.get("trail").getAsBoolean() : true);

            if (root.has("color")) {
                JsonElement colourElement = root.get("color");
                if (!colourElement.isJsonObject()) {
                    throw new JsonParseException("Element \"color\" is of unexpected type. Expected object, got " + colourElement.getClass().getSimpleName());
                }

                JsonObject colourRoot = colourElement.getAsJsonObject();
                if (colourRoot.has("primary")) {
                    JsonElement primaryElement = colourRoot.get("primary");

                    if (primaryElement.isJsonPrimitive()) {
                        effectBuilder.withColor(Color.fromRGB(Integer.decode(primaryElement.getAsString())));
                    }

                    else if (primaryElement.isJsonArray()) {
                        JsonArray primaryArray = primaryElement.getAsJsonArray();
                        List<Color> colours = new ArrayList<>(primaryArray.size());
                        primaryArray.forEach(e -> colours.add(Color.fromRGB(Integer.decode(e.getAsString()))));
                        effectBuilder.withColor(colours);
                    }

                    else {
                        throw new JsonParseException("Element \"primary\" is of unexpected type. Expected number (decimal, hex, binary, etc.) or object, got " + primaryElement.getClass().getSimpleName());
                    }
                }

                if (colourRoot.has("fade")) {
                    JsonElement primaryElement = colourRoot.get("fade");

                    if (primaryElement.isJsonPrimitive()) {
                        effectBuilder.withColor(Color.fromRGB(Integer.decode(primaryElement.getAsString())));
                    }

                    else if (primaryElement.isJsonArray()) {
                        JsonArray primaryArray = primaryElement.getAsJsonArray();
                        List<Color> colours = new ArrayList<>(primaryArray.size());
                        primaryArray.forEach(e -> colours.add(Color.fromRGB(Integer.decode(e.getAsString()))));
                        effectBuilder.withColor(colours);
                    }

                    else {
                        throw new JsonParseException("Element \"fade\" is of unexpected type. Expected number (decimal, hex, binary, etc.) or object, got " + primaryElement.getClass().getSimpleName());
                    }
                }
            }

            metaSpecific.setEffect(effectBuilder.build());
        }

        // Firework rocket meta (FireworkMeta)
        if (meta instanceof FireworkMeta) {
            FireworkMeta metaSpecific = (FireworkMeta) meta;

            if (root.has("power")) {
                metaSpecific.setPower(Math.max(root.get("power").getAsInt(), 0));
            }

            if (root.has("effects") && root.get("effects").isJsonArray()) {
                JsonArray effectsArray = root.getAsJsonArray("effects");
                for (JsonElement effectElement : effectsArray) {
                    if (!effectElement.isJsonObject()) {
                        throw new JsonParseException("\"effects\" array element is of unexpected type. Expected object, got " + effectElement.getClass().getSimpleName());
                    }

                    JsonObject effectRoot = effectElement.getAsJsonObject();
                    FireworkEffect.Builder effectBuilder = FireworkEffect.builder();

                    FireworkEffect.Type effectType = Enums.getIfPresent(FireworkEffect.Type.class, effectRoot.get("effect").getAsString().toUpperCase()).orNull();
                    if (effectType == null) {
                        throw new JsonParseException("Unexpected value for \"effect\". Given \"" + effectRoot.get("effect").getAsString() + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/FireworkEffect.Type.html");
                    }

                    effectBuilder.with(effectType);
                    effectBuilder.flicker(effectRoot.has("flicker") ? effectRoot.get("flicker").getAsBoolean() : false);
                    effectBuilder.flicker(effectRoot.has("trail") ? effectRoot.get("trail").getAsBoolean() : true);

                    if (effectRoot.has("color")) {
                        JsonElement colourElement = effectRoot.get("color");
                        if (!colourElement.isJsonObject()) {
                            throw new JsonParseException("Element \"color\" is of unexpected type. Expected JsonObject, got " + colourElement.getClass().getSimpleName());
                        }

                        JsonObject colourRoot = colourElement.getAsJsonObject();
                        if (colourRoot.has("primary")) {
                            JsonElement primaryElement = colourRoot.get("primary");

                            if (primaryElement.isJsonPrimitive()) {
                                effectBuilder.withColor(Color.fromRGB(Integer.decode(primaryElement.getAsString())));
                            }

                            else if (primaryElement.isJsonArray()) {
                                JsonArray primaryArray = primaryElement.getAsJsonArray();
                                List<Color> colours = new ArrayList<>(primaryArray.size());
                                primaryArray.forEach(e -> colours.add(Color.fromRGB(Integer.decode(e.getAsString()))));
                                effectBuilder.withColor(colours);
                            }

                            else {
                                throw new JsonParseException("Element \"primary\" is of unexpected type. Expected number (decimal, hex, binary, etc.) or object, got " + primaryElement.getClass().getSimpleName());
                            }
                        }

                        if (colourRoot.has("fade")) {
                            JsonElement primaryElement = colourRoot.get("fade");

                            if (primaryElement.isJsonPrimitive()) {
                                effectBuilder.withColor(Color.fromRGB(Integer.decode(primaryElement.getAsString())));
                            }

                            else if (primaryElement.isJsonArray()) {
                                JsonArray primaryArray = primaryElement.getAsJsonArray();
                                List<Color> colours = new ArrayList<>(primaryArray.size());
                                primaryArray.forEach(e -> colours.add(Color.fromRGB(Integer.decode(e.getAsString()))));
                                effectBuilder.withColor(colours);
                            }

                            else {
                                throw new JsonParseException("Element \"fade\" is of unexpected type. Expected number (decimal, hex, binary, etc.) or object, got " + primaryElement.getClass().getSimpleName());
                            }
                        }
                    }

                    metaSpecific.addEffect(effectBuilder.build());
                }
            }
        }

        // Knowledge book meta (KnowledgeBookMeta)
        if (meta instanceof KnowledgeBookMeta) {
            KnowledgeBookMeta metaSpecific = (KnowledgeBookMeta) meta;

            if (root.has("recipes")) {
                JsonElement recipesElement = root.get("recipes");
                if (!recipesElement.isJsonArray()) {
                    throw new JsonParseException("Element \"recipes\" is of unexpected type. Expected array, got " + recipesElement.getClass().getSimpleName());
                }

                recipesElement.getAsJsonArray().forEach(e -> {
                    NamespacedKey recipe = NamespacedKeyUtil.fromString(e.getAsString(), null);
                    if (recipe != null) {
                        metaSpecific.addRecipe(recipe);
                    }
                });
            }
        }

        // Leather armour meta (LeatherArmorMeta)
        if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta metaSpecific = (LeatherArmorMeta) meta;

            if (root.has("color")) {
                metaSpecific.setColor(Color.fromRGB(Integer.decode(root.get("color").getAsString())));
            }
        }

        // Map meta (MapMeta)
        if (meta instanceof MapMeta) {
            MapMeta metaSpecific = (MapMeta) meta;

            if (root.has("color")) {
                metaSpecific.setColor(Color.fromRGB(Integer.decode(root.get("color").getAsString())));
            }

            if (root.has("location")) {
                metaSpecific.setLocationName(root.get("location").getAsString());
            }

            if (root.has("scaling")) {
                metaSpecific.setScaling(root.get("scaling").getAsBoolean());
            }
        }

        // Potion meta (PotionMeta)
        if (meta instanceof PotionMeta) {
            PotionMeta metaSpecific = (PotionMeta) meta;

            PotionType basePotionType = (root.has("base")) ? Enums.getIfPresent(PotionType.class, root.get("base").getAsString().toUpperCase()).or(PotionType.UNCRAFTABLE) : PotionType.UNCRAFTABLE;
            boolean upgraded = basePotionType.isUpgradeable() && root.has("upgraded") && root.get("upgraded").getAsBoolean();
            boolean extended = basePotionType.isExtendable() && root.has("extended") && root.get("extended").getAsBoolean();

            metaSpecific.setBasePotionData(new PotionData(basePotionType, upgraded, extended));

            if (root.has("color")) {
                metaSpecific.setColor(Color.fromRGB(Integer.decode(root.get("color").getAsString())));
            }

            if (root.has("effects") && root.get("effects").isJsonObject()) {
                JsonElement effectsElement = root.get("effects");
                if (!effectsElement.isJsonObject()) {
                    throw new JsonParseException("Element \"effects\" is of unexpected type. Expected object, got " + effectsElement.getClass().getSimpleName());
                }

                for (Entry<String, JsonElement> effectElement : effectsElement.getAsJsonObject().entrySet()) {
                    PotionEffectType effect = PotionEffectType.getByName(effectElement.getKey());
                    if (effect == null) {
                        throw new JsonParseException("Could not find potion effect with id \"" + effectElement.getKey() + "\" for item loot pool. Does it exist? https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html");
                    }

                    // Default to 30 seconds
                    int duration = 600, amplifier = 0;
                    boolean ambient = false, particles = true, icon = true;

                    JsonElement effectDataElement = effectElement.getValue();
                    if (effectDataElement.isJsonPrimitive()) {
                        duration = effectDataElement.getAsInt();
                    }

                    else if (effectDataElement.isJsonObject()) {
                        JsonObject effectDataRoot = effectDataElement.getAsJsonObject();

                        if (effectDataRoot.has("duration")) {
                            duration = effectDataRoot.get("duration").getAsInt();
                        }

                        if (effectDataRoot.has("amplifier")) {
                            amplifier = effectDataRoot.get("amplifier").getAsInt();
                        }

                        if (effectDataRoot.has("ambient")) {
                            ambient = effectDataRoot.get("ambient").getAsBoolean();
                        }

                        if (effectDataRoot.has("particles")) {
                            particles = effectDataRoot.get("particles").getAsBoolean();
                        }

                        if (effectDataRoot.has("icon")) {
                            icon = effectDataRoot.get("icon").getAsBoolean();
                        }
                    }

                    else {
                        throw new JsonParseException("Effect element is of unexpected type. Expected number (duration) or object, got " + effectsElement.getClass().getSimpleName());
                    }

                    metaSpecific.addCustomEffect(new PotionEffect(effect, duration, amplifier, ambient, particles, icon), false);
                }
            }
        }

        // Skull meta (SkullMeta)
        if (meta instanceof SkullMeta) {
            SkullMeta metaSpecific = (SkullMeta) meta;

            if (root.has("owner")) {
                metaSpecific.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(root.get("owner").getAsString())));
            }
        }

        // Suspicious Stew Meta (SuspiciousStewMeta)
        if (meta instanceof SuspiciousStewMeta) {
            SuspiciousStewMeta metaSpecific = (SuspiciousStewMeta) meta;

            if (root.has("effects")) {
                JsonObject effectsRoot = root.getAsJsonObject("effects");
                for (Entry<String, JsonElement> effectElement : effectsRoot.entrySet()) {
                    PotionEffectType effect = PotionEffectType.getByName(effectElement.getKey());
                    if (effect == null) {
                        throw new JsonParseException("Could not find potion effect with id \"" + effectElement.getKey() + "\" for item loot pool. Does it exist? https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html");
                    }

                    // Default to 30 seconds
                    int duration = 600, amplifier = 0;
                    boolean ambient = false, particles = true, icon = true;

                    JsonElement effectDataElement = effectElement.getValue();
                    if (effectDataElement.isJsonPrimitive()) {
                        duration = effectDataElement.getAsInt();
                    }

                    else if (effectDataElement.isJsonObject()) {
                        JsonObject effectDataRoot = effectDataElement.getAsJsonObject();

                        if (effectDataRoot.has("duration")) {
                            duration = effectDataRoot.get("duration").getAsInt();
                        }

                        if (effectDataRoot.has("amplifier")) {
                            amplifier = effectDataRoot.get("amplifier").getAsInt();
                        }

                        if (effectDataRoot.has("ambient")) {
                            ambient = effectDataRoot.get("ambient").getAsBoolean();
                        }

                        if (effectDataRoot.has("particles")) {
                            particles = effectDataRoot.get("particles").getAsBoolean();
                        }

                        if (effectDataRoot.has("icon")) {
                            icon = effectDataRoot.get("icon").getAsBoolean();
                        }
                    }

                    else {
                        throw new JsonParseException("Effect element is of unexpected type. Expected number (duration) or object, got " + effectElement.getClass().getSimpleName());
                    }

                    metaSpecific.addCustomEffect(new PotionEffect(effect, duration, amplifier, ambient, particles, icon), false);
                }
            }
        }

        // Fish bucket meta (TropicalFishBucketMeta)
        if (meta instanceof TropicalFishBucketMeta) {
            TropicalFishBucketMeta metaSpecific = (TropicalFishBucketMeta) meta;

            if (root.has("pattern")) {
                TropicalFish.Pattern pattern = Enums.getIfPresent(TropicalFish.Pattern.class, root.get("pattern").getAsString().toUpperCase()).orNull();
                if (pattern == null) {
                    throw new JsonParseException("Unexpected value for \"pattern\". Given \"" + root.get("pattern").getAsString() + "\", expected https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/TropicalFish.Pattern.html");
                }

                metaSpecific.setPattern(pattern);
            }

            if (root.has("color") && root.get("color").isJsonObject()) {
                JsonObject colorRoot = root.getAsJsonObject("color");

                if (colorRoot.has("body")) {
                    metaSpecific.setBodyColor(Enums.getIfPresent(DyeColor.class, colorRoot.get("body").getAsString().toUpperCase()).or(DyeColor.WHITE));
                }

                if (colorRoot.has("pattern")) {
                    metaSpecific.setPatternColor(Enums.getIfPresent(DyeColor.class, colorRoot.get("pattern").getAsString().toUpperCase()).or(DyeColor.WHITE));
                }
            }
        }

        item.setItemMeta(meta);
        return elementBuilder.build(item, weight);
    }

    private static IntegerRange parseRange(JsonObject root, String elementName) {
        JsonElement element = root.get(elementName);
        if (element.isJsonPrimitive()) {
            return IntegerRange.only(Math.max(element.getAsInt(), 0));
        }

        else if (element.isJsonObject()) {
            JsonObject elementObject = element.getAsJsonObject();

            int min = elementObject.has("min") ? Math.max(elementObject.get("min").getAsInt(), 0) : 0;
            int max = elementObject.has("max") ? Math.max(elementObject.get("max").getAsInt(), 0) : min;

            return IntegerRange.between(min, max);
        }

        else {
            throw new JsonParseException("Element \"" + elementName + "\" is of unexpected type. Expected number or object, got " + element.getClass().getSimpleName());
        }
    }

    private static class DragonLootElementItemBuilder {

        private IntegerRange amount, damage;
        private Map<Enchantment, IntegerRange> enchantments;

        private DragonLootElementItemBuilder() { }

        public void amount(IntegerRange amount) {
            this.amount = amount;
        }

        public void damage(IntegerRange damage) {
            this.damage = damage;
        }

        public void enchantment(Enchantment enchantment, IntegerRange levelRange) {
            if (enchantments == null) {
                this.enchantments = new HashMap<>();
            }

            this.enchantments.put(enchantment, levelRange);
        }

        public DragonLootElementItem build(ItemStack item, double weight) {
            DragonLootElementItem element = new DragonLootElementItem(item, weight);

            element.amount = (amount != null ? amount : IntegerRange.only(1));
            element.enchantments = (enchantments != null ? enchantments : Collections.emptyMap());
            element.damage = (damage != null ? damage : IntegerRange.only(0));

            return element;
        }

    }

}
