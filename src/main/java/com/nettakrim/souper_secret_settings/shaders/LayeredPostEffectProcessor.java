package com.nettakrim.souper_secret_settings.shaders;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.systems.RenderSystem;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidHierarchicalFileException;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LayeredPostEffectProcessor {
    protected final List<PostEffectPass> beforeStackRenderPasses = Lists.newArrayList();

    protected final List<PostEffectPass> afterStackRenderPasses = Lists.newArrayList();

    protected final List<PostEffectPass> beforeShaderRenderPasses = Lists.newArrayList();

    protected final List<PostEffectPass> afterShaderRenderPasses = Lists.newArrayList();

    protected boolean useDepth;

    private static final String MAIN_TARGET_NAME = "minecraft:main";
    private final Framebuffer mainTarget;
    private final ResourceManager resourceManager;
    private final String name;
    private final Map<String, Framebuffer> targetsByName = Maps.newHashMap();
    private final List<Framebuffer> defaultSizedTargets = Lists.newArrayList();
    private Matrix4f projectionMatrix;
    private int width;
    private int height;
    private float time;
    private float lastTickDelta;


    public LayeredPostEffectProcessor(TextureManager textureManager, ResourceManager resourceManager, Framebuffer framebuffer, Identifier id) throws IOException, JsonSyntaxException {
        this.resourceManager = resourceManager;
        this.mainTarget = framebuffer;
        this.time = 0.0F;
        this.lastTickDelta = 0.0F;
        this.width = framebuffer.viewportWidth;
        this.height = framebuffer.viewportHeight;
        this.name = id.toString();
        this.setupProjectionMatrix();
        this.parseEffect(textureManager, id);
    }

    public void updateTime(float tickDelta) {
        if (tickDelta < this.lastTickDelta) {
            this.time += 1.0F - this.lastTickDelta;
            this.time += tickDelta;
        } else {
            this.time += tickDelta - this.lastTickDelta;
        }

        for(this.lastTickDelta = tickDelta; this.time > 20.0F; this.time -= 20.0F) {}
    }

    public void beforeStackRender() {
        renderPasses(beforeStackRenderPasses);
    }

    public void afterStackRender() {
        renderPasses(afterStackRenderPasses);
    }

    public void beforeShaderRender() {
        renderPasses(beforeShaderRenderPasses);
    }

    public void afterShaderRender() {
        renderPasses(afterShaderRenderPasses);
    }

    private void renderPasses(List<PostEffectPass> passes) {
        if (passes.size() == 0) return;
        if (useDepth) {
            SouperSecretSettingsClient.client.getFramebuffer().copyDepthFrom(SouperSecretSettingsClient.depthFrameBuffer);
        }
        for (PostEffectPass postEffectPass : passes) {
            postEffectPass.render(this.time);
        }
    }

    private void parseEffect(TextureManager textureManager, Identifier id) throws IOException, JsonSyntaxException {
        Resource resource = this.resourceManager.getResourceOrThrow(id);

        try {
            Reader reader = resource.getReader();

            try {
                JsonObject jsonObject = JsonHelper.deserialize(reader);
                JsonArray jsonArray;
                int i;
                Iterator<JsonElement> arrayIterator;
                JsonElement jsonElement;
                InvalidHierarchicalFileException invalidHierarchicalFileException;
                if (JsonHelper.hasArray(jsonObject, "targets")) {
                    jsonArray = jsonObject.getAsJsonArray("targets");
                    i = 0;

                    for(arrayIterator = jsonArray.iterator(); arrayIterator.hasNext(); ++i) {
                        jsonElement = arrayIterator.next();

                        try {
                            this.parseTarget(jsonElement);
                        } catch (Exception e) {
                            invalidHierarchicalFileException = InvalidHierarchicalFileException.wrap(e);
                            invalidHierarchicalFileException.addInvalidKey("targets[" + i + "]");
                            throw invalidHierarchicalFileException;
                        }
                    }
                }

                parsePasses("beforestackrender", beforeStackRenderPasses, jsonObject, textureManager);
                parsePasses("afterstackrender", afterStackRenderPasses, jsonObject, textureManager);
                parsePasses("beforeshaderrender", beforeShaderRenderPasses, jsonObject, textureManager);
                parsePasses("aftershaderrender", afterShaderRenderPasses, jsonObject, textureManager);

            } catch (Throwable throwable) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Throwable suppressed) {
                        throwable.addSuppressed(suppressed);
                    }
                }

                throw throwable;
            }

            reader.close();

        } catch (Exception e) {
            InvalidHierarchicalFileException invalidHierarchicalFileException2 = InvalidHierarchicalFileException.wrap(e);
            String path = id.getPath();
            invalidHierarchicalFileException2.addInvalidFile(path + " (" + resource.getResourcePackName() + ")");
            throw invalidHierarchicalFileException2;
        }
    }

    public void parsePasses(String name, List<PostEffectPass> passes, JsonObject jsonObject, TextureManager textureManager) throws InvalidHierarchicalFileException {
        if (JsonHelper.hasArray(jsonObject, name)) {
            JsonArray jsonArray = jsonObject.getAsJsonArray(name);

            for (JsonElement jsonElement : jsonArray) {
                try {
                    this.parsePass(textureManager, jsonElement, passes);
                } catch (Exception e) {
                    InvalidHierarchicalFileException invalidHierarchicalFileException = InvalidHierarchicalFileException.wrap(e);
                    invalidHierarchicalFileException.addInvalidKey("passes[?]");
                    throw invalidHierarchicalFileException;
                }
            }
        }
    }

    private void parseTarget(JsonElement jsonTarget) throws InvalidHierarchicalFileException {
        if (JsonHelper.isString(jsonTarget)) {
            this.addTarget(jsonTarget.getAsString(), this.width, this.height);
        } else {
            JsonObject jsonObject = JsonHelper.asObject(jsonTarget, "target");
            String targetName = JsonHelper.getString(jsonObject, "name");
            int width = JsonHelper.getInt(jsonObject, "width", this.width);
            int height = JsonHelper.getInt(jsonObject, "height", this.height);
            if (this.targetsByName.containsKey(targetName)) {
                throw new InvalidHierarchicalFileException(targetName + " is already defined");
            }

            this.addTarget(targetName, width, height);
        }
    }

    private void parsePass(TextureManager textureManager, JsonElement jsonPass, List<PostEffectPass> passes) throws IOException {
        JsonObject jsonObject = JsonHelper.asObject(jsonPass, "pass");
        String passName = JsonHelper.getString(jsonObject, "name");
        String inTarget = JsonHelper.getString(jsonObject, "intarget");
        String outTarget = JsonHelper.getString(jsonObject, "outtarget");
        Framebuffer inFrameBuffer = this.getTarget(inTarget);
        Framebuffer outFrameBuffer = this.getTarget(outTarget);
        if (inFrameBuffer == null) {
            throw new InvalidHierarchicalFileException("Input target '" + inTarget + "' does not exist");
        } else if (outFrameBuffer == null) {
            throw new InvalidHierarchicalFileException("Output target '" + outTarget + "' does not exist");
        } else {
            PostEffectPass postEffectPass = this.addPass(passName, inFrameBuffer, outFrameBuffer, passes);
            JsonArray auxtargets = JsonHelper.getArray(jsonObject, "auxtargets", null);
            if (auxtargets != null) {
                int currentAuxIndex = 0;

                for(Iterator<JsonElement> auxTargetsIterator = auxtargets.iterator(); auxTargetsIterator.hasNext(); ++currentAuxIndex) {
                    JsonElement jsonElement = auxTargetsIterator.next();

                    try {
                        JsonObject targetObject = JsonHelper.asObject(jsonElement, "auxtarget");
                        String targetName = JsonHelper.getString(targetObject, "name");
                        String rawTargetID = JsonHelper.getString(targetObject, "id");
                        boolean currentTargetUsesDepth;
                        String targetID;
                        if (rawTargetID.endsWith(":depth")) {
                            currentTargetUsesDepth = true;
                            targetID = rawTargetID.substring(0, rawTargetID.lastIndexOf(58));
                            useDepth = true;
                        } else {
                            currentTargetUsesDepth = false;
                            targetID = rawTargetID;
                        }

                        Framebuffer renderTarget = this.getTarget(targetID);
                        if (renderTarget == null) {
                            if (currentTargetUsesDepth) {
                                throw new InvalidHierarchicalFileException("Render target '" + targetID + "' can't be used as depth buffer");
                            }

                            Identifier effectTexture = new Identifier("textures/effect/" + targetID + ".png");
                            this.resourceManager.getResource(effectTexture).orElseThrow(() -> new InvalidHierarchicalFileException("Render target or texture '" + targetID + "' does not exist"));
                            RenderSystem.setShaderTexture(0, effectTexture);
                            textureManager.bindTexture(effectTexture);
                            AbstractTexture abstractTexture = textureManager.getTexture(effectTexture);
                            int effectWidth = JsonHelper.getInt(targetObject, "width");
                            int effectHeight = JsonHelper.getInt(targetObject, "height");
                            boolean isBilinear = JsonHelper.getBoolean(targetObject, "bilinear");
                            if (isBilinear) {
                                RenderSystem.texParameter(3553, 10241, 9729);
                                RenderSystem.texParameter(3553, 10240, 9729);
                            } else {
                                RenderSystem.texParameter(3553, 10241, 9728);
                                RenderSystem.texParameter(3553, 10240, 9728);
                            }

                            Objects.requireNonNull(abstractTexture);
                            postEffectPass.addAuxTarget(targetName, abstractTexture::getGlId, effectWidth, effectHeight);
                        } else if (currentTargetUsesDepth) {
                            Objects.requireNonNull(renderTarget);
                            postEffectPass.addAuxTarget(targetName, renderTarget::getDepthAttachment, renderTarget.textureWidth, renderTarget.textureHeight);
                        } else {
                            Objects.requireNonNull(renderTarget);
                            postEffectPass.addAuxTarget(targetName, renderTarget::getColorAttachment, renderTarget.textureWidth, renderTarget.textureHeight);
                        }
                    } catch (Exception e) {
                        InvalidHierarchicalFileException auxTargetException = InvalidHierarchicalFileException.wrap(e);
                        auxTargetException.addInvalidKey("auxtargets[" + currentAuxIndex + "]");
                        throw auxTargetException;
                    }
                }
            }

            JsonArray uniforms = JsonHelper.getArray(jsonObject, "uniforms", null);
            if (uniforms != null) {
                int currentUniformIndex = 0;

                for(Iterator<JsonElement> uniformIterator = uniforms.iterator(); uniformIterator.hasNext(); ++currentUniformIndex) {
                    JsonElement uniform = uniformIterator.next();

                    try {
                        this.parseUniform(uniform, passes);
                    } catch (Exception e) {
                        InvalidHierarchicalFileException uniformException = InvalidHierarchicalFileException.wrap(e);
                        uniformException.addInvalidKey("uniforms[" + currentUniformIndex + "]");
                        throw uniformException;
                    }
                }
            }

        }
    }

    private void parseUniform(JsonElement jsonUniform, List<PostEffectPass> passes) throws InvalidHierarchicalFileException {
        JsonObject uniformObject = JsonHelper.asObject(jsonUniform, "uniform");
        String uniformName = JsonHelper.getString(uniformObject, "name");
        GlUniform glUniform = (passes.get(passes.size() - 1)).getProgram().getUniformByName(uniformName);
        if (glUniform == null) {
            throw new InvalidHierarchicalFileException("Uniform '" + uniformName + "' does not exist");
        } else {
            float[] fs = new float[4];
            int currentValue = 0;
            JsonArray values = JsonHelper.getArray(uniformObject, "values");

            for(Iterator<JsonElement> valuesIterator = values.iterator(); valuesIterator.hasNext(); ++currentValue) {
                JsonElement value = valuesIterator.next();

                try {
                    fs[currentValue] = JsonHelper.asFloat(value, "value");
                } catch (Exception e) {
                    InvalidHierarchicalFileException invalidHierarchicalFileException = InvalidHierarchicalFileException.wrap(e);
                    invalidHierarchicalFileException.addInvalidKey("values[" + currentValue + "]");
                    throw invalidHierarchicalFileException;
                }
            }

            switch (currentValue) {
                default -> {}
                case 1 -> glUniform.set(fs[0]);
                case 2 -> glUniform.set(fs[0], fs[1]);
                case 3 -> glUniform.set(fs[0], fs[1], fs[2]);
                case 4 -> glUniform.setAndFlip(fs[0], fs[1], fs[2], fs[3]);
            }

        }
    }

    public Framebuffer getSecondaryTarget(String name) {
        return this.targetsByName.get(name);
    }

    public void addTarget(String name, int width, int height) {
        Framebuffer framebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
        framebuffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.targetsByName.put(name, framebuffer);
        if (width == this.width && height == this.height) {
            this.defaultSizedTargets.add(framebuffer);
        }

    }

    public void close() {
        for (Framebuffer framebuffer : this.targetsByName.values()) {
            framebuffer.delete();
        }

        //this isn't cleared in vanilla PostEffectProcessor, potential bug?
        targetsByName.clear();

        closePasses(this.beforeStackRenderPasses);
        closePasses(this.afterStackRenderPasses);
        closePasses(this.beforeShaderRenderPasses);
        closePasses(this.afterShaderRenderPasses);
    }

    private void closePasses(List<PostEffectPass> passes) {
        for (PostEffectPass pass : passes) {
            pass.close();
        }
        passes.clear();
    }

    public PostEffectPass addPass(String programName, Framebuffer source, Framebuffer dest, List<PostEffectPass> passes) throws IOException {
        PostEffectPass postEffectPass = new PostEffectPass(this.resourceManager, programName, source, dest);
        passes.add(postEffectPass);
        return postEffectPass;
    }

    private void setupProjectionMatrix() {
        this.projectionMatrix = (new Matrix4f()).setOrtho(0.0F, (float)this.mainTarget.textureWidth, 0.0F, (float)this.mainTarget.textureHeight, 0.1F, 1000.0F);
    }

    public void setupDimensions(int targetsWidth, int targetsHeight) {
        this.width = this.mainTarget.textureWidth;
        this.height = this.mainTarget.textureHeight;
        this.setupProjectionMatrix();

        setPassesMatrix(this.beforeStackRenderPasses);
        setPassesMatrix(this.afterStackRenderPasses);
        setPassesMatrix(this.beforeShaderRenderPasses);
        setPassesMatrix(this.afterShaderRenderPasses);

        for (Framebuffer framebuffer : this.defaultSizedTargets) {
            framebuffer.resize(targetsWidth, targetsHeight, MinecraftClient.IS_SYSTEM_MAC);
        }
    }

    private void setPassesMatrix(List<PostEffectPass> passes) {
        for (PostEffectPass postEffectPass : passes) {
            postEffectPass.setProjectionMatrix(this.projectionMatrix);
        }
    }

    public final String getName() {
        return this.name;
    }

    @Nullable
    private Framebuffer getTarget(@Nullable String name) {
        if (name == null) {
            return null;
        } else {
            return name.equals("minecraft:main") ? this.mainTarget : this.targetsByName.get(name);
        }
    }
}
