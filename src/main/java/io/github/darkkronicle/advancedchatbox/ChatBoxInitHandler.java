/*
 * Copyright (C) 2021-2022 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatbox;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import io.github.darkkronicle.advancedchatbox.chat.ChatBoxSection;
import io.github.darkkronicle.advancedchatbox.config.ChatBoxConfigStorage;
import io.github.darkkronicle.advancedchatbox.config.GuiChatBoxConfig;
import io.github.darkkronicle.advancedchatbox.formatter.ColorCodeFormatter;
import io.github.darkkronicle.advancedchatbox.formatter.CommandColorer;
import io.github.darkkronicle.advancedchatbox.formatter.JSONFormatter;
import io.github.darkkronicle.advancedchatbox.registry.ChatFormatterRegistry;
import io.github.darkkronicle.advancedchatbox.registry.ChatSuggestorRegistry;
import io.github.darkkronicle.advancedchatbox.suggester.CalculatorSuggestor;
import io.github.darkkronicle.advancedchatbox.suggester.PlayerSuggestor;
import io.github.darkkronicle.advancedchatbox.suggester.ShortcutSuggestor;
import io.github.darkkronicle.advancedchatbox.suggester.SpellCheckSuggestor;
import io.github.darkkronicle.advancedchatcore.AdvancedChatCore;
import io.github.darkkronicle.advancedchatcore.chat.ChatScreenSectionHolder;
import io.github.darkkronicle.advancedchatcore.config.gui.GuiConfigHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

@Environment(EnvType.CLIENT)
public class ChatBoxInitHandler implements IInitializationHandler {
    @Override
    public void registerModHandlers() {
        ChatBoxConfigStorage.getInstance().load(); 
        ConfigManager.getInstance().registerConfigHandler(AdvancedChatBox.MOD_ID, new ChatBoxConfigStorage());
        GuiConfigHandler.getInstance().addTab(GuiConfigHandler.children("box", "advancedchat.config.tab.advancedchatbox",
                GuiConfigHandler.wrapScreen("box_general", "advancedchatbox.config.tab.general",
                        parent -> new GuiChatBoxConfig()
                ),
                GuiConfigHandler.wrapSaveableOptions(
                        "spellchecker",
                        "advancedchatbox.config.tab.spellchecker",
                        ChatBoxConfigStorage.SpellChecker.OPTIONS
                )
        ));


        ChatFormatterRegistry chatRegistry = ChatFormatterRegistry.getInstance();
        chatRegistry.register(CommandColorer::getInstance, "commandcolorer",
                "advancedchatbox.config.chatformatter.commandcolorer",
                "advancedchatbox.config.chatformatter.info.commandcolorer", true, true);
        chatRegistry.register(JSONFormatter::new, "jsonformatter", "advancedchatbox.config.chatformatter.jsonformatter",
                "advancedchatbox.config.chatformatter.info.jsonformatter", true, false);
        chatRegistry.register(ColorCodeFormatter::new, "colorcodeformatter",
                "advancedchatbox.config.chatformatter.colorcodeformatter",
                "advancedchatbox.config.chatformatter.info.colorcodeformatter", true, false);

        // Initiate chat suggestors
        ChatSuggestorRegistry suggestorRegistry = ChatSuggestorRegistry.getInstance();
        suggestorRegistry.register(PlayerSuggestor::new, "players", "advancedchatbox.config.chatsuggestor.players",
                "advancedchatbox.config.chatsuggestor.info.players", true, false);
        suggestorRegistry.register(CalculatorSuggestor::new, "calculator",
                "advancedchatbox.config.chatsuggestor.calculator",
                "advancedchatbox.config.chatsuggestor.info.calculator", true, false);
        suggestorRegistry.register(ShortcutSuggestor::new, "shortcut", "advancedchatbox.config.chatsuggestor.shortcut",
                "advancedchatbox.config.chatsuggestor.info.shortcut", true, false);
        try {
            suggestorRegistry.register(SpellCheckSuggestor::getInstance, "spellcheck",
                    "advancedchatbox.config.chatsuggestor.spellcheck",
                    "advancedchatbox.config.chatsuggestor.info.spellcheck", true, false);
            SpellCheckSuggestor.getInstance().setup();                   
        } catch (Exception e) {
            LogManager.getLogger().log(Level.ERROR, "[AdvancedChat] {}", "Couldn't load SpellCheckSuggestor", e);
        }

        AdvancedChatCore.CREATE_SUGGESTOR = false;
        ChatScreenSectionHolder.getInstance().addSectionSupplier(ChatBoxSection::new);
    }
}
