package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardInfoTest {

    @Test
    void testIsValidForShare() {
        assertThat(new CardInfo(Color.WHITE).isValidForShare()).isTrue();
        assertThat(new CardInfo(1).isValidForShare()).isTrue();
        assertThat(new CardInfo(Color.WHITE, 1).isValidForShare()).isFalse();
    }

    @Test
    void testMerge() {
        assertThat(new CardInfo(Color.WHITE).merge(new CardInfo(Color.RED)))
                .isEqualTo(new CardInfo(Color.WHITE));
        assertThat(new CardInfo(1).merge(new CardInfo(2)))
                .isEqualTo(new CardInfo(1));
        assertThat(new CardInfo(Color.WHITE).merge(new CardInfo(1)))
                .isEqualTo(new CardInfo(Color.WHITE, 1));
        assertThat(new CardInfo(1).merge(new CardInfo(Color.WHITE)))
                .isEqualTo(new CardInfo(Color.WHITE, 1));
        assertThat(new CardInfo(Color.WHITE, 1).merge(new CardInfo(Color.RED, 2)))
                .isEqualTo(new CardInfo(Color.WHITE, 1));
    }

}