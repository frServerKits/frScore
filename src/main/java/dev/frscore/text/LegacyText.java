package dev.frscore.text;

import java.util.regex.Pattern;

/**
 * Converts legacy color codes into MiniMessage tags so users can freely mix
 * both formats in config.yml.
 */
public final class LegacyText {

    private static final Pattern HEX_EXTENDED = Pattern.compile("[&§]x(?:[&§][0-9a-fA-F]){6}");
    private static final Pattern HEX_SHORT = Pattern.compile("[&§]#([0-9a-fA-F]{6})");

    private LegacyText() {
    }

    public static String toMiniMessage(final String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String text = HEX_EXTENDED.matcher(input).replaceAll(match -> {
            final String raw = match.group();
            final StringBuilder hex = new StringBuilder(6);
            for (int i = 2; i + 1 < raw.length(); i += 2) {
                hex.append(raw.charAt(i + 1));
            }
            return "<#" + hex + ">";
        });

        text = HEX_SHORT.matcher(text).replaceAll("<#$1>");

        final StringBuilder output = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            final char current = text.charAt(i);
            if ((current == '&' || current == '§') && i + 1 < text.length()) {
                final char code = Character.toLowerCase(text.charAt(i + 1));
                final String tag = tagFor(code);
                if (tag != null) {
                    output.append(tag);
                    i++;
                    continue;
                }
            }
            output.append(current);
        }
        return output.toString();
    }

    private static String tagFor(final char code) {
        return switch (code) {
            case '0' -> "<black>";
            case '1' -> "<dark_blue>";
            case '2' -> "<dark_green>";
            case '3' -> "<dark_aqua>";
            case '4' -> "<dark_red>";
            case '5' -> "<dark_purple>";
            case '6' -> "<gold>";
            case '7' -> "<gray>";
            case '8' -> "<dark_gray>";
            case '9' -> "<blue>";
            case 'a' -> "<green>";
            case 'b' -> "<aqua>";
            case 'c' -> "<red>";
            case 'd' -> "<light_purple>";
            case 'e' -> "<yellow>";
            case 'f' -> "<white>";
            case 'k' -> "<obfuscated>";
            case 'l' -> "<bold>";
            case 'm' -> "<strikethrough>";
            case 'n' -> "<underlined>";
            case 'o' -> "<italic>";
            case 'r' -> "<reset>";
            default -> null;
        };
    }
}
