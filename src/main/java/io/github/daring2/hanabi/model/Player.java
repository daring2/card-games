package io.github.daring2.hanabi.model;

import java.util.*;

import static java.util.Collections.unmodifiableList;

public class Player {

    final String name;
    final List<Card> cards = new ArrayList<>();
    final Map<Card, CardInfo> knownCards = new IdentityHashMap<>();

    public Player(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public List<Card> cards() {
        return unmodifiableList(cards);
    }

    public CardInfo getKnownCard(Card card) {
        return knownCards.getOrDefault(card, CardInfo.EMPTY);
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
