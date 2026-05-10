package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import net.minecraft.client.renderer.PostChainConfig;

interface InputInfo {
    // TODO: texture sampler implementation
    PostChainConfig.Input getInput(String inputName);
}
