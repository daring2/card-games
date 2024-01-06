package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardTest {

    @Test
    void testToString() {
        assertThat(new Card(Color.WHITE, 1)).hasToString("W-1");
        assertThat(new Card(Color.RED, 2)).hasToString("R-2");
    }

}