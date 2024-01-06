package io.github.daring2.hanabi.model;

import org.apache.commons.lang3.Validate;

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
        Validate.validState(!isEmpty(), "deck is empty");
        return cards.removeLast();
    }

}
