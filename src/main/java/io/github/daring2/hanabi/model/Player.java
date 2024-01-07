package io.github.daring2.hanabi.model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    final String name;
    final List<Card> cards = new ArrayList<>();

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    Card removeCard(int index) {
        var card = cards.remove(index);
//        knownCards.remove(card);
        return card;
    }

    @Override
    public String toString() {
        return name;
    }

}
