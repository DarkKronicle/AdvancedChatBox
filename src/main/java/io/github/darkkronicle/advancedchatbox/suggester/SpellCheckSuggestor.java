/*
 * Copyright (C) 2021-2022 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatbox.suggester;

import com.mojang.brigadier.context.StringRange;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatbox.chat.AdvancedSuggestion;
import io.github.darkkronicle.advancedchatbox.chat.AdvancedSuggestions;
import io.github.darkkronicle.advancedchatbox.config.ChatBoxConfigStorage;
import io.github.darkkronicle.advancedchatbox.interfaces.IMessageSuggestor;
import io.github.darkkronicle.advancedchatcore.util.FindType;
import io.github.darkkronicle.advancedchatcore.util.SearchUtils;
import io.github.darkkronicle.advancedchatcore.util.StringMatch;
import io.github.darkkronicle.advancedchatcore.util.StyleFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.languagetool.JLanguageTool;
import org.languagetool.ResultCache;
import org.languagetool.UserConfig;
import org.languagetool.language.*;
import org.languagetool.rules.RuleMatch;

@Environment(EnvType.CLIENT)
public class SpellCheckSuggestor implements IMessageSuggestor {

    private static SpellCheckSuggestor.Language previousLanguage = null;

    private JLanguageTool language;

    private final static SpellCheckSuggestor INSTANCE = new SpellCheckSuggestor();

    public static SpellCheckSuggestor getInstance() {
        return INSTANCE;
    }

    public static UserConfig generateConfig() {
        return new UserConfig(new ArrayList<>(), new HashMap<>(), 20, null, null, null, null);
    }

    private SpellCheckSuggestor() { }

    public void setup() {
        language = new JLanguageTool(
                ((Language) ChatBoxConfigStorage.SpellChecker.SPELL_LANGUAGE.config.getOptionListValue()).getLanguageSupplier().get(),
                null,
                new ResultCache(15),
                generateConfig()
        );

        language.setMaxErrorsPerWordRate(0.33f);
        try {
            // Set it up. Make it so it doesn't freeze later.
            language.check("a");
        } catch (IOException e) {
            e.printStackTrace();
        }
        previousLanguage = (Language) ChatBoxConfigStorage.SpellChecker.SPELL_LANGUAGE.config.getOptionListValue();
    }

    public void checkDifferent() {
        if (ChatBoxConfigStorage.SpellChecker.SPELL_LANGUAGE.config.getOptionListValue() != previousLanguage) {
            setup();
        }
    }

    @Override
    public Optional<List<AdvancedSuggestions>> suggest(String text) {
        ArrayList<AdvancedSuggestions> suggestions = new ArrayList<>();
        try {
            List<RuleMatch> matches = language.check(text);
            for (RuleMatch match : matches) {
                int fromPos = match.getFromPos();
                int toPos = match.getToPos();
                StringRange range = new StringRange(fromPos, toPos);
                suggestions.add(new AdvancedSuggestions(range, convertSuggestions(match, range)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
        return Optional.of(suggestions);
    }

    private static List<AdvancedSuggestion> convertSuggestions(RuleMatch match, StringRange range) {
        List<AdvancedSuggestion> replacements = new ArrayList<>();
        for (String s : match.getSuggestedReplacements()) {
            replacements
                    .add(new AdvancedSuggestion(range, s, Text.literal(s), getHover(match.getMessage())));
        }
        return replacements;
    }

    private static Text getHover(String message) {
        String text = ChatBoxConfigStorage.SpellChecker.HOVER_TEXT.config.getStringValue();
        text = text.replaceAll("&", "§");
        Optional<StringMatch> match = SearchUtils.getMatch(message, "<suggestion>(.+)</suggestion>", FindType.REGEX);
        if (match.isEmpty()) {
            text = text.replaceAll("\\$1", message).replaceAll("\\$2", "").replaceAll("\\$3", "");
            return StyleFormatter.formatText(Text.literal(text));
        }
        StringMatch stringMatch = match.get();
        String start = message.substring(0, stringMatch.start);
        String end = message.substring(stringMatch.end);
        String middle = message.substring(stringMatch.start + 12, stringMatch.end - 13);
        text = text.replaceAll("\\$1", start).replaceAll("\\$2", middle).replaceAll("\\$3", end);
        return StyleFormatter.formatText(Text.literal(text));
    }

    @AllArgsConstructor
    public enum Language implements IConfigOptionListEntry {
        AMERICAN("american", AmericanEnglish::new),
        BRITISH("british", BritishEnglish::new),
        GERMAN("german", GermanyGerman::new),
        RUSSIAN("russian", Russian::new),
        CHINESE("chinese", Chinese::new),
        PORTUGUESE("portuguese", PortugalPortuguese::new),
        FRENCH("french", French::new),
        DUTCH("dutch", Dutch::new),
        POLISH("polish", Polish::new),
        SPANISH("spanish", Spanish::new),
        ITALIAN("italian", Italian::new),
        UKRAINIAN("ukrainian", Ukrainian::new),
        JAPANESE("japanese", Japanese::new),
        ;

        @Getter
        private final String language;
        @Getter
        private final Supplier<org.languagetool.Language> languageSupplier;

        @Override
        public String getStringValue() {
            return language;
        }

        @Override
        public String getDisplayName() {
            return StringUtils.translate("advancedchatbox.config.language." + language);
        }

        @Override
        public Language cycle(boolean forward) {
            int i = ordinal();
            return values()[(i + (forward ? 1 : -1)) % values().length];
        }

        @Override
        public Language fromString(String value) {
            for (Language language : Language.values()) {
                if (language.language.equals(value)) {
                    return language;
                }
            }
            return null;
        }
    }
}
