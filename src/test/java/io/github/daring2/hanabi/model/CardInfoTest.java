package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

import static io.github.daring2.hanabi.model.CardInfo.NULL_VALUE;
import static io.github.daring2.hanabi.model.Color.WHITE;
import static org.assertj.core.api.Assertions.assertThat;

class CardInfoTest {

    @Test
    void testIsValidForSuggest() {
        assertThat(new CardInfo(WHITE).isValidForSuggest()).isTrue();
        assertThat(new CardInfo(1).isValidForSuggest()).isTrue();
        assertThat(new CardInfo(WHITE, 1).isValidForSuggest()).isFalse();
    }

    @Test
    void testMerge() {
        assertThat(new CardInfo(WHITE).merge(new CardInfo(Color.RED)))
                .isEqualTo(new CardInfo(WHITE));
        assertThat(new CardInfo(1).merge(new CardInfo(2)))
                .isEqualTo(new CardInfo(1));
        assertThat(new CardInfo(WHITE).merge(new CardInfo(1)))
                .isEqualTo(new CardInfo(WHITE, 1));
        assertThat(new CardInfo(1).merge(new CardInfo(WHITE)))
                .isEqualTo(new CardInfo(WHITE, 1));
        assertThat(new CardInfo(WHITE, 1).merge(new CardInfo(Color.RED, 2)))
                .isEqualTo(new CardInfo(WHITE, 1));
    }

    @Test
    void testToString() {
        assertThat(new CardInfo(null).toString()).isEqualTo("?-?");
        assertThat(new CardInfo(WHITE).toString()).isEqualTo("W-?");
        assertThat(new CardInfo(1).toString()).isEqualTo("?-1");
        assertThat(new CardInfo(WHITE, 1).toString()).isEqualTo("W-1");
    }
}