package dev.frscore.placeholder;

import dev.frscore.FrScorePlugin;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves PlaceholderAPI placeholders first, then fills in frScore's built-in
 * placeholders. This means an installed expansion can override a built-in
 * value, while the built-ins keep working when PlaceholderAPI is absent.
 */
public final class PlaceholderService {

    private static final Pattern PLACEHOLDER = Pattern.compile("%([a-zA-Z0-9_]+)%");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final FrScorePlugin plugin;
    private final boolean placeholderApi;
    private final AtomicBoolean papiWarningLogged = new AtomicBoolean();

    public PlaceholderService(final FrScorePlugin plugin) {
        this.plugin = plugin;
        this.placeholderApi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public boolean hasPlaceholderApi() {
        return placeholderApi;
    }

    public String resolve(@Nullable final Player player, final String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String result = input;
        if (player != null && placeholderApi) {
            try {
                result = PlaceholderAPI.setPlaceholders(player, result);
            } catch (final RuntimeException exception) {
                if (papiWarningLogged.compareAndSet(false, true)) {
                    plugin.getLogger().warning(
                            "A PlaceholderAPI expansion threw an exception. "
                                    + "The built-in placeholders will still be used."
                    );
                    plugin.getLogger().warning(exception.toString());
                }
            }
        }

        return resolveBuiltIns(player, result);
    }

    private String resolveBuiltIns(@Nullable final Player player, final String input) {
        final Matcher matcher = PLACEHOLDER.matcher(input);
        final StringBuilder output = new StringBuilder(input.length() + 16);

        while (matcher.find()) {
            final String key = matcher.group(1);
            final String value = builtIn(player, key);
            matcher.appendReplacement(output, Matcher.quoteReplacement(value != null ? value : matcher.group()));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    private String builtIn(@Nullable final Player player, final String key) {
        if (player != null) {
            final String playerValue = playerPlaceholder(player, key);
            if (playerValue != null) {
                return playerValue;
            }
        }

        return switch (key) {
            case "server_online" -> Integer.toString(Bukkit.getOnlinePlayers().size());
            case "server_max_players" -> Integer.toString(Bukkit.getMaxPlayers());
            case "server_tps" -> decimal(Bukkit.getServer().getTPS()[0]);
            case "server_tps_1m" -> decimal(Bukkit.getServer().getTPS()[0]);
            case "server_tps_5m" -> decimal(Bukkit.getServer().getTPS()[1]);
            case "server_tps_15m" -> decimal(Bukkit.getServer().getTPS()[2]);
            case "server_time" -> LocalTime.now().format(TIME_FORMAT);
            case "server_date" -> LocalDate.now().format(DATE_FORMAT);
            case "server_name" -> Bukkit.getServer().getName();
            default -> null;
        };
    }

    private String playerPlaceholder(final Player player, final String key) {
        return switch (key) {
            case "player_name", "player" -> player.getName();
            case "player_displayname" -> PlainTextComponentSerializer.plainText().serialize(player.displayName());
            case "player_uuid" -> player.getUniqueId().toString();
            case "player_world" -> player.getWorld().getName();
            case "player_x" -> Integer.toString(player.getLocation().getBlockX());
            case "player_y" -> Integer.toString(player.getLocation().getBlockY());
            case "player_z" -> Integer.toString(player.getLocation().getBlockZ());
            case "player_ping" -> Integer.toString(player.getPing());
            case "player_health" -> decimal(player.getHealth());
            case "player_max_health" -> {
                final AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                yield decimal(maxHealth == null ? player.getHealth() : maxHealth.getValue());
            }
            case "player_food" -> Integer.toString(player.getFoodLevel());
            case "player_level" -> Integer.toString(player.getLevel());
            case "player_exp" -> Integer.toString(Math.round(player.getExp() * 100.0F));
            case "player_gamemode" -> player.getGameMode().name().toLowerCase(Locale.ROOT);
            case "player_sneaking" -> Boolean.toString(player.isSneaking());
            case "player_flying" -> Boolean.toString(player.isFlying());
            default -> null;
        };
    }

    private static String decimal(final double value) {
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
