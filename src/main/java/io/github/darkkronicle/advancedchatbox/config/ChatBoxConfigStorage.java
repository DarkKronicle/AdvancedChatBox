/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatbox.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatbox.AdvancedChatBox;
import io.github.darkkronicle.advancedchatbox.registry.ChatFormatterRegistry;
import io.github.darkkronicle.advancedchatbox.registry.ChatSuggestorRegistry;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchatcore.config.SaveableConfig;
import io.github.darkkronicle.advancedchatcore.config.options.ConfigColor;
import io.github.darkkronicle.advancedchatcore.util.Color;
import java.io.File;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ChatBoxConfigStorage implements IConfigHandler {
    public static final String CONFIG_FILE_NAME = AdvancedChatBox.MOD_ID + ".json";
    private static final int CONFIG_VERSION = 1;

    public static class General {
        public static final String NAME = "general";

        public static String translate(String key) {
            return StringUtils.translate("advancedchatbox.config.general." + key);
        }

        public static final SaveableConfig<ConfigColor> HIGHLIGHT_COLOR =
                SaveableConfig.fromConfig("highlightColor", new ConfigColor(translate("highlightcolor"),
                        new Color(255, 255, 0, 255), translate("info.highlightcolor")));
        public static final SaveableConfig<ConfigColor> UNHIGHLIGHT_COLOR =
                SaveableConfig.fromConfig("unhighlightColor", new ConfigColor(translate("unhighlightcolor"),
                        new Color(170, 170, 170, 255), translate("info.unhighlightcolor")));
        public static final SaveableConfig<ConfigColor> BACKGROUND_COLOR =
                SaveableConfig.fromConfig("backgroundColor", new ConfigColor(translate("backgroundcolor"),
                        new Color(0, 0, 0, 170), translate("info.backgroundcolor")));
        public static final SaveableConfig<ConfigInteger> SUGGESTION_SIZE = SaveableConfig.fromConfig("suggestionSize",
                new ConfigInteger(translate("suggestionsize"), 10, 1, 50, translate("info.suggestionsize")));
        public static final SaveableConfig<ConfigBoolean> REMOVE_IDENTIFIER =
                SaveableConfig.fromConfig("removeIdentifier",
                        new ConfigBoolean(translate("removeidentifier"), true, translate("info.removeidentifier")));
        public static final SaveableConfig<ConfigBoolean> PRUNE_PLAYER_SUGGESTIONS = SaveableConfig.fromConfig(
                "prunePlayerSuggestions",
                new ConfigBoolean(translate("pruneplayersuggestions"), true, translate("info.pruneplayersuggestions")));
        public static final SaveableConfig<ConfigColor> AVAILABLE_SUGGESTION_COLOR = SaveableConfig
                .fromConfig("availableSuggestionColor", new ConfigColor(translate("availablesuggestioncolor"),
                        new Color(150, 150, 150, 255), translate("info.availablesuggestioncolor")));

        public static final ImmutableList<SaveableConfig<? extends IConfigBase>> OPTIONS =
                ImmutableList.of(HIGHLIGHT_COLOR, UNHIGHLIGHT_COLOR, BACKGROUND_COLOR, SUGGESTION_SIZE,
                        REMOVE_IDENTIFIER, PRUNE_PLAYER_SUGGESTIONS, AVAILABLE_SUGGESTION_COLOR);
    }

    public static class SpellChecker {
        public static final String NAME = "spellchecker";

        public static String translate(String key) {
            return StringUtils.translate("advancedchatbox.config.spellchecker." + key);
        }

        public static final SaveableConfig<ConfigString> HOVER_TEXT = SaveableConfig.fromConfig("hoverText",
                new ConfigString(translate("hovertext"), "&7$1&b$2&7$3", translate("info.hovertext")));

        // public static final SaveableConfig<ConfigBoolean>
        // SUGGEST_CAPITAL =
        // SaveableConfig.fromConfig(
        // "suggest_capital",
        // new ConfigBoolean(
        // translate("suggestcapital"),
        // true,
        // translate("info.suggestcapital")
        // )
        // );

        public static final ImmutableList<SaveableConfig<? extends IConfigBase>> OPTIONS = ImmutableList.of(HOVER_TEXT
        // SUGGEST_CAPITAL
        );
    }

    public static void loadFromFile() {
        File configFile =
                FileUtils.getConfigDirectory().toPath().resolve("advancedchat").resolve(CONFIG_FILE_NAME).toFile();

        if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
            JsonElement element = ConfigStorage.parseJsonFile(configFile);

            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();

                ConfigStorage.readOptions(root, General.NAME, (List<SaveableConfig<?>>) General.OPTIONS);
                ConfigStorage.readOptions(root, SpellChecker.NAME, (List<SaveableConfig<?>>) SpellChecker.OPTIONS);

                ConfigStorage.applyRegistry(root.get(ChatFormatterRegistry.NAME), ChatFormatterRegistry.getInstance());
                ConfigStorage.applyRegistry(root.get(ChatSuggestorRegistry.NAME), ChatSuggestorRegistry.getInstance());

                int version = JsonUtils.getIntegerOrDefault(root, "configVersion", 0);
            }
        }
    }

    public static void saveFromFile() {
        File dir = FileUtils.getConfigDirectory().toPath().resolve("advancedchat").toFile();

        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs()) {
            JsonObject root = new JsonObject();

            ConfigStorage.writeOptions(root, General.NAME, (List<SaveableConfig<?>>) General.OPTIONS);
            ConfigStorage.writeOptions(root, SpellChecker.NAME, (List<SaveableConfig<?>>) SpellChecker.OPTIONS);

            root.add("config_version", new JsonPrimitive(CONFIG_VERSION));

            root.add(ChatFormatterRegistry.NAME, ConfigStorage.saveRegistry(ChatFormatterRegistry.getInstance()));
            root.add(ChatSuggestorRegistry.NAME, ConfigStorage.saveRegistry(ChatSuggestorRegistry.getInstance()));

            ConfigStorage.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));
        }
    }

    @Override
    public void load() {
        loadFromFile();
    }

    @Override
    public void save() {
        saveFromFile();
    }
}
