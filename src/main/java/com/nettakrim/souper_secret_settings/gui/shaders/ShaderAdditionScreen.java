package com.nettakrim.souper_secret_settings.gui.shaders;

import com.nettakrim.souper_secret_settings.gui.SoupButtonWidget;
import dev.dannytaylor.luminance.client.data.ClientData;
import dev.dannytaylor.luminance.common.util.Couple;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.AdditionButton;
import com.nettakrim.souper_secret_settings.gui.ListAdditionScreen;
import com.nettakrim.souper_secret_settings.gui.SoupGui;
import com.nettakrim.souper_secret_settings.gui.custom.GraphScreen;
import com.nettakrim.souper_secret_settings.shaders.Group;
import com.nettakrim.souper_secret_settings.shaders.ShaderData;
import com.nettakrim.souper_secret_settings.shaders.SoupRenderer;
import java.util.Map;

import com.nettakrim.souper_secret_settings.shaders.custom.chain.CustomPostChain;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class ShaderAdditionScreen extends ListAdditionScreen<ShaderData> {
    protected final ShaderScreen shaderScreen;

    protected ShaderAdditionScreen(ShaderScreen shaderScreen) {
        super(shaderScreen);
        this.shaderScreen = shaderScreen;
    }

    boolean isGroups;
    boolean changedGroups;

    int[] scrolls = new int[2];

    @Override
    protected int createHeader() {
        addRenderableWidget(new SoupButtonWidget(Component.translatable("gui.back"), (widget) -> onClose(), SoupGui.listGap, SoupGui.listGap, SoupGui.headerWidthSmall, 20));

        int halfWidth = (SoupGui.headerWidthSmall-SoupGui.listGap)/2;
        addRenderableWidget(new SoupButtonWidget(SouperSecretSettingsClient.translate(isGroups ? "gui.groups" : (shaderScreen.registry == SoupRenderer.modifierRegistry ? "gui.modifiers" : "gui.shaders")), (widget) -> toggleMode(), SoupGui.listGap, SoupGui.listGap*2 + 20, halfWidth, 20));
        addRenderableWidget(new SoupButtonWidget(SouperSecretSettingsClient.translate(isGroups ? "gui.groups.create" : "gui.shader.create"), this::create, SoupGui.listGap*2+halfWidth, SoupGui.listGap*2 + 20, halfWidth, 20)).active = isGroups || shaderScreen.registry != SoupRenderer.modifierRegistry;

        return SoupGui.listStart;
    }

    @Override
    protected void createAdditionButton(String addition) {
        if (isGroups != addition.startsWith("random_")) {
            return;
        }

        if (isGroups) {
            String name = addition.substring(7);
            createGroupButton(name, getRegistryGroups().get(name));
            return;
        }

        super.createAdditionButton(addition);
    }

    protected AdditionButton createGroupButton(String name, Group group) {
        Couple<Component, Component> text;
        group.requestUpdate();

        String title = name.replaceFirst("/", ":");

        int recursionIndex = group.getStepAmounts(shaderScreen.registry, name).indexOf(null);
        if (recursionIndex >= 0) {
            text = new Couple<>(SouperSecretSettingsClient.translate("gui.group_error", title).setStyle(Style.EMPTY.withColor(0xFF1010)), SouperSecretSettingsClient.translate("gui.group_loop_index", recursionIndex));
        } else {
            int size = group.getComputed(shaderScreen.registry, name).size();
            text = new Couple<>(Component.literal(title), SouperSecretSettingsClient.translate("shader.group_suggestion", size));
        }

        AdditionButton groupButton = new AdditionButton("random_"+name, text, this::add, SoupGui.listWidth, 20, SoupGui.listX);

        if (name.startsWith("user/")) {
            groupButton.addRemoveListener(this::removeGroup);
        }
        groupButton.addEditListener(this::selectGroup);
        children.add(groupButton);
        addWidget(groupButton);

        return groupButton;
    }

    @Override
    public void setScroll(int scroll) {
        super.setScroll(scroll);
        scrolls[isGroups ? 1 : 0] = scroll;
    }

    protected void toggleMode() {
        isGroups = !isGroups;
        int scroll = scrolls[isGroups ? 1 : 0];
        rebuildWidgets();
        scrollWidget.offsetScroll(scroll);
    }

    protected void selectGroup(AdditionButton additionButton) {
        String name = additionButton.addition.substring(7);
        ClientData.minecraft.setScreen(new GroupEditScreen(this, getRegistryGroups().get(name), name));
        changedGroups = true;
    }

    protected void create(Button widget) {
        if (isGroups) {
            createGroup();
        } else {
            createShader();
        }
    }

    protected void createGroup() {
        Map<String, Group> map = getRegistryGroups();
        String name = Group.getNextName(map);

        Group group = new Group();
        map.put(name, group);

        selectGroup(createGroupButton(name, group));
        changedGroups = true;
        shaderScreen.recalculateAdditions();
    }

    protected void createShader() {
        onClose();
        CustomPostChain customPostChain = new CustomPostChain();
        listScreen.addAdditionInstance(new ShaderData(customPostChain, customPostChain.chainGraph.graphId, null, customPostChain.chainGraph));
        minecraft.setScreen(new GraphScreen(customPostChain.chainGraph, listScreen));
    }

    protected void removeGroup(AdditionButton button) {
        removeAddition(button);
        changedGroups = true;

        getRegistryGroups().remove(button.addition.substring(7)).deleteFile();
    }

    public Map<String, Group> getRegistryGroups() {
        return SouperSecretSettingsClient.soupRenderer.getShaderGroups(shaderScreen.registry);
    }

    @Override
    public void onClose() {
        if (changedGroups) {
            shaderScreen.recalculateAdditions();
            SouperSecretSettingsClient.soupData.changeData(true);
            SouperSecretSettingsClient.soupData.saveConfig();
        }
        super.onClose();
    }
}
