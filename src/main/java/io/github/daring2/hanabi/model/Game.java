package io.github.daring2.hanabi.model;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Game {

    public static final int MAX_CARD_VALUE = 5;
    public static final int MAX_BLUE_TOKENS = 8;
    public static final int MAX_RED_TOKENS = 8;
    public static final int MAX_FIREWORKS = 8;

    final Deck deck;

    final List<Player> players = new ArrayList<>();
    final Map<Color, List<Card>> table = new EnumMap<>(Color.class);
    final List<Card> discard = new ArrayList<>();

    boolean started;
    int turn;
    int fireworks;
    int blueTokens = MAX_BLUE_TOKENS;
    int redTokens = MAX_RED_TOKENS;
    GameResult result;

    public Game(Deck deck) {
        this.deck = deck;
    }

    public void join(Player player) {
        checkNotStarted();
        Validate.isTrue(
                players.size() < 5,
                "Maximum players in the game is 5"
        );
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
            var cards = new ArrayList<Card>();
            cards.add(new Card(color, 0));
            table.put(color, cards);
        }
        var initCards = players.size() <= 3 ? 5 : 4;
        for (int i = 0; i < initCards; i++) {
            players.forEach(this::takeCard);
        }
        turn = 1;
        started = true;
    }

    public void discardCard(Player player, int cardIndex) {
        Validate.isTrue(
                blueTokens < MAX_BLUE_TOKENS,
                "No blue tokens are available"
        );
        performPlayerAction(player, () -> {
            var card = player.cards.get(cardIndex);
            discard.add(card);
            takeCard(player);
        });
    }

    public void playCard(Player player, int cardIndex) {
        performPlayerAction(player, () -> {
            var card = player.cards.get(cardIndex);
            var tableCards = table.get(card.color());
            var lastValue = tableCards.getLast().value();
            if (card.value() == lastValue + 1) {
                addCardToTable(card);
            } else {
                discardRedToken();
            }
            takeCard(player);
        });
    }

    void addCardToTable(Card card) {
        table.get(card.color()).add(card);
        if (card.value() == MAX_CARD_VALUE) {
            fireworks++;
            if (blueTokens < MAX_BLUE_TOKENS) {
                blueTokens++;
            }
        }
        if (fireworks >= MAX_FIREWORKS) {
            result = GameResult.WIN;
        }
    }

    void discardRedToken() {
        redTokens--;
        if (redTokens <= 0) {
            result = GameResult.LOSS;
        }
    }

    public void shareInfo() {
        //TODO implement
    }

    void performPlayerAction(Player player, Runnable action) {
        checkActive();
        checkCurrentPlayer(player);
        action.run();
        if (result != null)
            return;
        startNextTurn();
    }

    void takeCard(Player player) {
        if (result != null) {
            return;
        }
        //TODO check if deck is empty
        player.cards.add(deck.takeCard());
    }

    void startNextTurn() {
        checkActive();
        turn++;
    }

    Player getCurrentPlayer() {
        return players.get((turn - 1) % players.size());
    }

    void checkNotStarted() {
        Validate.validState(!started, "game is started");
    }

    void checkActive() {
        Validate.validState(started, "game is not started");
        Validate.validState(result == null, "game is finished");
    }

    void checkCurrentPlayer(Player player) {
        Validate.isTrue(
                player == getCurrentPlayer(),
                "player '%s' is not current", player
        );
    }

}
