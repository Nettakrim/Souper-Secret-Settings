package com.nettakrim.souper_secret_settings.gui;

import com.mclegoman.luminance.client.data.ClientData;
import com.mclegoman.luminance.client.shaders.Shaders;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.option.OptionScreen;
import com.nettakrim.souper_secret_settings.gui.layers.LayerScreen;
import com.nettakrim.souper_secret_settings.gui.parameters.ParameterScreen;
import com.nettakrim.souper_secret_settings.gui.shaders.ShaderScreen;
import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class SoupGui {
    public static final WidgetSprites BUTTON_TEXTURES = new WidgetSprites(Identifier.withDefaultNamespace("widget/button"), Identifier.withDefaultNamespace("widget/button_disabled"), Identifier.withDefaultNamespace("widget/button_highlighted"));

    private final List<AbstractWidget> header;

    public final int[] currentScroll;

    public static final int listGap = 2;
    public static final int headerHeight = 42;
    public static final int listWidth = 150;
    public static final int listStart = headerHeight + listGap*2;
    public static final int scrollWidth = 6;
    public static final int listX = listGap*2+scrollWidth;
    public static final int headerWidthLarge = 214;
    public static final int headerWidthSmall = listWidth+scrollWidth+listGap;

    private ScreenType currentScreenType = ScreenType.SHADERS;
    private Component currentHoverText;

    private Screen previous = null;

    public SoupGui() {
        header = new ArrayList<>();

        int x;
        int mainWidth = (headerWidthLarge -listGap*2)/3;
        int smallWidth = 12;

        x = listGap;
        x += addHeaderButton(Button.builder(Component.empty(),  (widget) -> open(ScreenType.LAYERS, false)).bounds(x, listGap, (mainWidth*3+listGap*2)-(smallWidth*4+listGap*3)-listGap, 20).build());
        x += addHeaderButton(new HoverButtonWidget(x, listGap, smallWidth, 20, SouperSecretSettingsClient.translate("gui.undo"), null, (widget) -> undo()));
        x += addHeaderButton(new HoverButtonWidget(x, listGap, smallWidth, 20, SouperSecretSettingsClient.translate("gui.redo"), null, (widget) -> redo()));
        x += addHeaderButton(Button.builder(SouperSecretSettingsClient.soupRenderer.getRenderLocationText(), SouperSecretSettingsClient.soupRenderer::cycleRenderLocation).bounds(x, listGap, smallWidth, 20).build());
             addHeaderButton(Button.builder(SouperSecretSettingsClient.translate("gui.config"), (widget) -> open(ScreenType.OPTION, false)).bounds(x, listGap, smallWidth, 20).build());

        x = listGap;
        x += addHeaderButton(Button.builder(SouperSecretSettingsClient.translate("gui.shaders"),    (widget) -> open(ScreenType.SHADERS   , false)).bounds(x, listGap*2 + 20, mainWidth, 20).build());
        x += addHeaderButton(Button.builder(SouperSecretSettingsClient.translate("gui.modifiers"),  (widget) -> open(ScreenType.MODIFIERS , false)).bounds(x, listGap*2 + 20, mainWidth, 20).build());
             addHeaderButton(Button.builder(SouperSecretSettingsClient.translate("gui.parameters"), (widget) -> open(ScreenType.PARAMETERS, false)).bounds(x, listGap*2 + 20, mainWidth, 20).build());

        currentScroll = new int[ScreenType.values().length];
    }

    protected int addHeaderButton(Button buttonWidget) {
        header.add(buttonWidget);
        return listGap + buttonWidget.getWidth();
    }

    public void open(ScreenType screenType, boolean openNew) {
        ClientData.minecraft.setScreen(getScreen(screenType, openNew));
    }

    public Screen getScreen(ScreenType screenType, boolean openNew) {
        if (ClientData.minecraft.screen != null) {
            saveIfChangedOption();
        }

        currentScreenType = screenType;
        int index = screenType.ordinal();

        if (openNew) {
            previous = ClientData.minecraft.screen;
        }
        Screen screen = switch(screenType) {
            case LAYERS -> new LayerScreen(index);
            case SHADERS -> new ShaderScreen(index, SouperSecretSettingsClient.soupRenderer.activeLayer, Shaders.getMainRegistryId());
            case MODIFIERS -> new ShaderScreen(index, SouperSecretSettingsClient.soupRenderer.activeLayer, SoupRenderer.modifierRegistry);
            case PARAMETERS -> new ParameterScreen(index, SouperSecretSettingsClient.soupRenderer.activeLayer);
            case OPTION -> new OptionScreen(index);
        };

        for (ScreenType type : ScreenType.values()) {
            AbstractWidget clickableWidget = header.get(type.headerIndex);
            clickableWidget.active = screenType.headerIndex != type.headerIndex;
            clickableWidget.setFocused(false);
        }
        setActiveLayerMessage();

        return screen;
    }

    public void onClose() {
        saveIfChangedOption();

        ClientData.minecraft.setScreen(previous);
        previous = null;
    }

    private void saveIfChangedOption() {
        if (currentScreenType == ScreenType.OPTION) {
            SouperSecretSettingsClient.soupData.changeData(false);
            SouperSecretSettingsClient.soupData.saveConfig();
        }
    }

    public List<AbstractWidget> getHeader() {
        return header;
    }

    protected void undo() {
        SouperSecretSettingsClient.actions.undo();
        open(currentScreenType, false);
        ClientData.minecraft.schedule(() -> ((HoverButtonWidget)header.get(1)).deselect());
    }

    protected void redo() {
        SouperSecretSettingsClient.actions.redo();
        open(currentScreenType, false);
        ClientData.minecraft.schedule(() -> ((HoverButtonWidget)header.get(2)).deselect());
    }

    public void setHistoryButtons(int undoCount, int redoCount) {
        ((HoverButtonWidget)header.get(1)).setActiveText(undoCount > 0 ? SouperSecretSettingsClient.translate("gui.undo_count", undoCount) : null);
        ((HoverButtonWidget)header.get(2)).setActiveText(redoCount > 0 ? SouperSecretSettingsClient.translate("gui.redo_count", redoCount) : null);
    }

    public ScreenType getCurrentScreenType() {
        return Objects.requireNonNullElse(currentScreenType, ScreenType.SHADERS);
    }

    public void updateActiveLayerMessageOrScreen() {
        if (currentScreenType == ScreenType.LAYERS && ClientData.minecraft.screen instanceof LayerScreen) {
            open(ScreenType.LAYERS, false);
        } else {
            setActiveLayerMessage();
        }
    }

    public void setActiveLayerMessage() {
        header.getFirst().setMessage(SouperSecretSettingsClient.soupRenderer.activeLayer.name.isBlank() ? SouperSecretSettingsClient.translate("gui.layers.unnamed") : SouperSecretSettingsClient.translate("gui.layers", SouperSecretSettingsClient.soupRenderer.activeLayer.name));
    }

    public void setHoverText(Component text) {
        currentHoverText = text;
    }

    public void drawCurrentHoverText(GuiGraphics context, int mouseX, int mouseY) {
        if (currentHoverText == null) {
            return;
        }
        int offset = (mouseY > 30 && context.containsPointInScissor(mouseX, mouseY-17)) ? -13 : 13;
        context.fill(mouseX-2, mouseY+offset-2, mouseX + ClientData.minecraft.font.width(currentHoverText)+2, mouseY+offset+10, 128 << 24);
        context.drawString(ClientData.minecraft.font, currentHoverText, mouseX,  mouseY+offset, -1, true);
        currentHoverText = null;
    }

    public enum ScreenType implements StringRepresentable {
        LAYERS(0),
        SHADERS(5),
        MODIFIERS(6),
        PARAMETERS(7),
        OPTION(4);

        private final int headerIndex;

        ScreenType(int headerIndex) {
            this.headerIndex = headerIndex;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }

    public static class ScreenTypeArgumentType extends StringRepresentableArgument<@NotNull ScreenType> {
        public ScreenTypeArgumentType() {
            super(StringRepresentable.fromEnum(ScreenType::values), ScreenType::values);
        }
    }
}
