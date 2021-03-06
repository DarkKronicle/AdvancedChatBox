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
import io.github.darkkronicle.advancedchatcore.util.FindType;
import io.github.darkkronicle.advancedchatcore.util.FluidText;
import io.github.darkkronicle.advancedchatcore.util.SearchResult;
import io.github.darkkronicle.advancedchatcore.util.StringMatch;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class ColorCodeFormatter implements IMessageFormatter {
    @Override
    public Optional<FluidText> format(FluidText text, @Nullable ParseResults<CommandSource> parse) {
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
        FluidText formatted = new FluidText();
        for (StringMatch match : search.getMatches()) {
            formatted.append(text.truncate(new StringMatch("", index, match.start)).fillStyle(last));
            Formatting format = Formatting.byCode(match.match.charAt(1));
            last = last.withFormatting(format);
            index = match.start;
        }
        FluidText small = text.truncate(new StringMatch("", index, string.length()));
        if (small != null && !small.getString().isEmpty()) {
            formatted.append(small.fillStyle(last));
        }
        return Optional.of(formatted);
    }
}
