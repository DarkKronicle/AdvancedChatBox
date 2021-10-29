/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatbox.interfaces;

import com.mojang.brigadier.context.StringRange;
import io.github.darkkronicle.advancedchatbox.chat.AdvancedSuggestion;
import io.github.darkkronicle.advancedchatbox.chat.AdvancedSuggestions;
import java.util.List;
import java.util.Optional;

/**
 * An interface for building suggestions for the {@link
 * io.github.darkkronicle.advancedchat.chat.ChatSuggestor}
 */
public interface IMessageSuggestor {
    /**
     * Suggests completions to different parts of the text.
     *
     * <p>Example: - Suggests L to all GJ
     *
     * <p>Input: `Wow GJ everyone! GJ`
     *
     * <p>Built suggestions: `Wow GJ everyone! GJ` L L
     *
     * @param text Content of the chat box to suggest
     * @return List of {@link AdvancedSuggestions} to suggest
     */
    default Optional<List<AdvancedSuggestions>> suggest(String text) {
        return Optional.empty();
    }

    /**
     * Suggests completions to the last word of the text.
     *
     * <p>Example: - Suggests L to all GJ
     *
     * <p>Input: `Wow GJ everyone! GJ`
     *
     * <p>Built suggestions: `Wow GJ everyone! GJ` L
     *
     * @param text Content of the chat box to suggest
     * @return List of {@link AdvancedSuggestion} to suggest
     */
    default Optional<List<AdvancedSuggestion>> suggestCurrentWord(String text, StringRange range) {
        return Optional.empty();
    }
}
