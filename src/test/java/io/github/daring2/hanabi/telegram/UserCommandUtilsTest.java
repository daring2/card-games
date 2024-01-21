package io.github.daring2.hanabi.telegram;

import io.github.daring2.hanabi.model.CardInfo;
import io.github.daring2.hanabi.model.GameException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import static io.github.daring2.hanabi.model.Color.WHITE;
import static io.github.daring2.hanabi.model.Color.YELLOW;
import static io.github.daring2.hanabi.telegram.UserCommandUtils.parseCardInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserCommandUtilsTest {

    @Test
    void testParseCardInfo() {
        checkInvalidSuggestionError(() -> parseCardInfo(null));
        checkInvalidSuggestionError(() -> parseCardInfo(""));

        checkInvalidSuggestionError(() -> parseCardInfo("0"));
        assertThat(parseCardInfo("1")).isEqualTo(new CardInfo(1));
        assertThat(parseCardInfo("5")).isEqualTo(new CardInfo(5));
        checkInvalidSuggestionError(() -> parseCardInfo("6"));

        checkInvalidSuggestionError(() -> parseCardInfo("A"));
        assertThat(parseCardInfo("W")).isEqualTo(new CardInfo(WHITE));
        assertThat(parseCardInfo("w")).isEqualTo(new CardInfo(WHITE));
        assertThat(parseCardInfo("Y")).isEqualTo(new CardInfo(YELLOW));
        checkInvalidSuggestionError(() -> parseCardInfo("Z"));
    }

    void checkInvalidSuggestionError(ThrowableAssert.ThrowingCallable action) {
        assertThatExceptionOfType(GameException.class)
                .isThrownBy(action)
                .withMessage("invalid_suggestion");
    }

}