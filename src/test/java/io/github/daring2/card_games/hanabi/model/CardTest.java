package io.github.daring2.card_games.hanabi.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardTest {

    @Test
    void testToString() {
        assertThat(new Card(1, Color.WHITE)).hasToString("W1");
        assertThat(new Card(2, Color.RED)).hasToString("R2");
    }

}