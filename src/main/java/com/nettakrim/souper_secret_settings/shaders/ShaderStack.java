package com.nettakrim.souper_secret_settings.shaders;

import com.nettakrim.souper_secret_settings.shaders.calculations.Calculation;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ObjectAllocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShaderStack {
    public ArrayList<ShaderData> shaderDatas;

    public ArrayList<Calculation> calculations;
    public Map<String, Float> parameterValues;

    public ShaderStack() {
        shaderDatas = new ArrayList<>();
        calculations = new ArrayList<>();
        parameterValues = new HashMap<>();
    }

    public void render(Framebuffer framebuffer, ObjectAllocator objectAllocator) {
        renderingStack = this;
        parameterValues.clear();
        for (Calculation calculation : calculations) {
            calculation.update(this);
        }

        for (ShaderData shaderData : shaderDatas) {
            shaderData.render(framebuffer, objectAllocator);
        }
        renderingStack = null;
    }

    public void addShaderData(ShaderData shaderData) {
        shaderDatas.add(shaderData);
    }

    public void clear() {
        shaderDatas.clear();
    }

    private static ShaderStack renderingStack;
    public static ShaderStack getRenderingStack() {
        return renderingStack;
    }
}
