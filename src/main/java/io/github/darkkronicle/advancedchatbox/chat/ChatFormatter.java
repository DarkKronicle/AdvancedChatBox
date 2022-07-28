/*
 * Copyright (C) 2021-2022 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatbox.chat;

import com.mojang.brigadier.context.StringRange;
import io.github.darkkronicle.advancedchatbox.config.ChatBoxConfigStorage;
import io.github.darkkronicle.advancedchatbox.registry.ChatFormatterRegistry;
import io.github.darkkronicle.advancedchatcore.util.RawText;
import io.github.darkkronicle.advancedchatcore.util.StringInsert;
import io.github.darkkronicle.advancedchatcore.util.StringMatch;
import java.util.HashMap;
import java.util.Optional;

import io.github.darkkronicle.advancedchatcore.util.TextUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

/**
 * A class to format the chat box on the
 */
@Environment(EnvType.CLIENT)
public class ChatFormatter {
    /** The last content that was formatted */
    private String current = null;

    /** The formatted current */
    private Text last = null;

    private final TextFieldWidget widget;
    private final ChatSuggestor suggestor;

    public ChatFormatter(TextFieldWidget widget, ChatSuggestor suggestor) {
        this.widget = widget;
        this.suggestor = suggestor;
    }

    /**
     * Format's the chat box contents
     *
     * @param string Contents
     * @return Formatted FluidText. If nothing is changed it will be the contents with Style.EMPTY
     */
    public Text format(String string) {
        Text text = Text.literal(string);
        if (string.length() == 0) {
            return text;
        }
        if (suggestor.getAllSuggestions() != null) {
            HashMap<StringMatch, StringInsert> format = new HashMap<>();
            for (AdvancedSuggestions suggestions : suggestor.getAllSuggestions()) {
                if (suggestions.getSuggestions().isEmpty()) {
                    // Don't want to format if there's nothing there...
                    continue;
                }
                boolean atLeastOne = false;
                for (AdvancedSuggestion suggestion : suggestions.getSuggestions()) {
                    if (suggestion.getText().length() > 0) {
                        atLeastOne = true;
                        break;
                    }
                }
                if (!atLeastOne) {
                    continue;
                }
                StringRange range = suggestions.getRange();
                int start = range.getStart();
                int end = range.getEnd();
                if (end > string.length()) {
                    end = string.length();
                }
                if (start < 0) {
                    start = 0;
                }
                String matchString = string.subSequence(start, end).toString();
                format.put(new StringMatch(matchString, start, end), (current, match) -> {
                    Style style = Style.EMPTY;
                    style = style.withFormatting(Formatting.UNDERLINE);
                    TextColor textColor = TextColor
                            .fromRgb(ChatBoxConfigStorage.General.AVAILABLE_SUGGESTION_COLOR.config.get().color());
                    style = style.withColor(textColor);
                    return Text.literal(matchString).fillStyle(style);
                });
            }
            text = TextUtil.replaceStrings(text, format);
        }
        for (ChatFormatterRegistry.ChatFormatterOption option : ChatFormatterRegistry.getInstance().getAll()) {
            if (!option.isActive()) {
                continue;
            }
            Optional<Text> otext = option.getOption().format(text, suggestor.getParse());
            if (otext.isPresent()) {
                text = otext.get();
            }
        }
        return text;
    }

    private OrderedText set(String s, Integer integer) {
        int length = s.length();
        if (length == 0) {
            return OrderedText.EMPTY;
        }
        if (last.getString().length() == 0) {
            return OrderedText.EMPTY;
        }
        int start = integer;
        int end = integer + length;
        int fluidLength = last.getString().length();
        if (end > fluidLength) {
            end = fluidLength;
        }
        return TextUtil.truncate(last, new StringMatch(s, start, end)).asOrderedText();
    }

    public OrderedText apply(String s, Integer integer) {
        String text = widget.getText();
        if (text.equals(current)) {
            // If the content hasn't changed, use the previous one.
            return set(s, integer);
        }
        current = text;
        last = format(text);
        return set(s, integer);
    }
}
