package io.github.daring2.card_games.hanabi.model;

import java.util.Collections;
import java.util.List;

public class Deck {

    private final List<Card> cards;

    public Deck(List<Card> cards) {
        this.cards = cards;
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

}
