/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatbox.formatter;

import io.github.darkkronicle.advancedchatcore.util.StringMatch;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class CommandSection<T> implements Comparable<CommandSection<T>> {

    public enum Section {
        COMMAND, ARGUMENT,
    }

    @Override
    public int compareTo(@NotNull CommandSection<T> o) {
        return match.compareTo(o.match);
    }

    @Getter
    private final T value;

    @Getter
    private final StringMatch match;

    @Getter
    private final Section section;

}
