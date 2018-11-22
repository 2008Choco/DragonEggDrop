package com.ninjaguild.dragoneggdrop.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.ninjaguild.dragoneggdrop.utils.ItemBuilder.WrappedItemStackResult.Result;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

/**
 * A utility class to assist in the creation of ItemStacks in the confines of a single line.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class ItemBuilder {

	private static final Set<Material> ILLEGAL_TYPES = EnumSet.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);

	private final ItemStack item;
	private final ItemMeta meta;

	private ItemBuilder(Material type, int amount) {
		Preconditions.checkArgument(type != null, "Cannot create ItemBuilder for null Material");
		Preconditions.checkArgument(!ILLEGAL_TYPES.contains(type), "Illegal material!");
		Preconditions.checkArgument(amount > 0 && amount <= type.getMaxStackSize(), "Amount must be between 0 - &d", type.getMaxStackSize());

		this.item = new ItemStack(type);
		this.meta = item.getItemMeta();
	}

	private ItemBuilder(ItemStack item) {
		Preconditions.checkArgument(item != null, "Cannot modify a null item");
		Preconditions.checkArgument(!ILLEGAL_TYPES.contains(item.getType()), "Illegal material!");

		this.item = item.clone();
		this.meta = item.getItemMeta();
	}

	/**
	 * Get a new instance of an ItemBuilder given a (non-null and non-air) {@link Material} and
	 * a quantity greater than 0 and less than or equal to {@link Material#getMaxStackSize()}.
	 *
	 * @param type the type of item to build
	 * @param amount the item amount
	 *
	 * @return the ItemBuilder instance for the provided values
	 */
	public static ItemBuilder of(Material type, int amount) {
		return new ItemBuilder(type, amount);
	}

	/**
	 * Get a new instance of an ItemBuilder given a (non-null and non-air) {@link Material}.
	 *
	 * @param type the type of item to build
	 *
	 * @return the ItemBuilder instance for the provided material
	 */
	public static ItemBuilder of(Material type) {
		return new ItemBuilder(type, 1);
	}

	/**
	 * Get a new instance of ItemBuilder to modify an existing {@link ItemStack}. The
	 * ItemStack passed will be cloned, therefore the passed reference will not be modified,
	 * but rather a copy of it. The result of {@link #build()} will be a separate item with
	 * the changes applied from this builder instance. The provided item acts as a base for
	 * the values in this builder.
	 *
	 * @param item the item to build
	 *
	 * @return the ItemBuilder instance for the provided item
	 */
	public static ItemBuilder modify(ItemStack item) {
		return new ItemBuilder(item);
	}

	/**
	 * Check whether the specified type of ItemMeta is supported by this ItemBuilder.
	 *
	 * @param type the type of meta to check
	 *
	 * @return true if supported, false otherwise or if null
	 */
	public boolean isSupportedMeta(Class<? extends ItemMeta> type) {
		return type != null && type.isInstance(meta);
	}

	/**
	 * Apply a method from a more specific type of ItemMeta to this ItemBuilder instance.
	 * If the type provided is unsupported by this ItemBuilder (according to
	 * {@link #isSupportedMeta(Class)}), this method will throw an exception, therefore it
	 * is recommended that it be checked before invoking this method if you are unsure as to
	 * what is and is not supported.
	 *
	 * @param type the type of ItemMeta to apply
	 * @param applier the function to apply to the ItemMeta instance
	 *
	 * @param <T> The ItemMeta type to be applied in the consumer function
	 *
	 * @return this instance. Allows for chained method calls
	 */
	public <T extends ItemMeta> ItemBuilder specific(Class<T> type, Consumer<T> applier) {
		Preconditions.checkArgument(type != null, "Cannot apply meta for type null");
		Preconditions.checkArgument(isSupportedMeta(type), "The specified ItemMeta type is not supported by this ItemBuilder instance");
		Preconditions.checkArgument(applier != null, "Application function must not be null");

		applier.accept(type.cast(meta));
		return this;
	}

	/**
	 * Set the item name.
	 *
	 * @param name the name to set
	 *
	 * @return this instance. Allows for chained method calls
	 *
	 * @see ItemMeta#setDisplayName(String)
	 */
	public ItemBuilder name(String name) {
		this.meta.setDisplayName(name);
		return this;
	}

	/**
	 * Set the item lore in the form of varargs.
	 *
	 * @param lore the lore to set
	 *
	 * @return this instance. Allows for chained method calls
	 *
	 * @see ItemBuilder#lore(List)
	 * @see ItemMeta#setLore(List)
	 */
	public ItemBuilder lore(String... lore) {
		if (lore.length > 0) {
			this.meta.setLore(Arrays.asList(lore));
		}

		return this;
	}

	/**
	 * Set the item lore in the form of a {@literal List<String>}.
	 *
	 * @param lore the lore to set
	 *
	 * @return this instance. Allows for chained method calls
	 *
	 * @see ItemBuilder#lore(String...)
	 * @see ItemMeta#setLore(List)
	 */
	public ItemBuilder lore(List<String> lore) {
		this.meta.setLore(lore);
		return this;
	}

	/**
	 * Set the item damage. Some items may not display damage or accept the damage attribute at
	 * all, in which case this method will simply fail silently.
	 *
	 * @param damage the damage to set
	 *
	 * @return this instance. Allows for chained method calls
	 *
	 * @see Damageable#setDamage(int)
	 */
	public ItemBuilder damage(int damage) {
		((Damageable) meta).setDamage(damage);
		return this;
	}

	/**
	 * Set the item amount. This damage must range between 1 and {@link Material#getMaxStackSize()}
	 * according to the type being built in this ItemBuilder instance.
	 *
	 * @param amount the amount to set
	 *
	 * @return this instance. Allows for chained method calls
	 *
	 * @see ItemStack#setAmount(int)
	 */
	public ItemBuilder amount(int amount) {
		this.item.setAmount(amount);
		return this;
	}

	/**
	 * Apply an enchantment with the specified level to the item. This method does not respect the
	 * level limitations of an enchantment (i.e. Sharpness VI may be applied if desired).
	 *
	 * @param enchantment the enchantment to add
	 * @param level the enchantment level to set
	 *
	 * @return this instance. Allows for chained method calls
	 *
	 * @see ItemMeta#addEnchant(Enchantment, int, boolean)
	 */
	public ItemBuilder enchantment(Enchantment enchantment, int level) {
		this.meta.addEnchant(enchantment, level, true);
		return this;
	}

	/**
	 * Apply flags to the item.
	 *
	 * @param flags the flags to set
	 *
	 * @return this instance. Allows for chained method calls
	 *
	 * @see ItemMeta#addItemFlags(ItemFlag...)
	 */
	public ItemBuilder flags(ItemFlag... flags) {
		if (flags.length > 0) {
			this.meta.addItemFlags(flags);
		}

		return this;
	}

	/**
	 * Set the unbreakable state of this item to true.
	 *
	 * @return this instance. Allows for chained method calls
	 *
	 * @see ItemMeta#setUnbreakable(boolean)
	 */
	public ItemBuilder unbreakable() {
		this.meta.setUnbreakable(true);
		return this;
	}

	/**
	 * Set the item's localized name.
	 *
	 * @param name the localized name to set
	 *
	 * @return this instance. Allows for chained method calls
	 *
	 * @see ItemMeta#setLocalizedName(String)
	 */
	public ItemBuilder localizedName(String name) {
		this.meta.setLocalizedName(name);
		return this;
	}

	/**
	 * Complete the building of this ItemBuilder and return the resulting ItemStack.
	 *
	 * @return the completed {@link ItemStack} instance built by this builder
	 */
	public ItemStack build() {
		this.item.setItemMeta(meta);
		return item;
	}

	public static WrappedItemStackResult fromConfig(ConfigurationSection config) {
		// Type and amount
		Material type = Material.matchMaterial(config.getString("type"));
		if (type == null) {
			return new WrappedItemStackResult(Result.INVALID_TYPE, "The type " + config.getString("type") + " is invalid. These must match in-game IDs (with the exclusion of minecraft:)");
		}

		int amount = config.getInt("amount", 1);
		if (amount <= 0 || amount > type.getMaxStackSize()) {
			return new WrappedItemStackResult(Result.INVALID_AMOUNT, String.format("The amount %1$d is invalid. Must be between 1 and %1$d", amount));
		}

		ItemBuilder item = ItemBuilder.of(type, amount);

		// Name, lore and damage
		item.damage(config.getInt("damage", 0));
		String name = ChatColor.translateAlternateColorCodes('&', config.getString("display-name", ""));
		List<String> lore = config.getStringList("lore").stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList());

		if (!name.isEmpty()) {
			item.name(name);
		}
		if (!lore.isEmpty()) {
			item.lore(lore);
		}

		// Banner meta
		if (config.contains("banner-patterns")) {
			if (!item.isSupportedMeta(BannerMeta.class)) {
				return new WrappedItemStackResult(Result.UNSUPPORTED_META_TYPE, "Banner patterns are unsupported for an item with type " + type);
			}

			List<Pattern> patterns = new ArrayList<>();
			ConfigurationSection configBanner = config.getConfigurationSection("banner-patterns");

			for (String patternId : configBanner.getKeys(false)) {
				boolean useIdentifier = patternId.length() <= 3;
				PatternType pattern = (useIdentifier) ? PatternType.getByIdentifier(patternId.toLowerCase()) : EnumUtils.getEnum(PatternType.class, patternId.toUpperCase());
				if (pattern == null) {
					return new WrappedItemStackResult(Result.INVALID_BANNER_PATTERN, "A banner with the " + (useIdentifier ? "identifier" : "name") + " does not exist");
				}

				String colourName = configBanner.getString(patternId);
				DyeColor colour = EnumUtils.getEnum(DyeColor.class, colourName.toUpperCase());
				if (colour == null) {
					return new WrappedItemStackResult(Result.INVALID_BANNER_PATTERN, "The banner colour " + colourName + " could not be found");
				}

				patterns.add(new Pattern(colour, pattern));
			}

			item.specific(BannerMeta.class, m -> m.setPatterns(patterns));
		}

		// Book meta
		if (config.contains("book-data")) {
			if (!item.isSupportedMeta(BookMeta.class)) {
				return new WrappedItemStackResult(Result.UNSUPPORTED_META_TYPE, "Book data is unsupported for an item with type " + type);
			}

			ConfigurationSection configBook = config.getConfigurationSection("book-data");
			String author = configBook.getString("author");
			String title = configBook.getString("title");

			String generationName = configBook.getString("generation", "original").toUpperCase();
			Generation generation = EnumUtils.getEnum(Generation.class, generationName.toUpperCase());
			if (generation == null) {
				return new WrappedItemStackResult(Result.INVALID_BOOK_GENERATION, "A book generation with the ID " + generationName + " does not exist");
			}

			List<String> pages = configBook.getStringList("pages");

			item.specific(BookMeta.class, m -> {
				if (author != null) {
					m.setAuthor(author);
				}
				if (title != null) {
					m.setTitle(title);
				}
				if (!pages.isEmpty()) {
					m.setPages(pages);
				}
				m.setGeneration(generation);
			});
		}

		// Yes, I know, duplicate code. It's difficult given the results
		// Enchantments (default enchantments)
		if (!item.isSupportedMeta(EnchantmentStorageMeta.class)) {
			ConfigurationSection configEnchantments = config.getConfigurationSection("enchantments");
			for (String enchantmentName : configEnchantments.getKeys(false)) {
				NamespacedKey key = NamespacedKey.minecraft(enchantmentName.toLowerCase());
				Enchantment enchantment = Enchantment.getByKey(key);
				if (enchantment == null) {
					return new WrappedItemStackResult(Result.INVALID_ENCHANTMENT, "An enchantment with the ID " + key + " could not be found. These must match in-game IDs (with the exclusion of minecraft:");
				}

				int level = configEnchantments.getInt(enchantmentName);
				if (level <= 0) {
					return new WrappedItemStackResult(Result.INVALID_ENCHANTMENT, "Invalid enchantment level specified, " + level + ", for enchantment with ID " + key);
				}

				item.enchantment(enchantment, level);
			}
		}
		// Enchantment storage meta (for enchanted books)
		else {
			ConfigurationSection configEnchantments = config.getConfigurationSection("enchantments");
			for (String enchantmentName : configEnchantments.getKeys(false)) {
				NamespacedKey key = NamespacedKey.minecraft(enchantmentName.toLowerCase());
				Enchantment enchantment = Enchantment.getByKey(key);
				if (enchantment == null) {
					return new WrappedItemStackResult(Result.INVALID_ENCHANTMENT, "An enchantment with the ID " + key + " could not be found. These must match in-game IDs (with the exclusion of minecraft:");
				}

				int level = configEnchantments.getInt(enchantmentName);
				if (level <= 0) {
					return new WrappedItemStackResult(Result.INVALID_ENCHANTMENT, "Invalid enchantment level specified, " + level + ", for enchantment with ID " + key);
				}

				item.specific(EnchantmentStorageMeta.class, m -> m.addStoredEnchant(enchantment, level, true));
			}
		}

		// Knowledge book meta
		if (config.contains("knowledge-book-recipes")) {
			if (!item.isSupportedMeta(KnowledgeBookMeta.class)) {
				return new WrappedItemStackResult(Result.UNSUPPORTED_META_TYPE, "Knowledge book recipes are unsupported for an item with type " + type);
			}

			List<String> rawRecipes = config.getStringList("knowledge-book-recipes");
			if (!rawRecipes.isEmpty()) {
				List<NamespacedKey> recipes = new ArrayList<>();
				for (String rawRecipe : rawRecipes) {
					String[] keyParts = rawRecipe.split(":");
					if (keyParts.length != 2) {
						return new WrappedItemStackResult(Result.INVALID_RECIPE_KEY, "The recipe key " + rawRecipe + " is invalid. Must be formatted as \"namespace_value:key_value\"");
					}

					@SuppressWarnings("deprecation")
					NamespacedKey recipeKey = new NamespacedKey(keyParts[0].toLowerCase(), keyParts[1].toLowerCase());
					recipes.add(recipeKey);
				}

				item.specific(KnowledgeBookMeta.class, m -> m.setRecipes(recipes));
			}
		}

		// Leather armour meta
		if (config.contains("leather-color")) {
			if (!item.isSupportedMeta(LeatherArmorMeta.class)) {
				return new WrappedItemStackResult(Result.UNSUPPORTED_META_TYPE, "Leather colours are unsupported for an item with type " + type);
			}

			int red = config.getInt("r"), green = config.getInt("g"), blue = config.getInt("b");
			if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
				return new WrappedItemStackResult(Result.EXCEEDING_RGB, "An RGB value must be within the range of 0 - 255 (inclusive)");
			}

			item.specific(LeatherArmorMeta.class, m -> m.setColor(Color.fromRGB(red, green, blue)));
		}

		// Potion meta
		if (config.contains("potion")) {
			if (!item.isSupportedMeta(PotionMeta.class)) {
				return new WrappedItemStackResult(Result.UNSUPPORTED_META_TYPE, "Potion data is unsupported for an item with type " + type);
			}

			ConfigurationSection configPotion = config.getConfigurationSection("potion");
			if (configPotion.contains("color")) {
				int red = config.getInt("r"), green = config.getInt("g"), blue = config.getInt("b");
				if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
					return new WrappedItemStackResult(Result.EXCEEDING_RGB, "An RGB value must be within the range of 0 - 255 (inclusive)");
				}

				item.specific(PotionMeta.class, m -> m.setColor(Color.fromRGB(red, green, blue)));
			}

			if (configPotion.contains("base")) {
				ConfigurationSection baseSection = config.getConfigurationSection("base");
				PotionType potionType = EnumUtils.getEnum(PotionType.class, baseSection.getString("potion-type", "").toUpperCase());
				if (potionType == null) {
					return new WrappedItemStackResult(Result.INVALID_POTION_TYPE, "A potion type with the name " + baseSection.getString("potion-type") + " was not found");
				}

				boolean extended = baseSection.getBoolean("extended", false);
				boolean upgraded = baseSection.getBoolean("upgraded", false);

				item.specific(PotionMeta.class, m -> m.setBasePotionData(new PotionData(potionType, extended, upgraded)));
			}

			if (configPotion.contains("custom-effects")) {
				for (String effectSectionName : configPotion.getConfigurationSection("custom-effects").getKeys(false)) {
					ConfigurationSection effectSection = configPotion.getConfigurationSection("custom-effects." + effectSectionName);

					PotionEffectType effectType = PotionEffectType.getByName(effectSection.getString("effect", "null"));
					if (effectType == null) {
						return new WrappedItemStackResult(Result.INVALID_POTION_TYPE, "An effect with the name " + effectSection.getString("effect") + " was not found");
					}

					int duration = effectSection.getInt("duration", 600);
					int amplifier = effectSection.getInt("amplifier", 0);
					boolean ambient = effectSection.getBoolean("ambient", false);
					boolean particles = effectSection.getBoolean("particles", true);
					boolean icon = effectSection.getBoolean("icon", true);

					item.specific(PotionMeta.class, m -> m.addCustomEffect(new PotionEffect(effectType, duration, amplifier, ambient, particles, icon), true));
				}
			}
		}

		// Skull meta
		if (config.contains("skull-owner")) {
			if (!item.isSupportedMeta(SkullMeta.class)) {
				return new WrappedItemStackResult(Result.UNSUPPORTED_META_TYPE, "Skull owners are unsupported for an item with type " + type);
			}

			@SuppressWarnings("deprecation")
			OfflinePlayer owner = Bukkit.getOfflinePlayer(config.getString("skull-owner"));
			if (owner == null) {
				return new WrappedItemStackResult(Result.UNKNOWN_PLAYER, "A player with the name " + config.getString("skull-owner") + " could not be found");
			}

			item.specific(SkullMeta.class, m -> m.setOwningPlayer(owner));
		}

		// Tropical fish bucket meta
		if (config.contains("fish-bucket-data")) {
			if (!item.isSupportedMeta(TropicalFishBucketMeta.class)) {
				return new WrappedItemStackResult(Result.UNSUPPORTED_META_TYPE, "Tropical fish bucket data is unsupported for an item with type " + type);
			}

			ConfigurationSection configFishBucket = config.getConfigurationSection("fish-bucket-data");
			org.bukkit.entity.TropicalFish.Pattern pattern = EnumUtils.getEnum(org.bukkit.entity.TropicalFish.Pattern.class, configFishBucket.getString("pattern", "").toUpperCase());
			if (pattern == null) {
				return new WrappedItemStackResult(Result.INVALID_FISH_PATTERN, "A fish pattern with the name " + config.getString("pattern") + " could not be found");
			}

			DyeColor patternColour = EnumUtils.getEnum(DyeColor.class, configFishBucket.getString("pattern-color", "white").toUpperCase());
			DyeColor bodyColour = EnumUtils.getEnum(DyeColor.class, configFishBucket.getString("body-color", "white").toUpperCase());

			item.specific(TropicalFishBucketMeta.class, m -> {
				m.setPattern(pattern);
				m.setPatternColor(patternColour);
				m.setBodyColor(bodyColour);
			});
		}

		return new WrappedItemStackResult(item.build());
	}


	public static final class WrappedItemStackResult {

		public enum Result {
			INVALID_TYPE,
			INVALID_AMOUNT,
			INVALID_ENCHANTMENT,
			INVALID_BANNER_PATTERN,
			INVALID_BOOK_GENERATION,
			INVALID_RECIPE_KEY,
			INVALID_POTION_TYPE,
			INVALID_FISH_PATTERN,
			EXCEEDING_RGB,
			UNSUPPORTED_META_TYPE,
			UNKNOWN_PLAYER,
			SUCCESS;
		}

		private final ItemStack item;
		private final Result result;
		private final String errorMessage;

		private WrappedItemStackResult(Result result, String errorMessage) {
			this.item = null;
			this.result = result;
			this.errorMessage = errorMessage;
		}

		private WrappedItemStackResult(ItemStack item) {
			this.item = item;
			this.result = Result.SUCCESS;
			this.errorMessage = null;
		}

		public ItemStack getItem() {
			return item;
		}

		public Result getResult() {
			return result;
		}

		public boolean isError() {
			return result != Result.SUCCESS;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

	}

}