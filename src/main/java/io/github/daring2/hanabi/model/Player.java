package io.github.daring2.hanabi.model;

import java.util.ArrayList;
import java.util.List;

public class Player {

    final String name;
    final List<Card> cards = new ArrayList<>();

    public Player(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
