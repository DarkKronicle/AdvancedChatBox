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
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import io.github.darkkronicle.advancedchatbox.AdvancedChatBox;
import io.github.darkkronicle.advancedchatbox.config.CommandColorerStorage;
import io.github.darkkronicle.advancedchatbox.interfaces.IMessageFormatter;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchatcore.config.options.ConfigSimpleColor;
import io.github.darkkronicle.advancedchatcore.gui.buttons.BackButtonListener;
import io.github.darkkronicle.advancedchatcore.gui.buttons.Buttons;
import io.github.darkkronicle.advancedchatcore.interfaces.IClosable;
import io.github.darkkronicle.advancedchatcore.interfaces.IJsonApplier;
import io.github.darkkronicle.advancedchatcore.interfaces.IScreenSupplier;
import io.github.darkkronicle.advancedchatcore.util.ColorUtil;
import io.github.darkkronicle.advancedchatcore.util.FluidText;
import io.github.darkkronicle.advancedchatcore.util.RawText;
import io.github.darkkronicle.advancedchatcore.util.StringMatch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.AngleArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.OperationArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleEffect;
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
        if (parse == null) {
            if (text.getString().charAt(0) == '/') {
                return Optional.of(new FluidText(RawText.withColor(text.getString(),
                        CommandColorerStorage.ERROR_COLOR.config.getSimpleColor())));
            }
            return Optional.empty();
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
        JsonObject obj = new JsonObject();
        // Compiler is weird and casting is required
        ConfigStorage.writeOptions(obj, CommandColorerStorage.NAME,
                (List<ConfigStorage.SaveableConfig<?>>) CommandColorerStorage.OPTIONS);
        ConfigStorage.writeOptions(obj, "colors", new ArrayList<>(colors.values()));
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
                (List<ConfigStorage.SaveableConfig<?>>) CommandColorerStorage.OPTIONS);
        ConfigStorage.readOptions(obj, "colors", new ArrayList<>(colors.values()));
    }

    @Override
    public Supplier<Screen> getScreen(@Nullable Screen parent) {
        return () -> new CommandColorerScreen(this, parent);
    }

    public enum ArgumentColor {
        ENTITY("entity", EntitySelector.class, new ColorUtil.SimpleColor(88, 54, 199, 255)), POS("pos",
                PosArgument.class, new ColorUtil.SimpleColor(223, 51, 68, 255)), BLOCK_STATE("block_state",
                        BlockStateArgument.class, new ColorUtil.SimpleColor(87, 195, 230, 255)), BLOCK_PREDICATE(
                                "block_predicate", BlockPredicateArgumentType.BlockPredicate.class,
                                new ColorUtil.SimpleColor(236, 44, 107, 255)), ITEM_STACK("item_stack",
                                        ItemStackArgument.class,
                                        new ColorUtil.SimpleColor(87, 195, 230, 255)), ITEM_PREDICATE("item_predicate",
                                                ItemPredicateArgumentType.ItemPredicateArgument.class,
                                                new ColorUtil.SimpleColor(206, 68, 96, 255)), FORMATTING("formatting",
                                                        Formatting.class,
                                                        new ColorUtil.SimpleColor(93, 90, 211, 255)), TEXT("text",
                                                                Text.class,
                                                                new ColorUtil.SimpleColor(143, 50, 62, 255)), INTEGER(
                                                                        "integer", Integer.class,
                                                                        new ColorUtil.SimpleColor(164, 72, 229,
                                                                                255)), STRING(
                                                                                        "string", String.class,
                                                                                        new ColorUtil.SimpleColor(219,
                                                                                                121, 134, 255)), COLOR(
                                                                                                        "color",
                                                                                                        Formatting.class,
                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                102,
                                                                                                                128,
                                                                                                                229,
                                                                                                                255)), MESSAGE_FORMAT(
                                                                                                                        "message_format",
                                                                                                                        MessageArgumentType.MessageFormat.class,
                                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                                211,
                                                                                                                                71,
                                                                                                                                134,
                                                                                                                                255)), NBT_COMPOUND(
                                                                                                                                        "nbt_compound",
                                                                                                                                        NbtCompound.class,
                                                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                                                65,
                                                                                                                                                92,
                                                                                                                                                154,
                                                                                                                                                255)), NBT_ELEMENT(
                                                                                                                                                        "nbt_element",
                                                                                                                                                        NbtElement.class,
                                                                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                                                                222,
                                                                                                                                                                65,
                                                                                                                                                                174,
                                                                                                                                                                255)), NBT_PATH(
                                                                                                                                                                        "nbt_path",
                                                                                                                                                                        NbtPathArgumentType.NbtPath.class,
                                                                                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                                                                                73,
                                                                                                                                                                                69,
                                                                                                                                                                                147,
                                                                                                                                                                                255)), SCOREBOARD_CRITERION(
                                                                                                                                                                                        "scoreboard_criterion",
                                                                                                                                                                                        NbtPathArgumentType.NbtPath.class,
                                                                                                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                                                                                                208,
                                                                                                                                                                                                71,
                                                                                                                                                                                                212,
                                                                                                                                                                                                255)), OPERATION(
                                                                                                                                                                                                        "operation",
                                                                                                                                                                                                        OperationArgumentType.Operation.class,
                                                                                                                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                                                                                                                135,
                                                                                                                                                                                                                49,
                                                                                                                                                                                                                90,
                                                                                                                                                                                                                255)), PARTICLE(
                                                                                                                                                                                                                        "particle",
                                                                                                                                                                                                                        ParticleEffect.class,
                                                                                                                                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                                                                                                                                178,
                                                                                                                                                                                                                                161,
                                                                                                                                                                                                                                231,
                                                                                                                                                                                                                                255)), ANGLE(
                                                                                                                                                                                                                                        "angle",
                                                                                                                                                                                                                                        AngleArgumentType.Angle.class,
                                                                                                                                                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                                                                                                                                                157,
                                                                                                                                                                                                                                                49,
                                                                                                                                                                                                                                                134,
                                                                                                                                                                                                                                                255)), SCORE_HOLDER(
                                                                                                                                                                                                                                                        "score_holder",
                                                                                                                                                                                                                                                        ScoreHolderArgumentType.ScoreHolder.class,
                                                                                                                                                                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                                                                                                                                                                230,
                                                                                                                                                                                                                                                                150,
                                                                                                                                                                                                                                                                205,
                                                                                                                                                                                                                                                                255)), STATUS_EFFECT(
                                                                                                                                                                                                                                                                        "status_effect",
                                                                                                                                                                                                                                                                        StatusEffect.class,
                                                                                                                                                                                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                                                                                                                                                                                129,
                                                                                                                                                                                                                                                                                53,
                                                                                                                                                                                                                                                                                162,
                                                                                                                                                                                                                                                                                255)), FUNCTION_ARGUMENT(
                                                                                                                                                                                                                                                                                        "function_argument",
                                                                                                                                                                                                                                                                                        CommandFunctionArgumentType.FunctionArgument.class,
                                                                                                                                                                                                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                                                                                                                                                                                                168,
                                                                                                                                                                                                                                                                                                98,
                                                                                                                                                                                                                                                                                                144,
                                                                                                                                                                                                                                                                                                255)), ENTITY_ANCHOR(
                                                                                                                                                                                                                                                                                                        "entity_anchor",
                                                                                                                                                                                                                                                                                                        EntityAnchorArgumentType.EntityAnchor.class,
                                                                                                                                                                                                                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                                                                                                                                                                                                                203,
                                                                                                                                                                                                                                                                                                                122,
                                                                                                                                                                                                                                                                                                                221,
                                                                                                                                                                                                                                                                                                                255)), ENCHANTMENT(
                                                                                                                                                                                                                                                                                                                        "enchantment",
                                                                                                                                                                                                                                                                                                                        Enchantment.class,
                                                                                                                                                                                                                                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                                                                                                                                                                                                                                115,
                                                                                                                                                                                                                                                                                                                                57,
                                                                                                                                                                                                                                                                                                                                119,
                                                                                                                                                                                                                                                                                                                                255)), UUID(
                                                                                                                                                                                                                                                                                                                                        "uuid",
                                                                                                                                                                                                                                                                                                                                        java.util.UUID.class,
                                                                                                                                                                                                                                                                                                                                        new ColorUtil.SimpleColor(
                                                                                                                                                                                                                                                                                                                                                138,
                                                                                                                                                                                                                                                                                                                                                108,
                                                                                                                                                                                                                                                                                                                                                182,
                                                                                                                                                                                                                                                                                                                                                255)),;

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
            for (ConfigStorage.SaveableConfig<? extends IConfigBase> saveable : CommandColorerStorage.OPTIONS) {
                options.add(saveable.config);
            }
            for (ConfigStorage.SaveableConfig<? extends IConfigBase> saveable : parent.colors.values()) {
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
