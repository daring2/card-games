package io.github.daring2.hanabi.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.daring2.hanabi.model.DeckFactory.DECK_SIZE;
import static org.assertj.core.api.Assertions.assertThat;

class DeckFactoryTest {

    @Test
    void testCreate() {
        var deck = new DeckFactory().create();
        assertThat(deck.size()).isEqualTo(DECK_SIZE);
        for (Color color : Color.values()) {
            assertThat(getCardCount(deck, color, 1)).isEqualTo(3);
            assertThat(getCardCount(deck, color, 2)).isEqualTo(2);
            assertThat(getCardCount(deck, color, 3)).isEqualTo(2);
            assertThat(getCardCount(deck, color, 4)).isEqualTo(2);
            assertThat(getCardCount(deck, color, 5)).isEqualTo(1);
        }
    }

    long getCardCount(List<Card> deck, Color color, int value) {
        return deck.stream()
                .filter(c -> c.color() == color && c.value() == value)
                .count();
    }

}