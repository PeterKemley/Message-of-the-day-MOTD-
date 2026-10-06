package motd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageOfTheDayMOTD implements ModInitializer {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("motd.json");

    private static MotdConfig config = new MotdConfig();

    /*
     * Stores the time each player last logged out.
     *
     * This is deliberately kept in memory only.
     * Restarting the server clears the logout history.
     */
    private static final Map<UUID, Long> LAST_LOGOUT = new HashMap<>();

    @Override
    public void onInitialize() {

        loadConfig();

        /*
         * Send the MOTD when a player joins.
         */
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {

            if (!config.enabled
                    || config.message == null
                    || config.message.isBlank()) {
                return;
            }

            UUID uuid = handler.getPlayer().getUUID();

            /*
             * Interval 0 means show the MOTD on every login.
             */
            if (config.intervalMinutes == 0) {

                handler.getPlayer().sendSystemMessage(
                        Component.literal(config.message)
                );

                return;
            }

            Long lastLogout = LAST_LOGOUT.get(uuid);

            /*
             * If we have no logout time, this is their first login
             * since the server started, so show the MOTD.
             */
            if (lastLogout == null) {

                handler.getPlayer().sendSystemMessage(
                        Component.literal(config.message)
                );

                return;
            }

            long intervalMilliseconds =
                    config.intervalMinutes * 60_000L;

            long timeOffline =
                    System.currentTimeMillis() - lastLogout;

            /*
             * Only show the MOTD again if they have been offline
             * for at least the configured interval.
             */
            if (timeOffline >= intervalMilliseconds) {

                handler.getPlayer().sendSystemMessage(
                        Component.literal(config.message)
                );
            }
        });

        /*
         * Record when a player logs out.
         */
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {

            LAST_LOGOUT.put(
                    handler.getPlayer().getUUID(),
                    System.currentTimeMillis()
            );
        });

        /*
         * Register /motd commands.
         */
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    dispatcher.register(
                            Commands.literal("motd")

                                    /*
                                     * /motd set <message>
                                     */
                                    .then(
                                            Commands.literal("set")
                                                    .requires(
                                                            Commands.hasPermission(
                                                                    Commands.LEVEL_ADMINS
                                                            )
                                                    )
                                                    .then(
                                                            Commands.argument(
                                                                            "message",
                                                                            StringArgumentType.greedyString()
                                                                    )
                                                                    .executes(context -> {

                                                                        String message =
                                                                                StringArgumentType.getString(
                                                                                        context,
                                                                                        "message"
                                                                                );

                                                                        /*
                                                                         * Convert \n typed in the command
                                                                         * into actual new lines.
                                                                         */
                                                                        config.message =
                                                                                message.replace(
                                                                                        "\\n",
                                                                                        "\n"
                                                                                );

                                                                        saveConfig();

                                                                        context.getSource()
                                                                                .sendSuccess(
                                                                                        () -> Component.literal(
                                                                                                "MOTD set to:\n"
                                                                                                        + config.message
                                                                                        ),
                                                                                        false
                                                                                );

                                                                        return 1;
                                                                    })
                                                    )
                                    )

                                    /*
                                     * /motd enable
                                     */
                                    .then(
                                            Commands.literal("enable")
                                                    .requires(
                                                            Commands.hasPermission(
                                                                    Commands.LEVEL_ADMINS
                                                            )
                                                    )
                                                    .executes(context -> {

                                                        config.enabled = true;

                                                        saveConfig();

                                                        context.getSource()
                                                                .sendSuccess(
                                                                        () -> Component.literal(
                                                                                "MOTD enabled."
                                                                        ),
                                                                        false
                                                                );

                                                        return 1;
                                                    })
                                    )

                                    /*
                                     * /motd disable
                                     */
                                    .then(
                                            Commands.literal("disable")
                                                    .requires(
                                                            Commands.hasPermission(
                                                                    Commands.LEVEL_ADMINS
                                                            )
                                                    )
                                                    .executes(context -> {

                                                        config.enabled = false;

                                                        saveConfig();

                                                        context.getSource()
                                                                .sendSuccess(
                                                                        () -> Component.literal(
                                                                                "MOTD disabled."
                                                                        ),
                                                                        false
                                                                );

                                                        return 1;
                                                    })
                                    )

                                    /*
                                     * /motd interval <minutes>
                                     */
                                    .then(
                                            Commands.literal("interval")
                                                    .requires(
                                                            Commands.hasPermission(
                                                                    Commands.LEVEL_ADMINS
                                                            )
                                                    )
                                                    .then(
                                                            Commands.argument(
                                                                            "minutes",
                                                                            IntegerArgumentType.integer(0)
                                                                    )
                                                                    .executes(context -> {

                                                                        int minutes =
                                                                                IntegerArgumentType.getInteger(
                                                                                        context,
                                                                                        "minutes"
                                                                                );

                                                                        config.intervalMinutes =
                                                                                minutes;

                                                                        saveConfig();

                                                                        if (minutes == 0) {

                                                                            context.getSource()
                                                                                    .sendSuccess(
                                                                                            () -> Component.literal(
                                                                                                    "MOTD interval disabled. "
                                                                                                            + "The MOTD will show on every login."
                                                                                            ),
                                                                                            false
                                                                                    );

                                                                        } else {

                                                                            context.getSource()
                                                                                    .sendSuccess(
                                                                                            () -> Component.literal(
                                                                                                    "MOTD interval set to "
                                                                                                            + minutes
                                                                                                            + " minute"
                                                                                                            + (minutes == 1 ? "." : "s.")
                                                                                            ),
                                                                                            false
                                                                                    );
                                                                        }

                                                                        return 1;
                                                                    })
                                                    )
                                    )

                                    /*
                                     * /motd help
                                     */
                                    .then(
                                            Commands.literal("help")
                                                    .executes(context -> {

                                                        context.getSource()
                                                                .sendSuccess(
                                                                        () -> Component.literal(
                                                                                "----- MOTD Help -----\n"
                                                                                        + "/motd set <message> - Sets the join MOTD\n"
                                                                                        + "/motd enable - Enables the join MOTD\n"
                                                                                        + "/motd disable - Disables the join MOTD\n"
                                                                                        + "/motd interval <minutes> - Sets how long a player must be offline before seeing the MOTD again\n"
                                                                                        + "/motd help - Shows this help message\n"
                                                                                        + "\\n - Creates a new line in the MOTD"
                                                                        ),
                                                                        false
                                                                );

                                                        return 1;
                                                    })
                                    )
                    );
                }
        );
    }

    private static void loadConfig() {

        if (!Files.exists(CONFIG_PATH)) {
            saveConfig();
            return;
        }

        try {

            String json = Files.readString(CONFIG_PATH);

            MotdConfig loaded =
                    GSON.fromJson(
                            json,
                            MotdConfig.class
                    );

            if (loaded != null) {
                config = loaded;
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to load MOTD config",
                    e
            );
        }
    }

    private static void saveConfig() {

        try {

            Files.createDirectories(
                    CONFIG_PATH.getParent()
            );

            Files.writeString(
                    CONFIG_PATH,
                    GSON.toJson(config)
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to save MOTD config",
                    e
            );
        }
    }

    private static class MotdConfig {

        boolean enabled = true;

        int intervalMinutes = 60;

        String message = "Welcome to the server!";
    }
}