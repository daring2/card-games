package io.github.daring2.hanabi.model;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.daring2.hanabi.model.Game.MAX_CARD_VALUE;

@Component
public class DeckFactory {

    public static final int DECK_SIZE = 50;

    public List<Card> create() {
        var cards = new ArrayList<Card>();
        for (Color color : Color.valueList) {
            for (int value = 1; value <= MAX_CARD_VALUE; value++) {
                var cardNumber = getCardNumber(value);
                for (int i = 0; i < cardNumber; i++) {
                    cards.add(new Card(color, value));
                }
            }
        }
        Collections.shuffle(cards);
        return cards;
    }

    protected int getCardNumber(int value) {
        return switch (value) {
            case 1 -> 3;
            case 5 -> 1;
            default -> 2;
        };
    }


}
