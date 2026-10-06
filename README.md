# Message of the Day (MOTD)

A simple server-side Fabric mod that allows you to change, enable, and disable your Minecraft server's MOTD in-game.

Designed for Minecraft **26.2** using Fabric.

## Features

- Change the server MOTD without restarting the server
- Enable or disable the custom MOTD in-game
- MOTD is saved between server restarts
- Simple JSON configuration
- Server-side only
- No client installation required

## Commands

| Command | Description |
| --- | --- |
| `/motd set <message>` | Sets the server MOTD |
| `/motd enable` | Enables the custom MOTD |
| `/motd disable` | Disables the custom MOTD |
| `/motd help` | Shows the available commands |

The `set`, `enable`, and `disable` commands require administrator permissions.

## Configuration

The configuration file is automatically generated when the server is started:

`config/motd.json`

Example:

```json
{
  "enabled": true,
  "message": "Welcome to the server!"
}
```

The configuration is automatically updated when the MOTD is changed in-game.

## Installation

1. Install Fabric Loader for Minecraft 26.2.
2. Install Fabric API.
3. Place the MOTD `.jar` file into the server's `mods` folder.
4. Start the server.
5. Use `/motd set <message>` to set your MOTD.

This mod only needs to be installed on the server.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.5 or newer
- Fabric API
- Java 25

## License

CC0-1.0