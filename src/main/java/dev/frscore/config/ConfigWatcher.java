package dev.frscore.config;

import dev.frscore.FrScorePlugin;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.scheduler.BukkitTask;

/**
 * Polls config.yml for changes and reloads after the file has been stable for
 * one check interval. The small debounce avoids reading while an editor or
 * deployment script is still writing the file.
 */
public final class ConfigWatcher {

    private final FrScorePlugin plugin;
    private final Path configPath;
    private BukkitTask task;
    private Stamp lastStamp;
    private Stamp pendingStamp;
    private boolean readWarningLogged;

    public ConfigWatcher(final FrScorePlugin plugin) {
        this.plugin = plugin;
        this.configPath = plugin.getDataFolder().toPath().resolve("config.yml");
    }

    public void start(final long checkIntervalTicks) {
        stop();
        lastStamp = readStamp();
        pendingStamp = null;
        task = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                this::check,
                checkIntervalTicks,
                checkIntervalTicks
        );
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        pendingStamp = null;
    }

    private void check() {
        final Stamp current = readStamp();
        if (current == null) {
            return;
        }

        if (lastStamp == null) {
            lastStamp = current;
            return;
        }

        if (current.equals(lastStamp)) {
            pendingStamp = null;
            return;
        }

        if (!current.equals(pendingStamp)) {
            pendingStamp = current;
            return;
        }

        lastStamp = current;
        pendingStamp = null;
        plugin.reloadConfigFromWatcher();
    }

    private Stamp readStamp() {
        try {
            final BasicFileAttributes attributes =
                    Files.readAttributes(configPath, BasicFileAttributes.class);
            readWarningLogged = false;
            return new Stamp(attributes.lastModifiedTime(), attributes.size());
        } catch (final IOException exception) {
            if (!readWarningLogged) {
                readWarningLogged = true;
                plugin.getLogger().log(
                        Level.WARNING,
                        "Could not read config.yml for live reloading. "
                                + "Changes will be picked up when the file is available again.",
                        exception
                );
            }
            return null;
        }
    }

    private record Stamp(FileTime modified, long size) {
        private Stamp {
            Objects.requireNonNull(modified, "modified");
        }
    }
}
