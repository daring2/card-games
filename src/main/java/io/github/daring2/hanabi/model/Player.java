package io.github.daring2.hanabi.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;
import io.github.daring2.hanabi.util.JsonAutoDetectFields;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;

@JsonAutoDetectFields
@JsonIdentityInfo(generator = PropertyGenerator.class, property = "id")
public class Player {

    final String id = randomUUID().toString();
    final String name;
    final List<Card> cards = new ArrayList<>();
    final Map<Card, CardInfo> knownCards = new IdentityHashMap<>();

    public Player(String name) {
        this.name = name;
    }

    // Constructor for JSON deserialization
    private Player() {
        this("");
    }

    public String id() {
        return id;
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
