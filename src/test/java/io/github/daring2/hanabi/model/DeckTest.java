package io.github.daring2.hanabi.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class DeckTest {

    Deck newDeck(Card... cards) {
        return new Deck(new ArrayList<>(List.of(cards)));
    }

    @Test
    void testIsEmpty() {
        assertThat(newDeck().isEmpty()).isTrue();
        assertThat(newDeck(new Card(Color.WHITE, 1)).isEmpty()).isFalse();
    }

    @Test
    void testTakeCard() {
        var cards = List.of(new Card(Color.WHITE, 1), new Card(Color.RED, 2));
        var deck = new Deck(new ArrayList<>(cards));
        assertThat(deck.isEmpty()).isFalse();
        assertThat(deck.takeCard()).isEqualTo(cards.get(1));
        assertThat(deck.takeCard()).isEqualTo(cards.get(0));
        assertThat(deck.isEmpty()).isTrue();
        assertThatThrownBy(deck::takeCard).hasMessage("Deck is empty");
    }

}