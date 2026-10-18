package dev.frscore.config;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

/**
 * All player-facing command messages, in one immutable place.
 */
public record Messages(
        String prefix,
        String reloaded,
        String reloadFailed,
        String toggledOn,
        String toggledOff,
        String noPermission,
        String playerOnly,
        String unknownCommand,
        String worldDisabled,
        List<String> help
) {

    private static final String DEFAULT_PREFIX =
            "<gradient:#34d399:#38bdf8><bold>frScore</bold></gradient> <dark_gray>»</dark_gray> ";

    private static final List<String> DEFAULT_HELP = List.of(
            "<dark_gray>─────────────────────────────",
            "<gradient:#34d399:#38bdf8><bold>frScore</bold></gradient> <dark_gray>v%version%",
            "<white>/frscore toggle <dark_gray>— <gray>Show or hide your scoreboard",
            "<white>/frscore reload <dark_gray>— <gray>Reload config.yml",
            "<white>/frscore help <dark_gray>— <gray>Show this help",
            "<dark_gray>─────────────────────────────"
    );

    public static Messages load(@Nullable final ConfigurationSection root) {
        final ConfigurationSection section =
                root == null ? null : root.getConfigurationSection("messages");

        return new Messages(
                string(section, "prefix", DEFAULT_PREFIX),
                string(section, "reloaded",
                        "<green>Configuration reloaded in <white>%time%</white>ms."),
                string(section, "reload-failed",
                        "<red>Could not reload the configuration. Check the console for details."),
                string(section, "toggled-on",
                        "<green>Your scoreboard is now visible."),
                string(section, "toggled-off",
                        "<gray>Your scoreboard is now hidden. "
                                + "Use <white>/frscore toggle</white> to show it again."),
                string(section, "no-permission",
                        "<red>You do not have permission to do that."),
                string(section, "player-only",
                        "<red>Only players can use that command."),
                string(section, "unknown-command",
                        "<red>Unknown subcommand. Use <white>/frscore help</white>."),
                string(section, "world-disabled",
                        "<yellow>Your scoreboard is hidden because it is disabled in this world."),
                list(section, "help", DEFAULT_HELP)
        );
    }

    private static String string(
            @Nullable final ConfigurationSection section,
            final String path,
            final String fallback
    ) {
        return section == null ? fallback : section.getString(path, fallback);
    }

    private static List<String> list(
            @Nullable final ConfigurationSection section,
            final String path,
            final List<String> fallback
    ) {
        if (section == null || !section.isList(path)) {
            return fallback;
        }
        return List.copyOf(section.getStringList(path));
    }
}
