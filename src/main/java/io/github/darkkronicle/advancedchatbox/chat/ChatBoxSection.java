/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatbox.chat;

import io.github.darkkronicle.advancedchatbox.config.ChatBoxConfigStorage;
import io.github.darkkronicle.advancedchatcore.chat.AdvancedChatScreen;
import io.github.darkkronicle.advancedchatcore.interfaces.AdvancedChatScreenSection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class ChatBoxSection extends AdvancedChatScreenSection {

    private ChatSuggestorGui suggestor;

    public ChatBoxSection(AdvancedChatScreen screen) {
        super(screen);
    }

    @Override
    public void onChatFieldUpdate(String chatText, String text) {
        this.suggestor.setWindowActive(!text.equals(getScreen().getOriginalChatText()));
        this.suggestor.refresh();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.suggestor.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.suggestor.render(matrixStack, mouseX, mouseY);
    }

    @Override
    public void setChatFromHistory(String hist) {
        this.suggestor.setWindowActive(false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return this.suggestor.mouseScrolled(amount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.suggestor.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void resize(int width, int height) {
        this.suggestor.refresh();
    }

    @Override
    public void initGui() {
        MinecraftClient client = MinecraftClient.getInstance();
        AdvancedChatScreen screen = getScreen();
        this.suggestor =
                new ChatSuggestorGui(
                        client,
                        screen,
                        screen.getChatField(),
                        client.textRenderer,
                        false,
                        false,
                        1,
                        ChatBoxConfigStorage.General.SUGGESTION_SIZE.config.getIntegerValue(),
                        true);
        this.suggestor.refresh();
    }
}
