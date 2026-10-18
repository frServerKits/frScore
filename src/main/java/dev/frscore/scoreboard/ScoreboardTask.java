package dev.frscore.scoreboard;

import dev.frscore.FrScorePlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Runs the repeating scoreboard refresh on the main server thread.
 */
public final class ScoreboardTask {

    private final FrScorePlugin plugin;
    private BukkitTask task;

    public ScoreboardTask(final FrScorePlugin plugin) {
        this.plugin = plugin;
    }

    public void start(final long intervalTicks) {
        stop();
        task = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                () -> plugin.scoreboards().tick(),
                intervalTicks,
                intervalTicks
        );
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
