package dev.frscore.command;

import dev.frscore.FrScorePlugin;
import dev.frscore.config.Messages;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class FrScoreCommand implements CommandExecutor, TabCompleter {

    private final FrScorePlugin plugin;

    public FrScoreCommand(final FrScorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            final @NotNull CommandSender sender,
            final @NotNull Command command,
            final @NotNull String label,
            final @NotNull String[] args
    ) {
        if (!sender.hasPermission("frscore.use")) {
            send(sender, plugin.config().messages().noPermission());
            return true;
        }

        final String subcommand = args.length == 0
                ? "help"
                : args[0].toLowerCase(Locale.ROOT);

        switch (subcommand) {
            case "toggle" -> toggle(sender);
            case "reload" -> reload(sender);
            case "help", "?" -> sendHelp(sender);
            default -> {
                send(sender, plugin.config().messages().unknownCommand());
                sendHelp(sender);
            }
        }
        return true;
    }

    private void toggle(final CommandSender sender) {
        final Messages messages = plugin.config().messages();
        if (!sender.hasPermission("frscore.toggle")) {
            send(sender, messages.noPermission());
            return;
        }
        if (!(sender instanceof Player player)) {
            send(sender, messages.playerOnly());
            return;
        }

        final boolean hidden = plugin.scoreboards().toggle(player);
        send(sender, hidden ? messages.toggledOff() : messages.toggledOn());

        if (!hidden && plugin.scoreboards().isWorldDisabled(player)) {
            send(sender, messages.worldDisabled());
        }
    }

    private void reload(final CommandSender sender) {
        if (!sender.hasPermission("frscore.reload")) {
            send(sender, plugin.config().messages().noPermission());
            return;
        }

        final long started = System.nanoTime();
        final boolean success = plugin.reloadConfigAndApply(true);
        final long elapsedMillis = (System.nanoTime() - started) / 1_000_000L;
        final Messages messages = plugin.config().messages();

        if (success) {
            send(sender, messages.reloaded().replace("%time%", Long.toString(elapsedMillis)));
        } else {
            send(sender, messages.reloadFailed());
        }
    }

    private void sendHelp(final CommandSender sender) {
        final Player player = sender instanceof Player ? (Player) sender : null;
        final String version = plugin.getPluginMeta().getVersion();

        for (final String line : plugin.config().messages().help()) {
            sendComponent(sender, plugin.text().render(
                    player,
                    line.replace("%version%", version)
            ));
        }
    }

    private void send(final CommandSender sender, final String raw) {
        sendComponent(sender, plugin.text().render(
                sender instanceof Player player ? player : null,
                plugin.config().messages().prefix() + raw
        ));
    }

    private void sendComponent(final CommandSender sender, final Component component) {
        sender.sendMessage(component);
    }

    @Override
    public @NotNull List<String> onTabComplete(
            final @NotNull CommandSender sender,
            final @NotNull Command command,
            final @NotNull String alias,
            final @NotNull String[] args
    ) {
        if (args.length != 1) {
            return Collections.emptyList();
        }

        final List<String> options = new ArrayList<>(3);
        if (sender.hasPermission("frscore.toggle")) {
            options.add("toggle");
        }
        if (sender.hasPermission("frscore.reload")) {
            options.add("reload");
        }
        options.add("help");

        final String prefix = args[0].toLowerCase(Locale.ROOT);
        options.removeIf(option -> !option.startsWith(prefix));
        return options;
    }
}
