package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardTest {

    @Test
    void testConstructor() {
        assertThat(new Card(Color.WHITE, 1)).satisfies(card -> {
            assertThat(card.color()).isEqualTo(Color.WHITE);
            assertThat(card.value()).isEqualTo(1);
        });
        assertThatThrownBy(() -> new Card(null, 1))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("color");
    }

    @Test
    void testToString() {
        assertThat(new Card(Color.WHITE, 1)).hasToString("W-1");
        assertThat(new Card(Color.RED, 2)).hasToString("R-2");
    }

}