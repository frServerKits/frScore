package dev.frscore.scoreboard;

import dev.frscore.FrScorePlugin;
import dev.frscore.config.PluginConfig;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Owns one scoreboard per player and keeps it in sync with the current config.
 */
public final class ScoreboardService {

    private final FrScorePlugin plugin;
    private final NamespacedKey hiddenKey;
    private final Map<UUID, Board> boards = new HashMap<>();
    private final Set<UUID> hidden = new HashSet<>();

    public ScoreboardService(final FrScorePlugin plugin) {
        this.plugin = plugin;
        this.hiddenKey = new NamespacedKey(plugin, "hidden");
    }

    public void handleJoin(final Player player) {
        if (isPersistentlyHidden(player)) {
            hidden.add(player.getUniqueId());
        }
        tick(player);
    }

    public void handleQuit(final Player player) {
        boards.remove(player.getUniqueId());
        hidden.remove(player.getUniqueId());
    }

    public void handleWorldChange(final Player player) {
        tick(player);
    }

    /**
     * @return {@code true} when the scoreboard is now hidden.
     */
    public boolean toggle(final Player player) {
        final UUID uuid = player.getUniqueId();
        if (hidden.remove(uuid)) {
            player.getPersistentDataContainer().remove(hiddenKey);
            tick(player);
            return false;
        }

        hidden.add(uuid);
        player.getPersistentDataContainer().set(hiddenKey, PersistentDataType.BYTE, (byte) 1);
        clear(player);
        return true;
    }

    public boolean isWorldDisabled(final Player player) {
        return plugin.config().isWorldDisabled(player.getWorld().getName());
    }

    public void tick() {
        if (!plugin.config().enabled()) {
            clearAll();
            return;
        }
        for (final Player player : Bukkit.getOnlinePlayers()) {
            tick(player);
        }
    }

    private void tick(final Player player) {
        if (!canShow(player)) {
            clear(player);
            return;
        }

        Board board = boards.get(player.getUniqueId());
        if (board == null) {
            board = createBoard();
            boards.put(player.getUniqueId(), board);
        }

        if (player.getScoreboard() != board.scoreboard()) {
            player.setScoreboard(board.scoreboard());
        }

        update(player, board);
    }

    /**
     * Rebuilds every player's scoreboard, for example after a live reload.
     */
    public void reload() {
        clearAll();
        tick();
    }

    public void shutdown() {
        clearAll();
        hidden.clear();
    }

    private boolean canShow(final Player player) {
        return plugin.config().enabled()
                && !hidden.contains(player.getUniqueId())
                && !isWorldDisabled(player);
    }

    private Board createBoard() {
        final PluginConfig config = plugin.config();
        final Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        final Objective objective = scoreboard.registerNewObjective(
                "frscore",
                Criteria.DUMMY,
                Component.empty()
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        if (config.hideNumbers()) {
            objective.numberFormat(NumberFormat.blank());
        }

        final List<Team> rows = new ArrayList<>(config.lines().size());
        final int topScore = config.lines().size();
        for (int index = 0; index < config.lines().size(); index++) {
            final Team team = scoreboard.registerNewTeam("fs_line_" + index);
            final String entry = "§" + Integer.toHexString(index) + "§r";
            team.addEntry(entry);
            team.prefix(Component.empty());
            objective.getScore(entry).setScore(topScore - index);
            rows.add(team);
        }

        return new Board(scoreboard, objective, List.copyOf(rows));
    }

    private void update(final Player player, final Board board) {
        final PluginConfig config = plugin.config();
        board.objective().displayName(plugin.text().render(player, config.title()));

        final List<Team> rows = board.lines();
        final int lineCount = Math.min(rows.size(), config.lines().size());
        for (int index = 0; index < lineCount; index++) {
            rows.get(index).prefix(plugin.text().render(player, config.lines().get(index)));
        }
    }

    private void clear(final Player player) {
        final Board board = boards.remove(player.getUniqueId());
        if (board != null && player.getScoreboard() == board.scoreboard()) {
            player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard());
        }
    }

    private void clearAll() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            clear(player);
        }
    }

    private boolean isPersistentlyHidden(final Player player) {
        final Byte value = player.getPersistentDataContainer().get(hiddenKey, PersistentDataType.BYTE);
        return value != null && value == (byte) 1;
    }

    private record Board(Scoreboard scoreboard, Objective objective, List<Team> lines) {
    }
}
