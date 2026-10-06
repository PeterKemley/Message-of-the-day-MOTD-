package motd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

public class MessageOfTheDayMOTD implements ModInitializer {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("motd.json");

    private static MotdConfig config = new MotdConfig();

    @Override
    public void onInitialize() {

        loadConfig();

        // Send the MOTD to a player when they join.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {

            if (config.enabled && config.message != null && !config.message.isBlank()) {
                handler.getPlayer().sendSystemMessage(
                        Component.literal(config.message)
                );
            }
        });

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {

                    dispatcher.register(
                            Commands.literal("motd")

                                    // /motd set <message>
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

                                                                        config.message = message;
                                                                        saveConfig();

                                                                        context.getSource().sendSuccess(
                                                                                () -> Component.literal(
                                                                                        "MOTD set to: " + message
                                                                                ),
                                                                                false
                                                                        );

                                                                        return 1;
                                                                    })
                                                    )
                                    )

                                    // /motd enable
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

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "MOTD enabled."
                                                                ),
                                                                false
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // /motd disable
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

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "MOTD disabled."
                                                                ),
                                                                false
                                                        );

                                                        return 1;
                                                    })
                                    )

                                    // /motd help
                                    .then(
                                            Commands.literal("help")
                                                    .executes(context -> {

                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal(
                                                                        "----- MOTD Help -----\n" +
                                                                        "/motd set <message> - Sets the join MOTD\n" +
                                                                        "/motd enable - Enables the join MOTD\n" +
                                                                        "/motd disable - Disables the join MOTD\n" +
                                                                        "/motd help - Shows this help message"
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
                    GSON.fromJson(json, MotdConfig.class);

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

        String message = "Welcome to the server!";
    }
}