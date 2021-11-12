/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatbox.suggester.gui;

import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.ITextFieldListener;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatbox.suggester.ShortcutSuggestor;
import io.github.darkkronicle.advancedchatcore.gui.WidgetConfigListEntry;
import io.github.darkkronicle.advancedchatcore.gui.buttons.NamedSimpleButton;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class ShortcutEntryListWidget extends WidgetConfigListEntry<ShortcutSuggestor.Shortcut> {

    private TextFieldWrapper<GuiTextFieldGeneric> name;
    private TextFieldWrapper<GuiTextFieldGeneric> replace;
    private List<TextFieldWrapper<GuiTextFieldGeneric>> texts = new ArrayList<>();

    public ShortcutEntryListWidget(int x, int y, int width, int height, boolean isOdd, ShortcutSuggestor.Shortcut entry,
            int listIndex, ShortcutListWidget parent) {
        super(x, y, width, height, isOdd, entry, listIndex);
        y += 1;
        int pos = x + width - 2;

        int removeWidth = addButton(pos, y, "advancedchat.config.shortcutmenu.remove", (button, mouseButton) -> {
            parent.suggestor.removeShortcut(entry);
            parent.refreshEntries();
        }) + 1;
        int nameWidth = 100;
        pos -= removeWidth;
        int replaceWidth = width - removeWidth - nameWidth + 1;
        GuiTextFieldGeneric replaceField = new GuiTextFieldGeneric(pos - replaceWidth, y, replaceWidth, 20,
                MinecraftClient.getInstance().textRenderer);
        replaceField.setMaxLength(512);
        replaceField.setText(entry.getReplace());
        replace = new TextFieldWrapper<>(replaceField, new SaveListener(this, false));
        parent.addTextField(replace);

        pos -= replaceWidth + 1;
        GuiTextFieldGeneric nameField =
                new GuiTextFieldGeneric(pos - nameWidth, y, nameWidth, 20, MinecraftClient.getInstance().textRenderer);
        nameField.setMaxLength(512);
        nameField.setText(entry.getName());
        name = new TextFieldWrapper<>(nameField, new SaveListener(this, true));
        texts.add(name);
        texts.add(replace);
        parent.addTextField(name);
    }

    @Override
    public List<TextFieldWrapper<GuiTextFieldGeneric>> getTextFields() {
        return texts;
    }

    private static class SaveListener implements ITextFieldListener<GuiTextFieldGeneric> {
        private final ShortcutEntryListWidget parent;
        private final boolean name;

        public SaveListener(ShortcutEntryListWidget parent, boolean name) {
            this.parent = parent;
            this.name = name;
        }

        @Override
        public boolean onTextChange(GuiTextFieldGeneric textField) {
            if (name) {
                parent.entry.setName(textField.getText());
            } else {
                parent.entry.setReplace(textField.getText());
            }
            return false;
        }
    }

    protected int addButton(int x, int y, String translation, IButtonActionListener listener) {
        ButtonGeneric button = new NamedSimpleButton(x, y, StringUtils.translate(translation), false);
        this.addButton(button, listener);

        return button.getWidth() + 1;
    }

    @Override
    public void renderEntry(int mouseX, int mouseY, boolean selected, MatrixStack matrixStack) {}
}
