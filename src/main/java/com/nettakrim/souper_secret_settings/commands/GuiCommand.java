package com.nettakrim.souper_secret_settings.commands;

import dev.dannytaylor.luminance.client.data.ClientData;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class GuiCommand {
    public static void register(RootCommandNode<FabricClientCommandSource> root) {
        LiteralCommandNode<FabricClientCommandSource> commandNode = ClientCommandManager.literal("soup:gui")
                .then(
                        ClientCommandManager.argument("screen", new SoupGui.ScreenTypeArgumentType())
                                .executes(context -> open(context.getArgument("screen", SoupGui.ScreenType.class)))
                )
                .executes(context -> open(SouperSecretSettingsClient.soupGui.getCurrentScreenType()))
                .build();
        root.addChild(commandNode);
    }

    public static int open(SoupGui.ScreenType screenType) {
        // delay by a tick, since the chat screen is closed *after* the command is executed
        ClientData.minecraft.schedule(() -> SouperSecretSettingsClient.soupGui.open(screenType, true));
        return 1;
    }
}
