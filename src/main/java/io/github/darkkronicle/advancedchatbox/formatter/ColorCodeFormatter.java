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

import java.util.Optional;
import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.command.CommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class ColorCodeFormatter implements IMessageFormatter {

    @Override
    public Optional<Text> format(Text text, @Nullable ParseResults<CommandSource> parse) {
        if (parse != null) {
            return Optional.empty();
        }
        String string = text.getString();
        if (!string.contains("&")) {
            return Optional.empty();
        }
        SearchResult search = SearchResult.searchOf(string, "(?i)&[0-9A-FK-OR]", FindType.REGEX);
        if (search.size() == 0) {
            return Optional.empty();
        }
        int index = 0;
        Style last = Style.EMPTY;
        TextBuilder formatted = new TextBuilder();
        for (StringMatch match : search.getMatches()) {
            formatted.append(TextUtil.truncate(text, new StringMatch("", index, match.start)).fillStyle(last));
            Formatting format = Formatting.byCode(match.match.charAt(1));
            last = last.withFormatting(format);
            index = match.start;
        }
        MutableText small = TextUtil.truncate(text, new StringMatch("", index, string.length()));
        if (!small.getString().isEmpty()) {
            formatted.append(small.fillStyle(last));
        }
        return Optional.of(formatted.build());
    }
}
