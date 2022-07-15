/*
 * Copyright (C) 2021-2022 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatbox.config;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatbox.AdvancedChatBox;
import io.github.darkkronicle.advancedchatcore.config.SaveableConfig;
import io.github.darkkronicle.advancedchatcore.config.gui.GuiConfig;
import io.github.darkkronicle.advancedchatcore.gui.buttons.NamedSimpleButton;

import java.util.ArrayList;
import java.util.List;

public class GuiChatBoxConfig extends GuiConfigsBase {

    public GuiChatBoxConfig() {
        super(10, 80, AdvancedChatBox.MOD_ID, null, "advancedchat.screen.main");
    }

    @Override
    public void initGui() {
        super.initGui();
        this.clearOptions();

        int x = 10;
        int y = 26;

        y += GuiConfig.addTabButtons(this, x, y) * 22;
        y += GuiConfig.addAllChildrenButtons(this, GuiConfig.TAB, x, y) * 22;

        x = width - 2;
        x -= addButton(x, y, "advancedchatbox.config.button.config_formatters",
                (button, mouseButton) -> GuiBase.openGui(new GuiFormatterRegistry(this))) + 2;
        x -= addButton(x, y, "advancedchatbox.config.button.config_suggestors",
                (button, mouseButton) -> GuiBase.openGui(new GuiSuggestorRegistry(this))) + 2;

        int scrollbarPosition = this.getListWidget().getScrollbar().getValue();
        this.setListPosition(this.getListX(), y);
        this.reCreateListWidget();
        this.getListWidget().getScrollbar().setValue(scrollbarPosition);
        this.getListWidget().refreshEntries();
    }

    private int addButton(int x, int y, String translation, IButtonActionListener listener) {
        return this.addButton(new NamedSimpleButton(x, y, StringUtils.translate(translation), false), listener).getWidth();
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        List<SaveableConfig<? extends IConfigBase>> configs = ChatBoxConfigStorage.General.OPTIONS;

        ArrayList<IConfigBase> config = new ArrayList<>();
        for (SaveableConfig<? extends IConfigBase> s : configs) {
            config.add(s.config);
        }

        return ConfigOptionWrapper.createFor(config);
    }
}
