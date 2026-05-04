package com.nettakrim.souper_secret_settings.shaders.custom;

import net.minecraft.client.renderer.ShaderManager;

public class GraphCompilationException extends ShaderManager.CompilationException {
    public final Node node;

    public GraphCompilationException(String string, Node node) {
        super(string);
        this.node = node;
    }

    @Override
    public String getMessage() {
        String className = node.toString();
        return super.getMessage()+" (at "+ className.substring(className.lastIndexOf('.')+1)+")";
    }

    public String getBaseMessage() {
        return super.getMessage();
    }
}
