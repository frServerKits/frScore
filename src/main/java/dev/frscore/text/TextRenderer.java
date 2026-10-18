package dev.frscore.text;

import dev.frscore.placeholder.PlaceholderService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Renders config values into Adventure components.
 */
public final class TextRenderer {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final PlaceholderService placeholders;

    public TextRenderer(final PlaceholderService placeholders) {
        this.placeholders = placeholders;
    }

    public Component render(@Nullable final Player player, final String raw) {
        final String resolved = placeholders.resolve(player, raw == null ? "" : raw);
        final String converted = LegacyText.toMiniMessage(resolved);
        try {
            return MINI_MESSAGE.deserialize(converted);
        } catch (final RuntimeException exception) {
            // A broken tag should never break the whole scoreboard.
            return Component.text(resolved);
        }
    }
}
