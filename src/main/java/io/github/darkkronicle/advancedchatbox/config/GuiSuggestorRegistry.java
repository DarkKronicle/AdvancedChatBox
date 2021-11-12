/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatbox.config;

import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatbox.registry.ChatSuggestorRegistry;
import io.github.darkkronicle.advancedchatcore.config.gui.widgets.WidgetListRegistryOption;
import io.github.darkkronicle.advancedchatcore.config.gui.widgets.WidgetRegistryOptionEntry;
import io.github.darkkronicle.advancedchatcore.gui.CoreGuiListBase;
import io.github.darkkronicle.advancedchatcore.gui.buttons.BackButtonListener;
import io.github.darkkronicle.advancedchatcore.gui.buttons.NamedSimpleButton;
import net.minecraft.client.gui.screen.Screen;

public class GuiSuggestorRegistry extends
        CoreGuiListBase<ChatSuggestorRegistry.ChatSuggestorOption, WidgetRegistryOptionEntry<ChatSuggestorRegistry.ChatSuggestorOption>, WidgetListRegistryOption<ChatSuggestorRegistry.ChatSuggestorOption>> {

    public GuiSuggestorRegistry(Screen parent) {
        super(10, 60);
        setParent(parent);
        this.title = StringUtils.translate("advancedchatbox.screen.suggestors");
    }

    @Override
    public void initGui() {
        super.initGui();
        this.reCreateListWidget();
        int x = 10;
        int y = 30;
        this.addButton(new NamedSimpleButton(x, y, StringUtils.translate("advancedchat.gui.button.back")),
                new BackButtonListener(this));
        this.getListWidget().refreshEntries();
    }

    @Override
    protected WidgetListRegistryOption<ChatSuggestorRegistry.ChatSuggestorOption> createListWidget(int listX,
            int listY) {
        return new WidgetListRegistryOption<>(listX, listY, this.getBrowserWidth(), this.getBrowserHeight(), null,
                ChatSuggestorRegistry.getInstance(), this);
    }

    @Override
    protected int getBrowserWidth() {
        return this.width - 20;
    }

    @Override
    protected int getBrowserHeight() {
        return this.height - 6 - this.getListY();
    }

    private void back() {
        closeGui(true);
    }
}
