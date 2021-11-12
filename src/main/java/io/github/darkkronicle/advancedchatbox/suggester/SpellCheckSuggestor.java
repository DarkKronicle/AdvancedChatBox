/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatbox.suggester;

import com.mojang.brigadier.context.StringRange;
import io.github.darkkronicle.advancedchatbox.chat.AdvancedSuggestion;
import io.github.darkkronicle.advancedchatbox.chat.AdvancedSuggestions;
import io.github.darkkronicle.advancedchatbox.config.ChatBoxConfigStorage;
import io.github.darkkronicle.advancedchatbox.interfaces.IMessageSuggestor;
import io.github.darkkronicle.advancedchatcore.util.FindType;
import io.github.darkkronicle.advancedchatcore.util.FluidText;
import io.github.darkkronicle.advancedchatcore.util.RawText;
import io.github.darkkronicle.advancedchatcore.util.SearchUtils;
import io.github.darkkronicle.advancedchatcore.util.StringMatch;
import io.github.darkkronicle.advancedchatcore.util.StyleFormatter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.languagetool.JLanguageTool;
import org.languagetool.ResultCache;
import org.languagetool.UserConfig;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

@Environment(EnvType.CLIENT)
public class SpellCheckSuggestor implements IMessageSuggestor {
    private final JLanguageTool lt;

    private static final SpellCheckSuggestor INSTANCE = new SpellCheckSuggestor();

    public static SpellCheckSuggestor getInstance() {
        return INSTANCE;
    }

    private SpellCheckSuggestor() {
        lt = new JLanguageTool(new AmericanEnglish(), new AmericanEnglish(), new ResultCache(15),
                new UserConfig(new ArrayList<>(), new HashMap<>(), 20));
        lt.setMaxErrorsPerWordRate(0.33f);
        try {
            // Set it up. Make it so it doesn't freeze later.
            lt.check("a");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<List<AdvancedSuggestions>> suggest(String text) {
        ArrayList<AdvancedSuggestions> suggestions = new ArrayList<>();
        try {
            List<RuleMatch> matches = lt.check(text);
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
                    .add(new AdvancedSuggestion(range, s, new RawText(s, Style.EMPTY), getHover(match.getMessage())));
        }
        return replacements;
    }

    private static Text getHover(String message) {
        String text = ChatBoxConfigStorage.SpellChecker.HOVER_TEXT.config.getStringValue();
        text = text.replaceAll("&", "§");
        Optional<StringMatch> match = SearchUtils.getMatch(message, "<suggestion>(.+)</suggestion>", FindType.REGEX);
        if (match.isEmpty()) {
            text = text.replaceAll("\\$1", message).replaceAll("\\$2", "").replaceAll("\\$3", "");
            return StyleFormatter.formatText(new FluidText(new RawText(text, Style.EMPTY)));
        }
        StringMatch stringMatch = match.get();
        String start = message.substring(0, stringMatch.start);
        String end = message.substring(stringMatch.end);
        String middle = message.substring(stringMatch.start + 12, stringMatch.end - 13);
        text = text.replaceAll("\\$1", start).replaceAll("\\$2", middle).replaceAll("\\$3", end);
        return StyleFormatter.formatText(new FluidText(new RawText(text, Style.EMPTY)));
    }
}
