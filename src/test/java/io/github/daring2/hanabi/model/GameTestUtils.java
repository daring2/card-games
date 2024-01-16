package io.github.daring2.hanabi.model;

import java.util.List;

public class GameTestUtils {

    public static Player newPlayer(String name, List<Card> cards) {
        var player = new Player(name);
        player.cards.addAll(cards);
        return player;
    }

    private GameTestUtils() {
    }

}
