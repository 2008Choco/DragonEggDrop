package com.ninjaguild.dragoneggdrop.utils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Preconditions;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;

/**
 * A Minecraft server version compatibility definition including a set of supported versions
 * among other variables and exceptions.
 *
 * @author Parker Hawke - 2008Choco
 */
public final class VersionCompatDefinition {

	public enum MajorVersion {

		V1_8(3),
		V1_9(2),
		V1_10(1),
		V1_11(1),
		V1_12(1),
		V1_13(2),
		UNDEFINED(Integer.MAX_VALUE);

		private final int maxRevision;

		private MajorVersion(int maxRevision) {
			this.maxRevision = maxRevision;
		}

	}

	private static String mappingsVersion;

	private final ServerVersion minimum, maximum;
	private final Set<String> unsupportedMappings = new HashSet<>();
	private final Set<ServerVersion> exceptedVersions = new HashSet<>();

	public VersionCompatDefinition(ServerVersion minimum, ServerVersion maximum, String... unsupportedMappings) {
		Preconditions.checkArgument(minimum != null, "Minimum verison must not be null");
		Preconditions.checkArgument(maximum != null, "Maximum version must not be null");
		Preconditions.checkArgument(minimum.matches(ServerVersion.LATEST) || minimum.isOlderThanOrMatches(maximum), "Minimum version must be older than maximum version...");

		this.minimum = minimum;
		this.maximum = maximum;

		for (String mapping : unsupportedMappings) {
			this.unsupportedMappings.add(mapping);
		}
	}

	public VersionCompatDefinition(ServerVersion minimum, String... unsupportedMappings) {
		this(minimum, ServerVersion.LATEST, unsupportedMappings);
	}

	public VersionCompatDefinition(String... unsupportedMappings) {
		this(ServerVersion.LATEST, ServerVersion.LATEST, unsupportedMappings);
	}

	public VersionCompatDefinition(ServerVersion minimum, ServerVersion maximum) {
		Preconditions.checkArgument(minimum != null, "Minimum verison must not be null");
		Preconditions.checkArgument(maximum != null, "Maximum version must not be null");
		Preconditions.checkArgument(minimum.matches(ServerVersion.LATEST) || minimum.isOlderThanOrMatches(maximum), "Minimum version must be older than maximum version...");

		this.minimum = minimum;
		this.maximum = maximum;
	}

	public VersionCompatDefinition(ServerVersion minimum) {
		this(minimum, ServerVersion.LATEST);
	}

	public VersionCompatDefinition() {
		this(ServerVersion.LATEST, ServerVersion.LATEST);
	}

	public VersionCompatDefinition exception(MajorVersion version, int revision) {
		this.exceptedVersions.add(ServerVersion.of(version, revision));
		return this;
	}

	public ServerVersion getMinimumServerVersion() {
		return minimum;
	}

	public ServerVersion getMaximumServerVersion() {
		return maximum;
	}

	public Set<String> getUnsupportedMappings() {
		return Collections.unmodifiableSet(unsupportedMappings);
	}

	public boolean isServerSupported(MajorVersion version, int revision, String mappingsId) {
		return isServerSupported(version, revision) && !unsupportedMappings.contains(mappingsId);
	}

	public boolean isServerSupported(MajorVersion version, int revision) {
		if (version == MajorVersion.UNDEFINED) {
			return true;
		}

		Preconditions.checkArgument(version != null, "null versions are not supported");
		Preconditions.checkArgument(revision >= 1, "Revision numbers must be >= 1");

		return minimum.isOlderThanOrMatches(version, revision) && maximum.isNewerThanOrMatches(version, revision);
	}

	public boolean isServerSupported(boolean checkMappings) {
		return isServerSupported() && (!checkMappings || !unsupportedMappings.contains(getCurrentMappingsVersion()));
	}

	public boolean isServerSupported() {
		return ServerVersion.CURRENT == ServerVersion.UNDEFINED || isServerSupported(ServerVersion.CURRENT.version, ServerVersion.CURRENT.revision);
	}

	private static String getCurrentMappingsVersion() {
		if (mappingsVersion == null) {
			String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
			try { // This field only exists as of December 25th, 2018, late 1.13.2.
				Class<?> classCraftMagicNumbers = Class.forName("org.bukkit.craftbukkit." + version + ".util.CraftMagicNumbers");
				Field fieldMappingsVersion = classCraftMagicNumbers.getField("MAPPINGS_VERSION");
				return (mappingsVersion = (String) fieldMappingsVersion.get(null));
			} catch (ReflectiveOperationException e) {
				return null;
			}
		}

		return mappingsVersion;
	}


	public static final class ServerVersion {

		public static final ServerVersion UNDEFINED = ServerVersion.of(MajorVersion.UNDEFINED, Integer.MAX_VALUE);
		public static final ServerVersion LATEST, CURRENT;

		static {
			// Define LATEST
			MajorVersion[] versionValues = MajorVersion.values();
			MajorVersion latest = versionValues[versionValues.length - 2];
			LATEST = ServerVersion.of(latest, latest.maxRevision);

			// Define CURRENT
			String versionString = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]; // v1_00_R0
			String serverVersionString = versionString.substring(0, versionString.length() - 3).toUpperCase(); // V1_00
			String serverRevisionString = versionString.substring(versionString.length() - 1);

			MajorVersion version = null;
			for (MajorVersion constant : MajorVersion.values()) {
				if (constant.name().equals(serverVersionString)) {
					version = constant;
					break;
				}
			}

			int revision = NumberUtils.toInt(serverRevisionString, -1);
			CURRENT = (version == null || (revision >= 1 && revision <= version.maxRevision)) ? ServerVersion.of(version, revision) : UNDEFINED;
		}

		private final MajorVersion version;
		private final int revision;

		private ServerVersion(MajorVersion version, int revision) {
			this.version = version;
			this.revision = revision;
		}

		public static ServerVersion of(MajorVersion version, int revision) {
			Preconditions.checkArgument(version != null, "Server version must not be null");
			Preconditions.checkArgument(revision >= 1 && revision <= version.maxRevision, "Revision must be >= 1 and <= major's maximum revision (%d)", version.maxRevision);

			return new ServerVersion(version, revision);
		}

		public static ServerVersion of(MajorVersion version) {
			return of(version, 1);
		}

		public MajorVersion getVersion() {
			return version;
		}

		public int getRevision() {
			return revision;
		}

		public boolean matches(ServerVersion other) {
			return other == null || matches(other.version, other.revision);
		}

		public boolean matches(MajorVersion version, int revision) {
			return this.version == version && this.revision == revision;
		}

		public boolean isNewerThan(ServerVersion other) {
			return other == null || isNewerThan(other.version, other.revision);
		}

		public boolean isNewerThan(MajorVersion version, int revision) {
			return version != null && this.version.ordinal() > version.ordinal() && this.revision > revision;
		}

		public boolean isNewerThanOrMatches(ServerVersion other) {
			return isNewerThan(other) || matches(other.version, other.revision);
		}

		public boolean isNewerThanOrMatches(MajorVersion version, int revision) {
			return isNewerThan(version, revision) || (this.version == version && this.revision == revision);
		}

		public boolean isOlderThan(ServerVersion other) {
			return other == null || isOlderThan(other.version, other.revision);
		}

		public boolean isOlderThan(MajorVersion version, int revision) {
			return version != null && this.version.ordinal() < version.ordinal() && this.revision < revision;
		}

		public boolean isOlderThanOrMatches(ServerVersion other) {
			return other == null || isOlderThanOrMatches(other.version, other.revision);
		}

		public boolean isOlderThanOrMatches(MajorVersion version, int revision) {
			return isOlderThan(version, revision) || matches(version, revision);
		}

		@Override
		public int hashCode() {
			return Objects.hash(version, revision);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof ServerVersion)) return false;

			ServerVersion other = (ServerVersion) obj;
			return version == other.version && revision == other.revision;
		}

	}

}