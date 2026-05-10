package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.resources.Identifier;

public class TargetInputInfo implements InputInfo {
    public final Identifier targetId;
    public final boolean useDepth;

    public TargetInputInfo(String id) {
        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null) {
            targetId = Identifier.withDefaultNamespace("");
            useDepth = false;
            return;
        }

        String path = identifier.getPath();

        if (path.equals("depth") && identifier.getNamespace().equals("minecraft")) {
            targetId = Identifier.withDefaultNamespace("main");
            useDepth = true;
        } else if (path.endsWith("/depth")) {
            targetId = Identifier.fromNamespaceAndPath(identifier.getNamespace(), path.substring(0, path.length()-6));
            useDepth = true;
        } else {
            targetId = identifier;
            useDepth = false;
        }
    }

    @Override
    public PostChainConfig.Input getInput(String inputName) {
        return new PostChainConfig.TargetInput(inputName, targetId, useDepth, false);
    }
}
