/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatbox.config;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.config.SaveableConfig;
import io.github.darkkronicle.advancedchatcore.config.options.ConfigColor;
import io.github.darkkronicle.advancedchatcore.config.options.ConfigColor;
import io.github.darkkronicle.advancedchatcore.util.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CommandColorerStorage {

    public static String translate(String key) {
        return StringUtils.translate("advancedchatbox.config.commandcolorer." + key);
    }

    public static final String NAME = "commandcolorer";

    public static final SaveableConfig<ConfigColor> COMMAND_COLOR = SaveableConfig.fromConfig("commandColor",
            new ConfigColor(translate("commandcolor"), new Color(180, 180, 180, 255), translate("info.commandcolor")));

    public static final SaveableConfig<ConfigColor> ERROR_COLOR = SaveableConfig.fromConfig("errorColor",
            new ConfigColor(translate("errorcolor"), new Color(255, 60, 60, 255), translate("info.errorcolor")));

    public static final SaveableConfig<ConfigString> DEFAULT_PALETTE = SaveableConfig.fromConfig("defaultPalette",
            new ConfigString(translate("defaultpalette"), "pastel_rgb", translate("info.defaultpalette")));

    public static final ImmutableList<SaveableConfig<? extends IConfigBase>> OPTIONS =
            ImmutableList.of(COMMAND_COLOR, ERROR_COLOR, DEFAULT_PALETTE);
}
