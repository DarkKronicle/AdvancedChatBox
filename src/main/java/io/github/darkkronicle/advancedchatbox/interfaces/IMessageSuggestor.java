package io.github.darkkronicle.advancedchatbox.interfaces;

import com.mojang.brigadier.context.StringRange;
import io.github.darkkronicle.advancedchatbox.chat.AdvancedSuggestion;
import io.github.darkkronicle.advancedchatbox.chat.AdvancedSuggestions;

import java.util.List;
import java.util.Optional;

/**
 * An interface for building suggestions for the {@link io.github.darkkronicle.advancedchat.chat.ChatSuggestor}
 */
public interface IMessageSuggestor {

    /**
     * Suggests completions to different parts of the text.
     *
     * Example:
     * - Suggests L to all GJ
     *
     * Input:
     *   `Wow GJ everyone! GJ`
     *
     * Built suggestions:
     *   `Wow GJ everyone! GJ`
     *        L            L
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
     * Example:
     * - Suggests L to all GJ
     *
     * Input:
     *   `Wow GJ everyone! GJ`
     *
     * Built suggestions:
     *   `Wow GJ everyone! GJ`
     *                     L
     *
     * @param text Content of the chat box to suggest
     * @return List of {@link AdvancedSuggestion} to suggest
     */
    default Optional<List<AdvancedSuggestion>> suggestCurrentWord(String text, StringRange range) {
        return Optional.empty();
    }

}
