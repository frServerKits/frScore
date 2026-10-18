package dev.frscore;

import dev.frscore.command.FrScoreCommand;
import dev.frscore.config.ConfigWatcher;
import dev.frscore.config.PluginConfig;
import dev.frscore.listener.PlayerListener;
import dev.frscore.placeholder.PlaceholderService;
import dev.frscore.scoreboard.ScoreboardService;
import dev.frscore.scoreboard.ScoreboardTask;
import dev.frscore.text.TextRenderer;
import java.util.logging.Level;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class FrScorePlugin extends JavaPlugin {

    private PluginConfig config;
    private PlaceholderService placeholders;
    private TextRenderer text;
    private ScoreboardService scoreboards;
    private ScoreboardTask scoreboardTask;
    private ConfigWatcher configWatcher;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.placeholders = new PlaceholderService(this);
        this.text = new TextRenderer(placeholders);
        this.scoreboards = new ScoreboardService(this);
        this.scoreboardTask = new ScoreboardTask(this);
        this.configWatcher = new ConfigWatcher(this);

        try {
            this.config = PluginConfig.load(getConfig(), getLogger());
        } catch (final RuntimeException exception) {
            getLogger().log(Level.SEVERE, "Could not read config.yml. Disabling frScore.", exception);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        final PluginCommand command = getCommand("frscore");
        if (command != null) {
            final FrScoreCommand executor = new FrScoreCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            getLogger().warning("The /frscore command is missing from plugin.yml.");
        }

        applyConfiguration();

        getLogger().info("frScore enabled. PlaceholderAPI: "
                + (placeholders.hasPlaceholderApi() ? "connected" : "not installed"));
    }

    @Override
    public void onDisable() {
        if (configWatcher != null) {
            configWatcher.stop();
        }
        if (scoreboardTask != null) {
            scoreboardTask.stop();
        }
        if (scoreboards != null) {
            scoreboards.shutdown();
        }
    }

    /**
     * Reloads config.yml and applies it without restarting the server. If the
     * file cannot be parsed, the previous snapshot stays active.
     *
     * @return true when the new config was applied
     */
    public boolean reloadConfigAndApply(final boolean logSuccess) {
        final long started = System.nanoTime();
        final PluginConfig previous = this.config;

        try {
            reloadConfig();
            final PluginConfig loaded = PluginConfig.load(getConfig(), getLogger());
            this.config = loaded;
            applyConfiguration();

            if (logSuccess) {
                final long elapsedMillis = (System.nanoTime() - started) / 1_000_000L;
                getLogger().info("config.yml reloaded in " + elapsedMillis + "ms.");
            }
            return true;
        } catch (final RuntimeException exception) {
            this.config = previous;
            getLogger().log(Level.SEVERE,
                    "Could not reload config.yml. The previous configuration is still active.",
                    exception);
            return false;
        }
    }

    public void reloadConfigFromWatcher() {
        final long started = System.nanoTime();
        if (reloadConfigAndApply(false)) {
            final long elapsedMillis = (System.nanoTime() - started) / 1_000_000L;
            getLogger().info("config.yml changed; reloaded automatically in " + elapsedMillis + "ms.");
        }
    }

    private void applyConfiguration() {
        scoreboardTask.start(config.updateInterval());

        if (config.autoReload()) {
            configWatcher.start(config.autoReloadCheckInterval());
        } else {
            configWatcher.stop();
        }

        scoreboards.reload();
    }

    public PluginConfig config() {
        return config;
    }

    public PlaceholderService placeholders() {
        return placeholders;
    }

    public TextRenderer text() {
        return text;
    }

    public ScoreboardService scoreboards() {
        return scoreboards;
    }
}
