> [!WARNING]  
> This plugin was created using AI, so if you encounter any errors, please open an issue.

# frScore

A small, fast scoreboard plugin for **Paper** with a deliberately simple
configuration. Edit `config.yml`, save it, and the scoreboard updates live —
no restart and no `/reload`.

![Paper](https://img.shields.io/badge/Paper-1.21%2B-38bdf8)
![Java](https://img.shields.io/badge/Java-21-34d399)

## Features

- **KISS configuration** — a title and a list of lines is all you need.
- **Live reloading** — automatic file watching plus `/frscore reload`.
- **PlaceholderAPI support** — every PAPI placeholder works in titles and lines.
- **Useful built-ins** — player, world, ping, health, TPS, time and more work
  even without PlaceholderAPI.
- **MiniMessage + legacy colors** — gradients, hex colors, bold, rainbow and
  classic `&a`/`§a` codes can be mixed freely.
- **Per-player toggle** — `/frscore toggle`, saved across restarts.
- **Clean numbers** — hide the red sidebar numbers out of the box.
- **World control** — disable the scoreboard in selected worlds.
- **Safe reloads** — an invalid config never replaces the last working one.
- **No database, no dependencies** — just drop the jar in `plugins/`.

## Requirements

| Requirement | Version |
| --- | --- |
| Server | Paper 1.21 or newer |
| Java | 21 or newer |
| PlaceholderAPI | Optional |

## Install

1. Download `frScore-1.0.0.jar` from the
   [releases page](https://github.com/frServerKits/frScore/releases) or build it
   yourself (see below).
2. Put the jar in your server's `plugins/` folder.
3. Start the server.
4. Edit `plugins/frScore/config.yml`, save, and watch the change happen live.

## Quick start

The only required part of the config is a title and some lines:

```yaml
title: "<gradient:#34d399:#38bdf8><bold>My Server</bold></gradient>"
lines:
  - "<gray>Welcome, <white>%player_name%</white>!"
  - ""
  - "<gray>Ping: <white>%player_ping%ms"
  - "<gray>Online: <white>%server_online%<gray>/<white>%server_max_players%"
```

Everything else has sensible defaults. The shipped config includes a fuller
example with messages and all available options.

### Main options

```yaml
enabled: true
update-interval: 20          # ticks; 20 = one second
auto-reload: true
auto-reload-check-interval: 40
hide-numbers: true
disabled-worlds: []
title: "<gradient:#34d399:#38bdf8><bold>frScore</bold></gradient>"
lines:
  - "<gray>Online: <white>%server_online%"
```

- `update-interval` controls how often placeholders are refreshed.
- `hide-numbers` removes the score numbers on the right side.
- `disabled-worlds` accepts world names; use `"*"` to disable everywhere.
- Up to **15 lines** are displayed. Extra lines are ignored with a warning.

## Live reload

Two ways to reload:

- **Automatic** — with `auto-reload: true`, frScore watches `config.yml` and
  applies changes after the file has been stable for one check interval. This
  also avoids loading a file while an editor is still writing it.
- **Manual** — `/frscore reload` (permission `frscore.reload`).

If the new file cannot be parsed, the error is logged and the previous working
configuration stays active.

## Colors and formatting

[Adventure MiniMessage](https://docs.advntr.dev/minimessage/format.html) is
used for rendering. Both of these are valid:

```yaml
title: "<rainbow><bold>frScore</bold></rainbow>"
lines:
  - "<gray>Health: <red>%player_health%<dark_gray>/<#fca5a5>%player_max_health%"
  - "<gradient:#34d399:#38bdf8>Fancy line</gradient>"
  - "<bold><white>Legacy &cand &lMiniMessage &7work together"
```

Legacy syntax is converted automatically for you:

| Legacy | Result |
| --- | --- |
| `&a` / `§a` | green |
| `&l` / `§l` | bold |
| `&r` / `§r` | reset |
| `&#ff8800` | hex color |
| `&x&f&f&8&8&0&0` | hex color |

## Placeholders

### PlaceholderAPI

Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)
and any expansions you need. Placeholders are resolved before MiniMessage is
parsed, so values can contain colors or formatting:

```yaml
lines:
  - "<gray>Balance: <green>%vault_eco_balance%"
  - "<gray>Rank: <white>%luckperms_primary_group_name%"
  - "<gray>PAPI: <white>%player_health_rounded%"
```

If PlaceholderAPI is absent, frScore still works; the built-in placeholders
below are always available.

### Built-in placeholders

| Placeholder | Value |
| --- | --- |
| `%player_name%` / `%player%` | Player name |
| `%player_displayname%` | Display name, plain text |
| `%player_uuid%` | UUID |
| `%player_world%` | Current world |
| `%player_x%`, `%player_y%`, `%player_z%` | Block coordinates |
| `%player_ping%` | Ping in milliseconds |
| `%player_health%` | Current health |
| `%player_max_health%` | Maximum health |
| `%player_food%` | Food level |
| `%player_level%` | XP level |
| `%player_exp%` | XP progress, 0–100 |
| `%player_gamemode%` | Game mode, lowercase |
| `%player_sneaking%` | `true` / `false` |
| `%player_flying%` | `true` / `false` |
| `%server_online%` | Online player count |
| `%server_max_players%` | Player limit |
| `%server_tps%` / `%server_tps_1m%` | 1-minute TPS |
| `%server_tps_5m%` | 5-minute TPS |
| `%server_tps_15m%` | 15-minute TPS |
| `%server_time%` | Server local time (`HH:mm`) |
| `%server_date%` | Server local date (`yyyy-MM-dd`) |
| `%server_name%` | Server implementation name |

PlaceholderAPI is given priority: if an installed expansion recognizes a
placeholder, its value wins. Built-ins fill in anything PAPI leaves untouched.

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/frscore toggle` | Show or hide your scoreboard | `frscore.toggle` |
| `/frscore reload` | Reload `config.yml` | `frscore.reload` |
| `/frscore help` | Show help | `frscore.use` |

Aliases: `/fs`, `/frboard`.

The player's toggle choice is stored in their player data, so it survives
restarts and plugin reloads.

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `frscore.use` | everyone | Use the `/frscore` command |
| `frscore.toggle` | everyone | Toggle the scoreboard |
| `frscore.reload` | operators | Reload the configuration |

## Build from source

Requires JDK 21. The Gradle wrapper is included.

```bash
./gradlew clean build
```

The plugin jar is written to `build/libs/frScore-1.0.0.jar`. Tests run with
`./gradlew test`.

## Project layout

```
src/main/java/dev/frscore/
├── FrScorePlugin.java        plugin lifecycle, reloading
├── command/                  /frscore command and tab completion
├── config/                   config snapshot, messages, file watcher
├── listener/                 join, quit and world-change handling
├── placeholder/              PlaceholderAPI + built-in placeholders
├── scoreboard/               per-player scoreboard management
└── text/                     MiniMessage rendering and legacy conversion
```

## Notes

- frScore creates one independent sidebar per player, so toggles and world
  restrictions never affect other players.
- Lines are rendered with scoreboard team prefixes. That is what allows long,
  fully formatted lines while keeping the score numbers hidden.
- The update task runs on the Paper main thread. The default of 20 ticks is
  plenty for clock-style placeholders; increase it for large servers.
- PlaceholderAPI is a soft dependency. No PAPI classes are loaded when the
  plugin is not installed.

## Troubleshooting

**No scoreboard appears**

Check that `enabled: true`, that you are not in a `disabled-worlds` entry, and
that you have not hidden it with `/frscore toggle`.

**A PAPI placeholder shows as raw text**

The expansion is probably not installed. Install it with
`/papi ecloud download <Expansion>`, then `/papi reload`.

**A color does not work**

MiniMessage uses angle brackets (`<red>`), while legacy colors use `&` or `§`.
Make sure the tag is spelled correctly. A malformed tag in a line falls back to
plain text instead of breaking the whole scoreboard.

**My line disappears**

Minecraft displays at most 15 sidebar lines. Check the console for a warning
about extra lines.
