package dev.frscore.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

/**
 * An immutable snapshot of config.yml. A new snapshot is created after every
 * reload, so the rest of the plugin never reads a half-updated config.
 */
public record PluginConfig(
        boolean enabled,
        long updateInterval,
        boolean autoReload,
        long autoReloadCheckInterval,
        boolean hideNumbers,
        Set<String> disabledWorlds,
        String title,
        List<String> lines,
        Messages messages
) {

    public static final int MAX_LINES = 15;

    private static final String DEFAULT_TITLE =
            "<gradient:#34d399:#38bdf8><bold>frScore</bold></gradient>";

    private static final List<String> DEFAULT_LINES = List.of(
            "<gray>Welcome, <white>%player_name%</white><gray>!",
            "",
            "<dark_gray>│ <gray>Ping: <#a7f3d0>%player_ping%ms",
            "<dark_gray>│ <gray>World: <#a7f3d0>%player_world%",
            "",
            "<dark_gray>│ <gray>Online: <#93c5fd>%server_online%<dark_gray>/<#bfdbfe>%server_max_players%"
    );

    public static PluginConfig load(final ConfigurationSection root, final Logger logger) {
        final long updateInterval = clamp(
                root.getLong("update-interval", 20L),
                1L,
                72_000L,
                "update-interval",
                logger
        );

        final long autoReloadCheckInterval = clamp(
                root.getLong("auto-reload-check-interval", 40L),
                20L,
                7_200L,
                "auto-reload-check-interval",
                logger
        );

        final List<String> configuredLines = root.isList("lines")
                ? root.getStringList("lines")
                : DEFAULT_LINES;

        final List<String> lines = new ArrayList<>(Math.min(configuredLines.size(), MAX_LINES));
        for (final String line : configuredLines) {
            if (lines.size() >= MAX_LINES) {
                break;
            }
            lines.add(line == null ? "" : line);
        }

        if (configuredLines.size() > MAX_LINES) {
            logger.warning("config.yml defines " + configuredLines.size() + " scoreboard lines. "
                    + "Minecraft only displays " + MAX_LINES + "; the extra lines were ignored.");
        }

        final Set<String> disabledWorlds = new HashSet<>();
        for (final String world : root.getStringList("disabled-worlds")) {
            if (world != null && !world.isBlank()) {
                disabledWorlds.add(world.toLowerCase(Locale.ROOT));
            }
        }

        return new PluginConfig(
                root.getBoolean("enabled", true),
                updateInterval,
                root.getBoolean("auto-reload", true),
                autoReloadCheckInterval,
                root.getBoolean("hide-numbers", true),
                Set.copyOf(disabledWorlds),
                root.getString("title", DEFAULT_TITLE),
                List.copyOf(lines),
                Messages.load(root)
        );
    }

    public boolean isWorldDisabled(final String worldName) {
        final String normalized = worldName.toLowerCase(Locale.ROOT);
        return disabledWorlds.contains("*") || disabledWorlds.contains(normalized);
    }

    private static long clamp(
            final long value,
            final long minimum,
            final long maximum,
            final String path,
            final Logger logger
    ) {
        final long clamped = Math.max(minimum, Math.min(maximum, value));
        if (clamped != value) {
            logger.warning("config.yml: " + path + " must be between " + minimum
                    + " and " + maximum + ". Using " + clamped + ".");
        }
        return clamped;
    }
}
