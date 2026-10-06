<div align="center">

<img src="src/main/resources/assets/message-of-the-day-motd/icon.png" width="220" alt="MOTD Icon">

# Message of the Day (MOTD)

**A simple server-side Fabric mod for displaying a custom Message of the Day to players when they join.**

</div>

---

## About

**Message of the Day (MOTD)** is a lightweight server-side Fabric mod that lets server administrators display a custom message to players when they join the server.

It's ideal for welcome messages, daily updates, server announcements, changelogs, or letting players know what's new.

Messages can be changed entirely in-game without restarting the server or manually editing configuration files.

---

## Features

- Displays a custom MOTD when players join
- Change the MOTD in-game
- Enable or disable the MOTD in-game
- Supports multi-line messages using `\n`
- Configurable rejoin interval
- Prevents the MOTD from being repeatedly shown to players who quickly reconnect
- Changes are saved automatically
- No server restart required
- Server-side only
- Players do **not** need the mod installed

---

## Commands

| Command                    | Description                                                         |
| -------------------------- | ------------------------------------------------------------------- |
| `/motd set <message>`      | Sets the Message of the Day                                         |
| `/motd enable`             | Enables the MOTD                                                    |
| `/motd disable`            | Disables the MOTD                                                   |
| `/motd interval <minutes>` | Sets how long a player must be offline before seeing the MOTD again |
| `/motd help`               | Displays the available commands                                     |

Administrative commands require server operator permissions.

---

## Multi-Line Messages

Use `\n` anywhere in your message to create a new line.

For example:

```text
/motd set Welcome back!\nToday's Changes:\n- Added new blocks\n- Updated spawn\n\nHave fun!
```

Players will see:

```text
Welcome back!
Today's Changes:
- Added new blocks
- Updated spawn

Have fun!
```

---

## Rejoin Interval

The rejoin interval controls how long a player must remain **offline** before the MOTD is displayed to them again.

The default is:

```text
60 minutes
```

For example:

```text
/motd interval 60
```

A player who disconnects and rejoins within 60 minutes will **not** receive the MOTD again.

Once they have been offline for at least 60 minutes, the MOTD will be displayed the next time they join.

To display the MOTD on **every login**, use:

```text
/motd interval 0
```

Rejoin timers are stored in memory and reset when the server restarts. Therefore, players will receive the MOTD on their first login after a server restart.

---

## Configuration

The configuration file is automatically created at:

```text
config/motd.json
```

Example:

```json
{
  "enabled": true,
  "intervalMinutes": 60,
  "message": "Welcome to the server!"
}
```

The configuration is automatically updated when commands are used, so manual editing is not normally required.

---

## Installation

1. Install **Fabric Loader**.
2. Install **Fabric API**.
3. Download the MOTD `.jar`.
4. Place it inside the server's `mods` folder.
5. Start the server.
6. Set your message with `/motd set <message>`.

> **This is a server-side mod. Players do not need MOTD installed on their clients.**

---

## Requirements

- **Minecraft:** 26.2
- **Mod Loader:** Fabric
- **Fabric Loader:** 0.19.5+
- **Fabric API:** Required
- **Java:** 25

---

## Example

```text
/motd set Welcome back!\n\nToday's Updates:\n- New building blocks added\n- Spawn has been updated\n- New server features are available\n\nHave fun!
```

Every player joining for the first time that session — or after being offline for the configured interval — will receive the message.

---

## Source Code & Issues

Source code and issue tracking are available on the GitHub repository:

https://github.com/PeterKemley/Message-of-the-day-MOTD-

---

## License

Licensed under **CC0-1.0**.
