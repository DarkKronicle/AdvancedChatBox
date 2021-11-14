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
import fi.dy.masa.malilib.util.StringUtils;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchatcore.config.options.ConfigSimpleColor;
import io.github.darkkronicle.advancedchatcore.util.ColorUtil;

public class CommandColorerStorage {

    public static String translate(String key) {
        return StringUtils.translate("advancedchatbox.config.general." + key);
    }

    public static final String NAME = "commandcolorer";

    public static final ConfigStorage.SaveableConfig<ConfigSimpleColor> COMMAND_COLOR =
            ConfigStorage.SaveableConfig.fromConfig("highlightColor", new ConfigSimpleColor(translate("highlightcolor"),
                    new ColorUtil.SimpleColor(180, 180, 180, 255), translate("info.highlightcolor")));

    public static final ConfigStorage.SaveableConfig<ConfigSimpleColor> ERROR_COLOR =
            ConfigStorage.SaveableConfig.fromConfig("errorColor", new ConfigSimpleColor(translate("errorcolor"),
                    new ColorUtil.SimpleColor(255, 60, 60, 255), translate("info.errorcolor")));

    public static final ImmutableList<ConfigStorage.SaveableConfig<? extends IConfigBase>> OPTIONS =
            ImmutableList.of(COMMAND_COLOR, ERROR_COLOR);
}
