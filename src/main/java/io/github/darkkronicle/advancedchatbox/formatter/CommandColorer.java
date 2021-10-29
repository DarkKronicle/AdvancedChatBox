/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatbox.formatter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import io.github.darkkronicle.advancedchatbox.interfaces.IMessageFormatter;
import io.github.darkkronicle.advancedchatcore.interfaces.IJsonApplier;
import io.github.darkkronicle.advancedchatcore.interfaces.IScreenSupplier;
import io.github.darkkronicle.advancedchatcore.util.ColorUtil;
import io.github.darkkronicle.advancedchatcore.util.FluidText;
import io.github.darkkronicle.advancedchatcore.util.RawText;
import io.github.darkkronicle.advancedchatcore.util.StringMatch;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Style;

@Environment(EnvType.CLIENT)
public class CommandColorer implements IMessageFormatter, IJsonApplier, IScreenSupplier {

    private static final ColorUtil.SimpleColor INFO = new ColorUtil.SimpleColor(180, 180, 180, 255);

    private List<ColorUtil.SimpleColor> colors =
            Arrays.asList(
                    new ColorUtil.SimpleColor(160, 172, 219, 255),
                    new ColorUtil.SimpleColor(156, 214, 162, 255),
                    new ColorUtil.SimpleColor(129, 110, 224, 255));

    @Override
    public Optional<FluidText> format(FluidText text, @Nullable ParseResults<CommandSource> parse) {
        if (parse == null) {
            return Optional.empty();
        }
        CommandContextBuilder<CommandSource> commandContextBuilder =
                parse.getContext().getLastChild();
        HashMap<StringMatch, FluidText.StringInsert> replace = new HashMap<>();
        int color = -1;
        int lowest = -1;
        String string = text.getString();
        int length = string.length();
        for (ParsedArgument<CommandSource, ?> commandSourceParsedArgument :
                commandContextBuilder.getArguments().values()) {
            int start = commandSourceParsedArgument.getRange().getStart();
            int end = Math.min(commandSourceParsedArgument.getRange().getEnd(), length);
            StringMatch match =
                    new StringMatch(string.subSequence(start, end).toString(), start, end);
            if (lowest == -1 || start < lowest) {
                lowest = start;
            }
            color++;
            if (color >= colors.size()) {
                color = 0;
            }
            final int thisCol = color;
            replace.put(
                    match,
                    (current, match1) -> {
                        if (current.getStyle().equals(Style.EMPTY)) {
                            return new FluidText(
                                    RawText.withColor(match1.match, colors.get(thisCol)));
                        }
                        return new FluidText(new RawText(match1.match, current.getStyle()));
                    });
        }
        if (lowest == -1) {
            lowest = text.getString().length();
        }
        replace.put(
                new StringMatch(text.getString().substring(0, lowest), 0, lowest),
                (current, match) -> {
                    if (current.getStyle().equals(Style.EMPTY)) {
                        return new FluidText(RawText.withColor(match.match, INFO));
                    }
                    return new FluidText(new RawText(match.match, current.getStyle()));
                });
        text.replaceStrings(replace);
        return Optional.of(text);
    }

    @Override
    public JsonObject save() {
        return null;
    }

    @Override
    public void load(JsonElement element) {}

    @Override
    public Supplier<Screen> getScreen(@Nullable Screen parent) {
        return null;
    }
}
