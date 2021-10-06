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
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchatcore.config.options.ConfigSimpleColor;
import io.github.darkkronicle.advancedchatcore.util.ColorUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.File;
import java.util.List;


@Environment(EnvType.CLIENT)
public class ChatBoxConfigStorage implements IConfigHandler {

    public static final String CONFIG_FILE_NAME = AdvancedChatBox.MOD_ID + ".json";
    private static final int CONFIG_VERSION = 1;

    public static class General {

        public static final String NAME = "general";

        public static String translate(String key) {
            return StringUtils.translate("advancedchatbox.config.general." + key);
        }

        public final static ConfigStorage.SaveableConfig<ConfigSimpleColor> HIGHLIGHT_COLOR = ConfigStorage.SaveableConfig.fromConfig("highlightColor",
                new ConfigSimpleColor(translate("highlightcolor"), new ColorUtil.SimpleColor(255, 255, 0, 255), translate("info.highlightcolor")));
        public final static ConfigStorage.SaveableConfig<ConfigSimpleColor> UNHIGHLIGHT_COLOR = ConfigStorage.SaveableConfig.fromConfig("unhighlightColor",
                new ConfigSimpleColor(translate("unhighlightcolor"), new ColorUtil.SimpleColor(170, 170, 170, 255), translate("info.unhighlightcolor")));
        public final static ConfigStorage.SaveableConfig<ConfigSimpleColor> BACKGROUND_COLOR = ConfigStorage.SaveableConfig.fromConfig("backgroundColor",
                new ConfigSimpleColor(translate("backgroundcolor"), new ColorUtil.SimpleColor(0, 0, 0, 170), translate("info.backgroundcolor")));
        public final static ConfigStorage.SaveableConfig<ConfigInteger> SUGGESTION_SIZE = ConfigStorage.SaveableConfig.fromConfig("suggestionSize",
                new ConfigInteger(translate("suggestionsize"), 10, 1, 50, translate("info.suggestionsize")));
        public final static ConfigStorage.SaveableConfig<ConfigBoolean> REMOVE_IDENTIFIER = ConfigStorage.SaveableConfig.fromConfig("removeIdentifier",
                new ConfigBoolean(translate("removeidentifier"), true, translate("info.removeidentifier")));
        public final static ConfigStorage.SaveableConfig<ConfigBoolean> PRUNE_PLAYER_SUGGESTIONS = ConfigStorage.SaveableConfig.fromConfig("prunePlayerSuggestions",
                new ConfigBoolean(translate("pruneplayersuggestions"), true, translate("info.pruneplayersuggestions")));
        public final static ConfigStorage.SaveableConfig<ConfigSimpleColor> AVAILABLE_SUGGESTION_COLOR = ConfigStorage.SaveableConfig.fromConfig("availableSuggestionColor",
                new ConfigSimpleColor(translate("availablesuggestioncolor"), new ColorUtil.SimpleColor(150, 150, 150, 255), translate("info.availablesuggestioncolor")));


        public final static ImmutableList<ConfigStorage.SaveableConfig<? extends IConfigBase>> OPTIONS = ImmutableList.of(
                HIGHLIGHT_COLOR,
                UNHIGHLIGHT_COLOR,
                BACKGROUND_COLOR,
                SUGGESTION_SIZE,
                REMOVE_IDENTIFIER,
                PRUNE_PLAYER_SUGGESTIONS,
                AVAILABLE_SUGGESTION_COLOR
        );

    }

    public static class SpellChecker {

        public static final String NAME = "spellchecker";

        public static String translate(String key) {
            return StringUtils.translate("advancedchatbox.config.spellchecker." + key);
        }

        public final static ConfigStorage.SaveableConfig<ConfigString> HOVER_TEXT = ConfigStorage.SaveableConfig.fromConfig("hoverText",
                new ConfigString(translate("hovertext"), "&8$1&b$2&8$3", translate("info.hovertext")));

        public final static ConfigStorage.SaveableConfig<ConfigBoolean> SUGGEST_CAPITAL = ConfigStorage.SaveableConfig.fromConfig("suggest_capital",
                new ConfigBoolean(translate("suggestcapital"), true, translate("info.suggestcapital")));

        public final static ImmutableList<ConfigStorage.SaveableConfig<? extends IConfigBase>> OPTIONS = ImmutableList.of(
                HOVER_TEXT,
                SUGGEST_CAPITAL
        );

    }

    public static void loadFromFile() {

        File configFile = FileUtils.getConfigDirectory().toPath().resolve("advancedchat").resolve(CONFIG_FILE_NAME).toFile();

        if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
            JsonElement element = ConfigStorage.parseJsonFile(configFile);

            if (element != null && element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();

                ConfigStorage.readOptions(root, General.NAME, (List<ConfigStorage.SaveableConfig<?>>) General.OPTIONS);
                ConfigStorage.readOptions(root, SpellChecker.NAME, (List<ConfigStorage.SaveableConfig<?>>) SpellChecker.OPTIONS);

                int version = JsonUtils.getIntegerOrDefault(root, "configVersion", 0);

            }
        }
    }

    public static void saveFromFile() {
        File dir = FileUtils.getConfigDirectory().toPath().resolve("advancedchat").toFile();

        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs()) {
            JsonObject root = new JsonObject();

            ConfigStorage.writeOptions(root, General.NAME, (List<ConfigStorage.SaveableConfig<?>>) General.OPTIONS);
            ConfigStorage.writeOptions(root, SpellChecker.NAME, (List<ConfigStorage.SaveableConfig<?>>) SpellChecker.OPTIONS);

            root.add("config_version", new JsonPrimitive(CONFIG_VERSION));

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
