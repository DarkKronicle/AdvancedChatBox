package io.github.darkkronicle.advancedchatbox.suggester;

import com.mojang.brigadier.context.StringRange;
import io.github.darkkronicle.advancedchatbox.chat.AdvancedSuggestion;
import io.github.darkkronicle.advancedchatbox.chat.AdvancedSuggestions;
import io.github.darkkronicle.advancedchatbox.interfaces.IMessageSuggestor;
import io.github.darkkronicle.advancedchatcore.util.FindType;
import io.github.darkkronicle.advancedchatcore.util.SearchUtils;
import io.github.darkkronicle.advancedchatcore.util.StringMatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.mariuszgromada.math.mxparser.Expression;

@Environment(EnvType.CLIENT)
public class CalculatorSuggestor implements IMessageSuggestor {

    private static final String BRACKET_REGEX = "\\[[^\\[\\]]*\\]";
    public static final String NAN = "NaN";

    @Override
    public Optional<List<AdvancedSuggestions>> suggest(String text) {
        if (!text.contains("[") || !text.contains("]")) {
            return Optional.empty();
        }
        List<StringMatch> matches = SearchUtils
            .findMatches(text, BRACKET_REGEX, FindType.REGEX)
            .orElse(null);
        if (matches == null) {
            return Optional.empty();
        }
        int last = -1;
        ArrayList<AdvancedSuggestions> suggest = new ArrayList<>();
        for (StringMatch m : matches) {
            if (m.start < last || m.end - m.start < 1) {
                // Don't want overlapping matches (just in case) or too small
                continue;
            }
            last = m.end;
            String string = m.match.substring(1, m.match.length() - 1);
            Expression expression = new Expression(string);
            double val = expression.calculate();
            String message = NAN;
            if (!Double.isNaN(val)) {
                message = String.valueOf(val);
            }
            StringRange range = new StringRange(m.start, m.end);
            suggest.add(
                new AdvancedSuggestions(
                    range,
                    new ArrayList<>(
                        Collections.singleton(
                            new AdvancedSuggestion(range, message)
                        )
                    )
                )
            );
        }
        if (suggest.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(suggest);
    }
}
