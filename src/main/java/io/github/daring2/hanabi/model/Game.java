package io.github.daring2.hanabi.model;

import io.github.daring2.hanabi.model.event.*;

import java.util.*;

import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;

public class Game {

    public static final int MAX_CARD_VALUE = 5;
    public static final int MAX_SCORE = 25;

    final GameSettings settings;

    final String id = randomUUID().toString();
    final GameEventBus eventBus = new GameEventBus();
    final List<GameEvent> events = new ArrayList<>();

    final List<Card> deck = new ArrayList<>();
    final List<Player> players = new ArrayList<>();
    final Map<Color, List<Card>> table = new EnumMap<>(Color.class);
    final List<Card> discard = new ArrayList<>();

    boolean started;
    int turn;
    int fireworks;
    int blueTokens;
    int redTokens;
    int lastTurn = -1;
    GameResult result;

    public Game(GameSettings settings) {
        this.settings = settings;
        this.blueTokens = settings.maxBlueTokens;
        eventBus.subscribe(events::add);
    }

    public Game() {
        this(new GameSettings());
    }

    public String id() {
        return id;
    }

    public GameSettings settings() {
        return settings;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isActive() {
        return started && result == null;
    }

    public int deckSize() {
        return deck.size();
    }

    public int blueTokens() {
        return blueTokens;
    }

    public int redTokens() {
        return redTokens;
    }

    public List<Card> tableCards() {
        return table.values().stream()
                .map(List::getLast)
                .toList();
    }

    public List<Player> players() {
        return unmodifiableList(players);
    }

    public GameEventBus eventBus() {
        return eventBus;
    }

    public void setDeck(List<Card> cards) {
        checkNotStarted();
        deck.clear();
        deck.addAll(cards);
    }

    public void addPlayer(Player player) {
        checkNotStarted();
        validate(players.size() < settings.maxPlayers, "too_many_players");
        players.add(player);
        publishEvent(new AddPlayerEvent(this, player));
    }

    public void removePlayer(Player player) {
        var removed = players.remove(player);
        if (!removed)
            return;
        publishEvent(new RemovePlayerEvent(this, player));
        if (started && result == null) {
            finish(GameResult.CANCEL);
        }
    }

    public void start() {
        checkNotStarted();
        checkPlayersBeforeStart();
        for (Color color : Color.valueList) {
            var cards = new ArrayList<Card>();
            cards.add(new Card(color, 0));
            table.put(color, cards);
        }
        var initCards = getInitPlayerCardsCount();
        for (int i = 0; i < initCards; i++) {
            players.forEach(this::takeCard);
        }
        started = true;
        publishEvent(new StartGameEvent(this));
        startNextTurn();
    }

    void finish(GameResult result) {
        validate(result != null, "result_is_null");
        this.result = result;
        publishEvent(new FinishGameEvent(
                this,
                result,
                calculateResultScore()
        ));
    }

    int calculateResultScore() {
        return table.values().stream()
                .mapToInt(it -> it.getLast().value())
                .sum();
    }

    void checkPlayersBeforeStart() {
        var playersCount = players.size();
        validate(playersCount >= settings.minPlayers, "not_enough_players");
        validate(playersCount <= settings.maxPlayers, "too_many_players");
    }

    int getInitPlayerCardsCount() {
        return players.size() <= 3 ? 5 : 4;
    }

    public void discardCard(Player player, int cardIndex) {
        checkCardIndex(player, cardIndex);
        validate(
                blueTokens < settings.maxBlueTokens,
                "all_blue_tokens_in_game"
        );
        performPlayerAction(player, () -> {
            var card = player.removeCard(cardIndex);
            discard.add(card);
            blueTokens++;
            publishEvent(new DiscardCardEvent(this, player, card));
            takeCard(player);
        });
    }

    public void playCard(Player player, int cardIndex) {
        checkCardIndex(player, cardIndex);
        performPlayerAction(player, () -> {
            var card = player.removeCard(cardIndex);
            var tableCards = table.get(card.color());
            var lastValue = tableCards.getLast().value();
            var isValid = card.value() == lastValue + 1;
            publishEvent(new PlayCardEvent(this, player, card, isValid));
            if (isValid) {
                addCardToTable(card);
            } else {
                discard.add(card);
                addRedToken();
            }
            takeCard(player);
        });
    }

    public void suggest(
            Player player,
            Player targetPlayer,
            CardInfo info
    ) {
        performPlayerAction(player, () -> {
            validate(targetPlayer != player, "invalid_target_player");
            validate(info.isValidForSuggest(), "invalid_suggestion");
            validate(blueTokens > 0, "no_blue_tokens_available");
            publishEvent(new SuggestEvent(this, player, targetPlayer, info));
            blueTokens--;
            targetPlayer.addCardInfo(info);
        });
    }

    public void launchFireworks(Player player) {
        performPlayerAction(player, () -> {
            validate(deck.isEmpty(), "deck_not_empty");
            finish(GameResult.LAUNCH);
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
        publishEvent(new AddCardToTableEvent(this, card));
        if (card.value() == MAX_CARD_VALUE) {
            fireworks++;
            publishEvent(new CreateFireworkEvent(this, card));
            if (blueTokens < settings.maxBlueTokens) {
                blueTokens++;
            }
        }
        if (fireworks >= settings.maxFireworks) {
            finish(GameResult.LAUNCH);
        }
    }

    void addRedToken() {
        redTokens++;
        publishEvent(new AddRedTokenEvent(this, redTokens));
        if (redTokens >= settings.maxRedTokens) {
            finish(GameResult.LOSS);
        }
    }

    void takeCard(Player player) {
        if (result != null)
            return;
        if (deck.isEmpty())
            return;
        player.cards.add(deck.removeLast());
        if (deck.isEmpty()) {
            if (settings.lastTurnOnEmptyDeck) {
                lastTurn = turn + players.size();
            }
            publishEvent(new DeckEmptyEvent(this, lastTurn > 0));
        }
    }

    void startNextTurn() {
        checkActive();
        if (lastTurn > 0 && turn >= lastTurn) {
            finish(GameResult.LAUNCH);
            return;
        }
        turn++;
        publishEvent(new StartTurnEvent(this, turn));
    }

    public Player currentPlayer() {
        if (players.isEmpty())
            return null;
        return players.get((turn - 1) % players.size());
    }

    void checkNotStarted() {
        validate(!started, "game_started");
    }

    void checkActive() {
        validate(started, "game_not_started");
        validate(result == null, "game_finished");
    }

    void checkCurrentPlayer(Player player) {
        var currentPlayer = currentPlayer();
        validate(
                player == currentPlayer,
                "player_not_current", currentPlayer
        );
    }

    void checkCardIndex(Player player, int index) {
        if (index < 0 || index >= player.cards.size()) {
            throw new GameException("invalid_card_index");
        }
    }

    void validate(boolean expression, String code, Object... args) {
        if (!expression) {
            throw new GameException(code, args);
        }
    }

    void publishEvent(GameEvent event) {
        eventBus.publish(event);
    }

    @Override
    public String toString() {
        return "Game(id=" + id + ")";
    }

}
