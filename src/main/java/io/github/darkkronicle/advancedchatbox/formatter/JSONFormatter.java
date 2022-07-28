/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatbox.formatter;

import com.mojang.brigadier.ParseResults;
import io.github.darkkronicle.advancedchatbox.interfaces.IMessageFormatter;
import io.github.darkkronicle.advancedchatcore.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.command.CommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class JSONFormatter implements IMessageFormatter {
    /*
     * Released under the MIT license
     *
     * JSON Logic is from https://github.com/joeattardi/json-colorizer/blob/master/src/lib/lexer.js
     */
    public enum JSONType {
        WHITESPACE("^\\s+", new Color(255, 255, 255, 255).withAlpha(0)), BRACE("^[\\{\\}]",
                new Color(130, 130, 130, 255)), BRACKET("^[\\[\\]]", new Color(180, 180, 180, 255)), COLON("^:",
                        new Color(130, 130, 130, 255)), COMMA("^,", new Color(130, 130, 130, 255)), NUMBER_LITERAL(
                                "^-?\\d+(?:\\.\\d+)?(?:e[+-]?\\d+)?",
                                new Color(168, 97, 199, 255)), STRING_KEY("^\"(?:\\\\.|[^\"\\\\])*\"(?=\\s*:)",
                                        new Color(120, 156, 183, 255)), STRING_LITERAL("^\"(?:\\\\.|[^\"\\\\])*\"",
                                                new Color(189, 215, 222, 255)), BOOLEAN_LITERAL("^true|^false",
                                                        new Color(232, 63, 113, 255)), NULL_LITERAL("^null",
                                                                new Color(194, 76, 75, 255)), OTHER(".",
                                                                        new Color(210, 43, 43, 255));

        public final String regex;
        public final Color color;

        JSONType(String regex, Color color) {
            this.regex = regex;
            this.color = color;
        }
    }

    @Value
    @AllArgsConstructor
    public static class JSONToken {
        StringMatch match;
        JSONType type;
    }

    @Override
    public Optional<Text> format(Text text, @Nullable ParseResults<CommandSource> parse) {
        String content = text.getString();
        Optional<List<StringMatch>> omatches = SearchUtils.findMatches(content, "\\{.+\\}", FindType.REGEX);
        if (!omatches.isPresent()) {
            return Optional.empty();
        }
        List<StringMatch> matches = omatches.get();
        HashMap<StringMatch, StringInsert> replace = new HashMap<>();
        for (StringMatch m : matches) {
            replace.put(m, (current, match) -> colorJson(match.match));
        }
        text = TextUtil.replaceStrings(text, replace);
        return Optional.of(text);
    }

    public MutableText colorJson(String string) {
        TextBuilder text = new TextBuilder();
        for (JSONToken token : parseJson(string)) {
            text.append(token.match.match, Style.EMPTY.withColor(token.type.color.color()));
        }
        return text.build();
    }

    public List<JSONToken> parseJson(String string) {
        List<JSONToken> json = new ArrayList<>();
        int index = 0;
        while (string.length() > 0) {
            for (JSONType type : JSONType.values()) {
                Optional<StringMatch> omatch = SearchUtils.getMatch(string, type.regex, FindType.REGEX);
                if (!omatch.isPresent()) {
                    continue;
                }
                StringMatch match = omatch.get();
                string = string.substring(match.end);
                match.end += index;
                match.start += index;
                index += match.end - match.start;
                json.add(new JSONToken(match, type));
                break;
            }
        }
        return json;
    }
}
