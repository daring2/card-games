package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardTest {

    @Test
    void testToString() {
        assertThat(new Card(1, Color.WHITE)).hasToString("W-1");
        assertThat(new Card(2, Color.RED)).hasToString("R-2");
    }

}