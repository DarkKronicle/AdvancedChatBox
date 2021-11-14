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
import io.github.darkkronicle.advancedchatbox.config.CommandColorerStorage;
import io.github.darkkronicle.advancedchatbox.interfaces.IMessageFormatter;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchatcore.config.options.ConfigSimpleColor;
import io.github.darkkronicle.advancedchatcore.interfaces.IJsonApplier;
import io.github.darkkronicle.advancedchatcore.interfaces.IScreenSupplier;
import io.github.darkkronicle.advancedchatcore.util.ColorUtil;
import io.github.darkkronicle.advancedchatcore.util.FluidText;
import io.github.darkkronicle.advancedchatcore.util.RawText;
import io.github.darkkronicle.advancedchatcore.util.StringMatch;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class CommandColorer implements IMessageFormatter, IJsonApplier, IScreenSupplier {

    private static final CommandColorer INSTANCE = new CommandColorer();
    private final HashMap<ArgumentColor, ConfigStorage.SaveableConfig<ConfigSimpleColor>> colors = new HashMap<>();

    public static CommandColorer getInstance() {
        return INSTANCE;
    }

    private CommandColorer() {
        for (ArgumentColor color : ArgumentColor.values()) {
            colors.put(color, ConfigStorage.SaveableConfig.fromConfig(color.key,
                    new ConfigSimpleColor(color.getDisplayKey(), color.getColor(), color.getInfoKey())));
        }
    }

    @Override
    public Optional<FluidText> format(FluidText text, @Nullable ParseResults<CommandSource> parse) {
        if (parse == null && text.getString().charAt(0) == '/') {
            return Optional.of(new FluidText(
                    RawText.withColor(text.getString(), CommandColorerStorage.ERROR_COLOR.config.getSimpleColor())));
        }
        CommandContextBuilder<CommandSource> commandContextBuilder = parse.getContext().getLastChild();
        HashMap<StringMatch, FluidText.StringInsert> replace = new HashMap<>();
        int lowest = -1;
        int max = 0;
        String string = text.getString();
        int length = string.length();
        for (ParsedArgument<CommandSource, ?> commandSourceParsedArgument : commandContextBuilder.getArguments()
                .values()) {
            int start = commandSourceParsedArgument.getRange().getStart();
            int end = Math.min(commandSourceParsedArgument.getRange().getEnd(), length);
            StringMatch match = new StringMatch(string.subSequence(start, end).toString(), start, end);
            if (lowest == -1 || start < lowest) {
                lowest = start;
            }
            if (end > max) {
                max = end;
            }
            ColorUtil.SimpleColor color = getColor(commandSourceParsedArgument.getResult());
            replace.put(match, (current, match1) -> {
                if (current.getStyle().equals(Style.EMPTY)) {
                    return new FluidText(RawText.withColor(match1.match, color));
                }
                return new FluidText(new RawText(match1.match, current.getStyle()));
            });
        }
        if (max == 0) {
            max = text.getString().length();
        }
        if (lowest > -1) {
            replace.put(new StringMatch(text.getString().substring(0, lowest), 0, lowest), (current, match) -> {
                if (current.getStyle().equals(Style.EMPTY)) {
                    return new FluidText(RawText.withColor(match.match,
                            CommandColorerStorage.COMMAND_COLOR.config.getSimpleColor()));
                }
                return new FluidText(new RawText(match.match, current.getStyle()));
            });
        }
        if (max != string.length()) {
            replace.put(new StringMatch(text.getString().substring(max, string.length()), max, string.length()),
                    (current, match) -> {
                        if (current.getStyle().equals(Style.EMPTY)) {
                            return new FluidText(RawText.withColor(match.match,
                                    CommandColorerStorage.ERROR_COLOR.config.getSimpleColor()));
                        }
                        return new FluidText(new RawText(match.match, current.getStyle()));
                    });
        }

        text.replaceStrings(replace);
        return Optional.of(text);
    }

    public ColorUtil.SimpleColor getColor(Object result) {
        ArgumentColor color = null;
        for (ArgumentColor c : ArgumentColor.values()) {
            if (c.clazz.isInstance(result)) {
                color = c;
                break;
            }
        }
        if (color == null) {
            return getOtherColor(result);
        }
        ConfigStorage.SaveableConfig<ConfigSimpleColor> save = colors.get(color);
        if (save == null) {
            return color.getColor();
        }
        return save.config.getSimpleColor();
    }

    public ColorUtil.SimpleColor getOtherColor(Object result) {
        return CommandColorerStorage.COMMAND_COLOR.config.getSimpleColor();
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

    public enum ArgumentColor {
        ENTITY("entity", EntitySelector.class, new ColorUtil.SimpleColor(255, 60, 60, 255)), POS("pos",
                PosArgument.class, new ColorUtil.SimpleColor(60, 255, 60, 255)), BLOCK_STATE("block_state",
                        BlockStateArgument.class, new ColorUtil.SimpleColor(60, 60, 255, 255)), BLOCK_PREDICATE(
                                "block_predicate", BlockPredicateArgumentType.BlockPredicate.class,
                                new ColorUtil.SimpleColor(60, 255, 255, 255)), ITEM_STACK("item_stack",
                                        ItemStackArgument.class,
                                        new ColorUtil.SimpleColor(255, 255, 60, 255)), ITEM_PREDICATE("item_predicate",
                                                ItemPredicateArgumentType.ItemPredicateArgument.class,
                                                new ColorUtil.SimpleColor(255, 60, 255, 255)), FORMATTING("formatting",
                                                        Formatting.class,
                                                        new ColorUtil.SimpleColor(150, 60, 255, 255)), TEXT("text",
                                                                Text.class,
                                                                new ColorUtil.SimpleColor(150, 60, 150, 255)), INTEGER(
                                                                        "integer", Integer.class,
                                                                        new ColorUtil.SimpleColor(60, 60, 150,
                                                                                255)), STRING("string", Integer.class,
                                                                                        new ColorUtil.SimpleColor(60,
                                                                                                150, 150, 255)),;

        @Getter
        private final Class<?> clazz;
        @Getter
        private final String key;
        @Getter
        private final ColorUtil.SimpleColor color;

        ArgumentColor(String key, Class<?> clazz, ColorUtil.SimpleColor color) {
            this.key = key;
            this.clazz = clazz;
            this.color = color;
        }

        public String getDisplayKey() {
            return "advancedchatbox.config.command." + key;
        }

        public String getInfoKey() {
            return "advancedchatbox.config.command.info" + key;
        }
    }
}
