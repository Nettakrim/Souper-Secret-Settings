package com.nettakrim.souper_secret_settings.gui.option;

import dev.dannytaylor.luminance.client.data.ClientData;
import dev.dannytaylor.luminance.client.gui.screen.config.ConfigScreen;
import dev.dannytaylor.luminance.client.gui.widget.AlphaSliderWidget;
import dev.dannytaylor.luminance.client.keybindings.Keybindings;
import dev.dannytaylor.luminance.client.shaders.Shaders;
import dev.dannytaylor.luminance.client.shaders.Uniforms;
import com.mojang.brigadier.StringReader;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.actions.Actions;
import com.nettakrim.souper_secret_settings.gui.*;
import com.nettakrim.souper_secret_settings.gui.shaders.ShaderScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OptionScreen extends ScrollScreen {
    public final List<AbstractWidget> widgets = new ArrayList<>();

    private final int scrollIndex;

    private final String[] previousItems = new String[2];
    private final CompletableFuture<?>[] itemFutures = new CompletableFuture[2];

    public OptionScreen(int scrollIndex) {
        super(Component.empty());
        this.scrollIndex = scrollIndex;
    }

    @Override
    protected void init() {
        for (AbstractWidget clickableWidget : SouperSecretSettingsClient.soupGui.getHeader()) {
            addRenderableWidget(clickableWidget);
        }

        createScrollWidget(SoupGui.listStart);

        int widgetWidth = SoupGui.headerWidthLarge - SoupGui.scrollWidth - SoupGui.listGap;

        widgets.clear();

        widgets.add(new StringWidget(SoupGui.listX, 0, widgetWidth, 8, SouperSecretSettingsClient.translate("option.gui.main"), ClientData.minecraft.font));
        widgets.add(new CycleButton(() -> SouperSecretSettingsClient.translate("option.gui.toggle."+SouperSecretSettingsClient.soupData.config.disableState), (direction) -> SouperSecretSettingsClient.soupData.config.disableState = CycleButton.cycleInt(SouperSecretSettingsClient.soupData.config.disableState + direction, 2), widgetWidth, SoupGui.listX
                )
        );
        AbstractSliderButton sliderWidget = new SoupAlphaSlider(SoupGui.listX, 0, widgetWidth, 20, Uniforms.getRawAlpha() / 100.0F, () -> Uniforms.updatingAlpha = true);
        widgets.add(sliderWidget);

        widgets.add(new StringWidget(SoupGui.listX, 0, widgetWidth, 8, SouperSecretSettingsClient.translate("option.gui.eating"), ClientData.minecraft.font));
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, SouperSecretSettingsClient.translate("option.gui.random"),
                (x, width) -> {
                    SuggestionEditBoxWidget widget = new SuggestionEditBoxWidget(x, width, 20, Component.empty(), false);
                    widget.setMaxLengthMin(1024);
                    CompletableFuture.runAsync(() -> widget.setValue(itemStackToString(SouperSecretSettingsClient.soupData.config.randomItem)));
                    widget.setListeners(this::getItemSuggestions, null, true);
                    widget.setResponder(value -> getItemStack(value, 1, itemStack -> SouperSecretSettingsClient.soupData.config.randomItem = itemStack));
                    widget.disableDrag = true;
                    return widget;
                })
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, SouperSecretSettingsClient.translate("option.gui.shader"),
                (x, width) -> {
                    SuggestionEditBoxWidget widget = new SuggestionEditBoxWidget(x, width, 20, Component.empty(), false);
                    widget.setValue(SouperSecretSettingsClient.soupData.config.randomShader);
                    widget.setListeners(() -> ShaderScreen.calculateAdditions(Shaders.getMainRegistryId()), null, true);
                    widget.setResponder((value) -> SouperSecretSettingsClient.soupData.config.randomShader = value);
                    widget.disableDrag = true;
                    return widget;
                })
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, SouperSecretSettingsClient.translate("option.gui.count"),
                (x, width) -> {
                    DraggableIntEditBoxWidget widget = new DraggableIntEditBoxWidget(x, width, 20, Component.empty(), 1, 256, 1, (value) -> SouperSecretSettingsClient.soupData.config.randomCount = value);
                    widget.setValue(Integer.toString(SouperSecretSettingsClient.soupData.config.randomCount));
                    return widget;
                })
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, SouperSecretSettingsClient.translate("option.gui.duration"),
                (x, width) -> {
                    DraggableIntEditBoxWidget widget = new DraggableIntEditBoxWidget(x, width, 20, Component.empty(), 0, Integer.MAX_VALUE, 0, (value) -> SouperSecretSettingsClient.soupData.config.randomDuration = value);
                    widget.setValue(Integer.toString(SouperSecretSettingsClient.soupData.config.randomDuration));
                    return widget;
                })
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, SouperSecretSettingsClient.translate("option.gui.clear"),
                (x, width) -> {
                    SuggestionEditBoxWidget widget = new SuggestionEditBoxWidget(x, width, 20, Component.empty(), false);
                    widget.setMaxLengthMin(1024);
                    CompletableFuture.runAsync(() -> widget.setValue(itemStackToString(SouperSecretSettingsClient.soupData.config.clearItem)));
                    widget.setListeners(this::getItemSuggestions, null, true);
                    widget.setResponder(value -> getItemStack(value, 0, itemStack -> SouperSecretSettingsClient.soupData.config.clearItem = itemStack));
                    widget.disableDrag = true;
                    return widget;
                })
        );
        widgets.add(new CycleButton(() -> SouperSecretSettingsClient.translate("option.gui.sound."+(SouperSecretSettingsClient.soupData.config.randomSound ? "on" : "off")), (direction) -> SouperSecretSettingsClient.soupData.config.randomSound = !SouperSecretSettingsClient.soupData.config.randomSound, widgetWidth, SoupGui.listX
                )
        );

        widgets.add(new StringWidget(SoupGui.listX, 0, widgetWidth, 8, SouperSecretSettingsClient.translate("option.gui.misc"), ClientData.minecraft.font));
        widgets.add(new SoupButtonWidget(SouperSecretSettingsClient.translate("option.gui.luminance"), (buttonWidget) -> ClientData.minecraft.setScreen(new ConfigScreen(this)), SoupGui.listX, 0, widgetWidth, 20));
        widgets.add(new SoupButtonWidget(SouperSecretSettingsClient.translate("option.gui.keybinds"), (buttonWidget) -> ClientData.minecraft.setScreen(new KeyBindsScreen(this, ClientData.minecraft.options)), SoupGui.listX, 0, widgetWidth, 20));
        widgets.add(new CycleButton(() -> SouperSecretSettingsClient.translate("option.gui.filter."+SouperSecretSettingsClient.soupData.config.messageFilter), (direction) -> SouperSecretSettingsClient.soupData.config.messageFilter = CycleButton.cycleInt(SouperSecretSettingsClient.soupData.config.messageFilter - direction, 2), widgetWidth, SoupGui.listX
                )
        );
        widgets.add(new CycleButton(() -> SouperSecretSettingsClient.translate("option.gui.warning."+(SouperSecretSettingsClient.soupData.config.warning ? "on" : "off")), (direction) -> SouperSecretSettingsClient.soupData.config.warning = !SouperSecretSettingsClient.soupData.config.warning, widgetWidth, SoupGui.listX
                )
        );
        widgets.add(new LabelledWidget(SoupGui.listX, widgetWidth, SouperSecretSettingsClient.translate("option.gui.undo_limit"),
                (x, width) -> {
                    DraggableIntEditBoxWidget widget = new DraggableIntEditBoxWidget(x, width, 20, Component.empty(), 16, Integer.MAX_VALUE, Actions.defaultLength, (value) -> SouperSecretSettingsClient.soupData.config.undoLimit = value);
                    widget.setValue(Integer.toString(SouperSecretSettingsClient.soupData.config.undoLimit));
                    return widget;
                })
        );

        int height = -2;
        for (AbstractWidget widget : widgets) {
            if (widget instanceof StringWidget) {
                height += 5;
            }

            if (widget instanceof LabelledWidget labelledWidget) {
                addWidget(labelledWidget.widget);
            } else {
                addWidget(widget);
            }
            height += widget.getHeight() + SoupGui.listGap;
        }
        scrollWidget.setContentHeight(height - SoupGui.listGap);

        scrollWidget.offsetScroll(SouperSecretSettingsClient.soupGui.currentScroll[scrollIndex]);
    }

    @Override
    public void setScroll(int scroll) {
        int y = (SoupGui.headerHeight - scroll) + SoupGui.listGap*2 - 2;

        for (AbstractWidget widget : widgets) {
            if (widget instanceof StringWidget) {
                y += 5;
            }

            widget.setY(y);
            y += widget.getHeight() + SoupGui.listGap;
        }

        SouperSecretSettingsClient.soupGui.currentScroll[scrollIndex] = scroll;
    }

    @Override
    protected void renderScrollables(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        for (AbstractWidget widget : widgets) {
            widget.render(guiGraphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public void onClose() {
        SouperSecretSettingsClient.soupGui.onClose();
    }

    private List<String> getItemSuggestions() {
        List<String> items = new ArrayList<>(BuiltInRegistries.ITEM.size());
        BuiltInRegistries.ITEM.forEach((item) -> items.add(item.toString()));
        return items;
    }

    private void getItemStack(String s, int index, Consumer<ItemStack> consumer) {
        if (s.equals(previousItems[index])) {
            return;
        }
        previousItems[index] = s;

        if (itemFutures[index] != null) {
            itemFutures[index].cancel(false);
        }

        itemFutures[index] = CompletableFuture.runAsync(() -> {
            try {
                StringReader stringReader = new StringReader(s);
                ItemParser.ItemResult result = new ItemParser(VanillaRegistries.createLookup()).parse(stringReader);
                ItemStack itemStack = new ItemStack(result.item(), 1);
                itemStack.applyComponents(result.components());
                consumer.accept(itemStack);
            } catch (Exception ignored) {}
        });
    }

    private String itemStackToString(ItemStack itemStack) {
        return new ItemInput(itemStack.getItemHolder(), itemStack.getComponentsPatch()).serialize(VanillaRegistries.createLookup());
    }

    private static class SoupAlphaSlider extends AlphaSliderWidget {
        public SoupAlphaSlider(int x, int y, int width, int height, double value, Runnable onChange) {
            super(x, y, width, height, value, onChange);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
            super.renderWidget(guiGraphics, mouseX, mouseY, delta);
            if (isHovered) {
                SouperSecretSettingsClient.soupGui.setHoverText(SouperSecretSettingsClient.translate("option.gui.alpha", Keybindings.adjustAlpha.getTranslatedKeyMessage()));
            }
        }
    }
}
