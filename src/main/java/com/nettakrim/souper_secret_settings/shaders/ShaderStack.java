package com.nettakrim.souper_secret_settings.shaders;

import java.util.ArrayList;

public class ShaderStack {
    public ArrayList<ShaderData> shaderDatas;

    public ShaderStack() {
        shaderDatas = new ArrayList<>();
    }

    public void addShaderData(ShaderData shaderData) {
        shaderDatas.add(shaderData);
    }
}
