package dev.frscore.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LegacyTextTest {

    @Test
    void convertsNamedColorsAndStyles() {
        assertEquals(
                "<green>Hello <bold>world<reset>!",
                LegacyText.toMiniMessage("&aHello &lworld&r!")
        );
    }

    @Test
    void convertsSectionSigns() {
        assertEquals("<red>Warning", LegacyText.toMiniMessage("§cWarning"));
    }

    @Test
    void convertsShortHex() {
        assertEquals("<#ff8800>Sunset", LegacyText.toMiniMessage("&#ff8800Sunset"));
    }

    @Test
    void convertsExtendedHex() {
        assertEquals(
                "<#00ccff>Sky",
                LegacyText.toMiniMessage("&x&0&0&c&c&f&fSky")
        );
    }

    @Test
    void leavesUnknownCodesAndMiniMessageAlone() {
        assertEquals(
                "<gradient:#34d399:#38bdf8>frScore</gradient> &z",
                LegacyText.toMiniMessage("<gradient:#34d399:#38bdf8>frScore</gradient> &z")
        );
    }
}
