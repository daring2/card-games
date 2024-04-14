package io.github.daring2.hanabi.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;
import io.github.daring2.hanabi.model.event.*;
import io.github.daring2.hanabi.util.JsonAutoDetectFields;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static io.github.daring2.hanabi.model.GameUtils.validate;
import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;

@JsonAutoDetectFields
@JsonIdentityInfo(generator = PropertyGenerator.class, property = "id")
public class Game {

    public static final int MAX_CARD_VALUE = 5;
    public static final int MAX_SCORE = 25;

    final GameSettings settings;

    final String id = randomUUID().toString();
    @JsonIgnore
    final GameEventBus eventBus = new GameEventBus();
    final List<Card> deck = new ArrayList<>();
    final List<Player> players = new ArrayList<>();
    final Map<Color, List<Card>> table = new EnumMap<>(Color.class);
    final List<Card> discard = new ArrayList<>();
    @JsonIgnore
    final List<GameEvent> events = new ArrayList<>();

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

    public boolean isFinished() {
        return result != null;
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
        new DiscardCardAction(this, player, cardIndex).execute();
    }

    public void playCard(Player player, int cardIndex) {
        new PlayCardAction(this, player, cardIndex).execute();
    }

    public void suggest(Player player, Player targetPlayer, CardInfo info) {
        new SuggestAction(this, player, targetPlayer, info).execute();
    }

    public void launchFireworks(Player player) {
        new LaunchFireworksAction(this, player).execute();
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

    void publishEvent(GameEvent event) {
        eventBus.publish(event);
    }

    @Override
    public String toString() {
        return "Game(id=" + id + ")";
    }

}
