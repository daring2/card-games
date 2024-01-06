package io.github.daring2.hanabi.model;

import org.apache.commons.lang3.Validate;

import java.util.*;

public class Game {

    final Deck deck;

    final List<Player> players = new ArrayList<>();
    final Map<Color, List<Card>> table = new EnumMap<>(Color.class);

    boolean started;
    int turn;
    int blueTokens = 8;
    int redTokens = 3;
    GameResult result;

    public Game(Deck deck) {
        this.deck = deck;
    }

    public void join(Player player) {
        checkNotStarted();
        Validate.isTrue(players.size() < 5, "Maximum players in the game is 5");
        players.add(player);
    }

    public void start() {
        checkNotStarted();
        var playersCount = players.size();
        Validate.validState(
                playersCount >= 2 && playersCount <= 5,
                "Invalid players count: " + playersCount
        );
        for (Color color : Color.values()) {
            table.put(color, new ArrayList<>());
        }
        var initCards = players.size() <= 3 ? 5 : 4;
        for (int i = 0; i < initCards; i++) {
            players.forEach(this::takeCard);
        }
        turn = 0;
        started = true;
    }

    void takeCard(Player player) {
        player.cards.add(deck.takeCard());
    }

    void startNextTurn() {
        if (result != null)
            return;
        if (redTokens <= 0) {
            result = GameResult.LOSS;
        }
        turn++;
    }

    void checkNotStarted() {
        Validate.validState(!started, "game is started");
    }

}
