package com.nettakrim.souper_secret_settings.shaders;

import com.nettakrim.souper_secret_settings.shaders.parameters.Calculation;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ObjectAllocator;

import java.util.ArrayList;

public class ShaderStack {
    public ArrayList<ShaderData> shaderDatas;

    public ArrayList<Calculation> calculations;

    public ShaderStack() {
        shaderDatas = new ArrayList<>();
        calculations = new ArrayList<>();
    }

    public void render(Framebuffer framebuffer, ObjectAllocator objectAllocator) {
        for (ShaderData shaderData : shaderDatas) {
            shaderData.render(framebuffer, objectAllocator);
        }
    }

    public void addShaderData(ShaderData shaderData) {
        shaderDatas.add(shaderData);
    }

    public void clear() {
        shaderDatas.clear();
    }
}
