package io.github.daring2.hanabi.model;

import org.apache.commons.lang3.Validate;

import java.util.*;

public class Game {

    public static final int MAX_CARD_VALUE = 5;
    public static final int MAX_BLUE_TOKENS = 8;
    public static final int MAX_RED_TOKENS = 3;
    public static final int MAX_FIREWORKS = 8;

    final List<Card> deck = new ArrayList<>();
    final List<Player> players = new ArrayList<>();
    final Map<Color, List<Card>> table = new EnumMap<>(Color.class);
    final List<Card> discard = new ArrayList<>();

    boolean started;
    int turn;
    int fireworks;
    int blueTokens = MAX_BLUE_TOKENS;
    int redTokens = MAX_RED_TOKENS;
    GameResult result;

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
        var initCards = getInitCards();
        for (int i = 0; i < initCards; i++) {
            players.forEach(this::takeCard);
        }
        turn = 1;
        started = true;
    }

    int getInitCards() {
        return players.size() <= 3 ? 5 : 4;
    }

    public void discardCard(Player player, int cardIndex) {
        Validate.isTrue(
                blueTokens < MAX_BLUE_TOKENS,
                "All blue tokens are in the game"
        );
        performPlayerAction(player, () -> {
            var card = player.removeCard(cardIndex);
            discard.add(card);
            takeCard(player);
        });
    }

    public void playCard(Player player, int cardIndex) {
        performPlayerAction(player, () -> {
            var card = player.removeCard(cardIndex);
            var tableCards = table.get(card.color());
            var lastValue = tableCards.getLast().value();
            if (card.value() == lastValue + 1) {
                addCardToTable(card);
            } else {
                discard.add(card);
                discardRedToken();
            }
            takeCard(player);
        });
    }

    public void shareInfo(
            Player player,
            Player targetPlayer,
            CardInfo info
    ) {
        performPlayerAction(player, () -> {
            Validate.isTrue(
                    info.isValidForShare(),
                    "Only one property can be shared"
            );
            Validate.isTrue(
                    blueTokens > 0,
                    "No blue tokens are available"
            );
            blueTokens--;
            targetPlayer.addCardInfo(info);
        });
    }

    void performPlayerAction(Player player, Runnable action) {
        checkActive();
        checkCurrentPlayer(player);
        action.run();
        if (result != null)
            return;
        startNextTurn();
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

    void takeCard(Player player) {
        if (result != null) {
            return;
        }
        //TODO check if deck is empty
        Validate.validState(!deck.isEmpty(), "Deck is empty");
        player.cards.add(deck.removeLast());
    }

    void startNextTurn() {
        checkActive();
        turn++;
    }

    Player getCurrentPlayer() {
        return players.get((turn - 1) % players.size());
    }

    void checkNotStarted() {
        Validate.validState(!started, "Game is started");
    }

    void checkActive() {
        Validate.validState(started, "Game is not started");
        Validate.validState(result == null, "Game is finished");
    }

    void checkCurrentPlayer(Player player) {
        Validate.isTrue(
                player == getCurrentPlayer(),
                "Player '%s' is not current", player
        );
    }

}
