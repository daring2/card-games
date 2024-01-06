package io.github.daring2.hanabi.model;

import java.util.Collections;
import java.util.List;

public class Deck {

    final List<Card> cards;

    public Deck(List<Card> cards) {
        this.cards = cards;
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card takeCard() {
        if (isEmpty()) {
            throw new IllegalArgumentException("deck is empty");
        }
        return cards.removeLast();
    }

}
