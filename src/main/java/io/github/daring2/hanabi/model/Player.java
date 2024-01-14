package io.github.daring2.hanabi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player {

    final String name;
    final List<Card> cards = new ArrayList<>();
    final Map<Card, CardInfo> knownCards = new HashMap<>();

    public Player(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    Card removeCard(int index) {
        var card = cards.remove(index);
        knownCards.remove(card);
        return card;
    }

    void addCardInfo(CardInfo info) {
        for (Card card : cards) {
            if (card.color() != info.color() && card.value() != info.value())
                continue;
            knownCards.merge(card, info, CardInfo::merge);
        }
    }

    @Override
    public String toString() {
        return name;
    }

}
