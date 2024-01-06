package io.github.daring2.hanabi.model;

import java.util.ArrayList;

import static io.github.daring2.hanabi.model.Game.MAX_CARD_VALUE;

public class DeckFactory {

    public Deck create() {
        var cards = new ArrayList<Card>();
        for (Color color : Color.values()) {
            for (int value = 1; value <= MAX_CARD_VALUE; value++) {
                var cardNumber = getCardNumber(value);
                for (int i = 0; i < cardNumber; i++) {
                    cards.add(new Card(color, value));
                }
            }
        }
        return new Deck(cards);
    }

    protected int getCardNumber(int value) {
        return switch (value) {
            case 1 -> 3;
            case 5 -> 1;
            default -> 2;
        };
    }


}
