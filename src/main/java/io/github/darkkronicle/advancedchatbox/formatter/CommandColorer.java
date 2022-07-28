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
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.StringRange;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import io.github.darkkronicle.advancedchatbox.AdvancedChatBox;
import io.github.darkkronicle.advancedchatbox.config.CommandColorerStorage;
import io.github.darkkronicle.advancedchatbox.interfaces.IMessageFormatter;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchatcore.config.SaveableConfig;
import io.github.darkkronicle.advancedchatcore.gui.buttons.BackButtonListener;
import io.github.darkkronicle.advancedchatcore.gui.buttons.Buttons;
import io.github.darkkronicle.advancedchatcore.interfaces.IClosable;
import io.github.darkkronicle.advancedchatcore.interfaces.IJsonApplier;
import io.github.darkkronicle.advancedchatcore.interfaces.IScreenSupplier;
import io.github.darkkronicle.advancedchatcore.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class CommandColorer implements IMessageFormatter, IJsonApplier, IScreenSupplier {

    private static final CommandColorer INSTANCE = new CommandColorer();

    public static CommandColorer getInstance() {
        return INSTANCE;
    }

    private CommandColorer() {

    }

    @Override
    public Optional<Text> format(Text text, @Nullable ParseResults<CommandSource> parse) {
        if (parse == null) {
            if (text.getString().charAt(0) == '/') {
                return Optional.of(Text.literal(text.getString()).fillStyle(Style.EMPTY.withColor(CommandColorerStorage.ERROR_COLOR.config.get().color())));
            }
            return Optional.empty();
        }
        CommandContextBuilder<CommandSource> commandContextBuilder = parse.getContext().getLastChild();
        HashMap<StringMatch, StringInsert> replace = new HashMap<>();
        int lowest = -1;
        int max = 0;
        int index = 0;
        String string = text.getString();
        int length = string.length();
        Colors.Palette palette = Colors.getInstance().get(CommandColorerStorage.DEFAULT_PALETTE.config.getStringValue())
                .orElse(Colors.getInstance().getDefault());

        TreeSet<CommandSection<?>> sections = new TreeSet<>(compileObjects(parse, string));
        // Arguments
        for (CommandSection<?> section : sections) {
            int start = section.getMatch().start;
            int end = Math.min(section.getMatch().end, length);
            StringMatch match = new StringMatch(string.subSequence(start, end).toString(), start, end);
            if (lowest == -1 || start < lowest) {
                lowest = start;
            }
            if (end > max) {
                max = end;
            }
            Color color = palette.getColors().get(index % palette.getColors().size());
            replace.put(match, (current, match1) -> {
                if (current.getStyle().equals(Style.EMPTY)) {
                    return Text.literal(match1.match).fillStyle(Style.EMPTY.withColor(color.color()));
                }
                return Text.literal(match1.match).fillStyle(current.getStyle());
            });
            index += 1;
        }

        if (lowest > -1) {
            replace.put(new StringMatch(text.getString().substring(0, lowest), 0, lowest), (current, match) -> {
                if (current.getStyle().equals(Style.EMPTY)) {
                    return Text.literal(match.match).fillStyle(Style.EMPTY.withColor(CommandColorerStorage.COMMAND_COLOR.config.get().color()));
                }
                return Text.literal(match.match).fillStyle(current.getStyle());
            });
        }
        if (max != string.length()) {
            replace.put(new StringMatch(text.getString().substring(max, string.length()), max, string.length()),
                    (current, match) -> {
                        if (current.getStyle().equals(Style.EMPTY)) {
                            return Text.literal(match.match).fillStyle(Style.EMPTY.withColor(CommandColorerStorage.ERROR_COLOR.config.get().color()));
                        }
                        return Text.literal(match.match).fillStyle(current.getStyle());
                    });
        }

        text = TextUtil.replaceStrings(text, replace);
        return Optional.of(text);
    }

    private List<CommandSection<?>> compileObjects(ParseResults<CommandSource> parse, String input) {
        CommandContextBuilder<CommandSource> commandContextBuilder = parse.getContext();
        List<CommandSection<?>> sections = new ArrayList<>();
        for (CommandContextBuilder<CommandSource> child : getAllChildren(commandContextBuilder)) {
            sections.addAll(addSubs(child, input));
            sections.addAll(addArgs(child, input));
        }
        return sections;
    }

    private List<CommandSection<ParsedCommandNode<CommandSource>>> addSubs(CommandContextBuilder<CommandSource> context,
            String input) {
        List<CommandSection<ParsedCommandNode<CommandSource>>> nodes = new ArrayList<>();
        for (ParsedCommandNode<CommandSource> node : context.getNodes()) {
            nodes.add(new CommandSection<>(node, fromRange(node.getRange(), input), CommandSection.Section.COMMAND));
        }
        return nodes;
    }

    private List<CommandSection<ParsedArgument<CommandSource, ?>>> addArgs(CommandContextBuilder<CommandSource> context,
            String input) {
        List<CommandSection<ParsedArgument<CommandSource, ?>>> nodes = new ArrayList<>();
        if (context.getArguments() == null) {
            return nodes;
        }
        for (ParsedArgument<CommandSource, ?> node : context.getArguments().values()) {
            nodes.add(new CommandSection<>(node, fromRange(node.getRange(), input), CommandSection.Section.ARGUMENT));
        }
        return nodes;
    }

    private List<CommandContextBuilder<CommandSource>> getAllChildren(CommandContextBuilder<CommandSource> context) {
        List<CommandContextBuilder<CommandSource>> children = new ArrayList<>();
        while (context != null) {
            children.add(context);
            context = context.getChild();
        }
        return children;
    }

    private static StringMatch fromRange(StringRange range, String input) {
        // Clamp to the beginning/end. Implemented by Kale Ko
        if (range.getStart() < 0) {
            range = new StringRange(0, range.getEnd());
        }
        if (range.getEnd() >= input.length()) {
            range = new StringRange(range.getStart(), input.length());
        }
        return new StringMatch(range.get(input), range.getStart(), range.getEnd());
    }

    @Override
    public JsonObject save() {
        JsonObject obj = new JsonObject();
        // Compiler is weird and casting is required
        ConfigStorage.writeOptions(obj, CommandColorerStorage.NAME,
                (List<SaveableConfig<?>>) CommandColorerStorage.OPTIONS);
        return obj;
    }

    @Override
    public void load(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }
        JsonObject obj = element.getAsJsonObject();
        // Compiler is weird and casting is required
        ConfigStorage.readOptions(obj, CommandColorerStorage.NAME,
                (List<SaveableConfig<?>>) CommandColorerStorage.OPTIONS);
    }

    @Override
    public Supplier<Screen> getScreen(@Nullable Screen parent) {
        return () -> new CommandColorerScreen(this, parent);
    }

    public static class CommandColorerScreen extends GuiConfigsBase implements IClosable {

        private final CommandColorer parent;

        public CommandColorerScreen(CommandColorer parent, Screen parentScreen) {
            super(10, 66, AdvancedChatBox.MOD_ID, parentScreen, "advancedchatbox.config.chatformatter.commandcolorer");
            this.parent = parent;
            setParent(parentScreen);
        }

        @Override
        public void initGui() {
            super.initGui();
            this.addButton(Buttons.BACK.createButton(2, 26), new BackButtonListener(this));
        }

        @Override
        public List<ConfigOptionWrapper> getConfigs() {
            List<IConfigBase> options = new ArrayList<>();
            for (SaveableConfig<? extends IConfigBase> saveable : CommandColorerStorage.OPTIONS) {
                options.add(saveable.config);
            }
            return ConfigOptionWrapper.createFor(options);
        }

        @Override
        public void close(ButtonBase button) {
            closeGui(true);
        }
    }
}
