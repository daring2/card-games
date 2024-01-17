package io.github.daring2.hanabi.model;

import io.github.daring2.hanabi.model.event.*;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

import static io.github.daring2.hanabi.model.Color.*;
import static io.github.daring2.hanabi.model.Game.*;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
class GameTest {

    @Test
    void testStart() {
        var game = newGame();
        game.players.clear();
        game.events.clear();
        checkNotEnoughPlayersError(game::start);

        game.addPlayer(new Player("p0"));
        checkNotEnoughPlayersError(game::start);

        game.addPlayer(new Player("p1"));
        game.start();
        assertThat(game.events).containsExactly(
                new PlayerAddedEvent(game, game.players.get(0)),
                new PlayerAddedEvent(game, game.players.get(1)),
                new GameStartedEvent(game),
                new TurnStartedEvent(game, 1)
        );
        for (Color color : Color.valueList) {
            assertThat(game.table.get(color)).singleElement()
                    .isEqualTo(new Card(color, 0));
        }
        assertThat(game.players).hasSize(2);
        for (Player player : game.players) {
            assertThat(player.cards).hasSize(5);
        }
        assertThat(game.turn).isEqualTo(1);
        assertThat(game.started).isTrue();

        checkGameStartedError(game::start);
    }

    @Test
    void testFinish() {
        var game = newGame();
        assertThatThrownBy(() -> game.finish(null))
                .isInstanceOf(GameException.class)
                .hasMessage("result_is_null");

        game.start();
        assertThat(game.result).isNull();

        game.events.clear();
        game.finish(GameResult.CANCEL);
        assertThat(game.result).isEqualTo(GameResult.CANCEL);
        assertThat(game.events).containsExactly(
                new FinishGameEvent(game, GameResult.CANCEL)
        );
    }

    @Test
    void testSetDeck() {
        var game = newGame();
        assertThat(game.deck).hasSize(25);
        var cards = rangeClosed(1, 2)
                .mapToObj(i -> new Card(WHITE, i))
                .toList();
        game.setDeck(cards);
        assertThat(game.deck).isEqualTo(cards);
    }

    @Test
    void testAddPlayer() {
        var game = newGame();
        game.players.clear();
        game.events.clear();

        var players = rangeClosed(0, 4)
                .mapToObj(i -> new Player("p" + i))
                .toList();
        players.forEach(game::addPlayer);
        assertThat(game.players).isEqualTo(players);
        assertThat(game.events).zipSatisfy(players, (event, player) -> {
            assertThat(event).isEqualTo(new PlayerAddedEvent(game, player));
        });

        assertThatThrownBy(() -> game.addPlayer(new Player("p5")))
                .isInstanceOf(GameException.class)
                .hasMessage("too_many_players");

        game.players.clear();
        game.started = true;
        checkGameStartedError(() -> game.addPlayer(players.getFirst()));
    }

    @Test
    void testRemovePlayer() {
        var game = newGame();
        game.players.clear();
        game.events.clear();

        var players = rangeClosed(0, 2)
                .mapToObj(i -> new Player("p" + i))
                .toList();
        players.forEach(game::addPlayer);
        assertThat(game.players).isEqualTo(players);

        var player1 = players.get(1);
        game.events.clear();
        game.removePlayer(player1);
        assertThat(game.players).containsExactly(players.get(0), players.get(2));
        assertThat(game.events).containsExactly(new PlayerRemovedEvent(game, player1));

        game.events.clear();
        game.removePlayer(player1);
        assertThat(game.players).containsExactly(players.get(0), players.get(2));
        assertThat(game.events).isEmpty();

        game.players.clear();
        players.forEach(game::addPlayer);
        game.start();
        game.events.clear();
        game.removePlayer(player1);
        assertThat(game.players).containsExactly(players.get(0), players.get(2));
        assertThat(game.result).isEqualTo(GameResult.CANCEL);
        assertThat(game.events).containsExactly(
                new PlayerRemovedEvent(game, player1),
                new FinishGameEvent(game, GameResult.CANCEL)
        );

    }

    @Test
    void testValidPlayersCount() {
        var game = newGame();
        game.players.clear();

        game.addPlayer(new Player("p0"));
        assertThatThrownBy(game::checkPlayersBeforeStart)
                .isInstanceOf(GameException.class)
                .hasMessage("not_enough_players");

        game.addPlayer(new Player("p1"));
        game.checkPlayersBeforeStart();

        rangeClosed(2, 5).forEach(i -> {
            game.players.add(new Player("p" + i));
        });
        assertThatThrownBy(game::checkPlayersBeforeStart)
                .isInstanceOf(GameException.class)
                .hasMessage("too_many_players");
    }

    @Test
    void testGetInitCards() {
        var game = newGame();
        assertThat(game.getInitCards()).isEqualTo(5);
        game.addPlayer(new Player("p2"));
        assertThat(game.getInitCards()).isEqualTo(5);
        game.addPlayer(new Player("p3"));
        assertThat(game.getInitCards()).isEqualTo(4);
    }

    @Test
    void testDiscardCard() {
        var game = newGame();

        var cards = new ArrayList<Card>();
        range(0, 20).forEach(i ->
                cards.add(new Card(WHITE, i))
        );
        game.setDeck(cards.reversed());

        game.start();
        assertThat(game.discard).isEmpty();

        var player0 = game.players.get(0);
        assertThatThrownBy(() -> game.discardCard(player0, 0))
                .isInstanceOf(GameException.class)
                .hasMessage("all_blue_tokens_in_game");
        assertThat(game.discard).isEmpty();

        game.blueTokens = 1;
        game.started = false;
        checkGameNotStartedError(() -> game.discardCard(player0, 0));
        assertThat(game.discard).isEmpty();

        game.started = true;
        checkCardIndexError(() -> game.discardCard(player0, -1));

        game.events.clear();
        game.discardCard(player0, 0);
        assertThat(player0.cards).map(Card::value)
                .containsExactly(2, 4, 6, 8, 10);
        assertThat(game.discard).map(Card::value)
                .containsExactly(0);
        assertThat(game.blueTokens).isEqualTo(2);
        assertThat(game.events).containsExactly(
                new DiscardCardEvent(game, player0, cards.get(0)),
                new TurnStartedEvent(game, 2)
        );

        var player1 = game.players.get(1);
        game.discardCard(player1, 0);
        assertThat(player1.cards).map(Card::value)
                .containsExactly(3, 5, 7, 9, 11);
        assertThat(game.discard).map(Card::value)
                .containsExactly(0, 1);
    }

    @Test
    void testPlayCard() {
        checkGame(it -> {
            var game = spy(it);
            var player0 = game.players.get(0);
            game.started = false;
            checkGameNotStartedError(() -> game.playCard(player0, 0));
            game.started = true;
            checkCardIndexError(() -> game.discardCard(player0, -1));
        });
        checkGame(it -> {
            var game = spy(it);
            game.events.clear();
            var player0 = game.players.get(0);
            var card0 = player0.cards.get(0);
            game.playCard(player0, 0); // W-1
            assertThat(player0.cards).hasSize(5)
                    .first().isEqualTo(new Card(WHITE, 3));
            verify(game, times(1))
                    .addCardToTable(card0);
            verify(game, times(0)).addRedToken();
            assertThat(game.redTokens).isEqualTo(0);
            assertThat(game.discard).isEmpty();
            verify(game, times(1)).takeCard(player0);
            assertThat(game.events).containsExactly(
                    new PlayCardEvent(game, player0, card0),
                    new TurnStartedEvent(game, 2)
            );
        });
        checkGame(it -> {
            var game = spy(it);
            var player0 = game.players.get(0);
            game.playCard(player0, 1); // W-3
            assertThat(player0.cards).hasSize(5)
                    .first().isEqualTo(new Card(WHITE, 1));
            verify(game, times(0))
                    .addCardToTable(new Card(WHITE, 1));
            verify(game, times(1)).addRedToken();
            assertThat(game.discard).containsExactly(new Card(WHITE, 3));
            assertThat(game.redTokens).isEqualTo(1);
            verify(game, times(1)).takeCard(player0);
        });
    }

    @Test
    void testSuggest() {
        var game = newGame();
        var player0 = game.players.get(0);
        var player1 = game.players.get(1);
        checkGameNotStartedError(() -> game.suggest(player0, player1, new CardInfo(WHITE)));

        game.start();
        assertThatThrownBy(() -> game.suggest(player0, player1, new CardInfo(WHITE, 1)))
                .isInstanceOf(GameException.class)
                .hasMessage("invalid_suggestion");

        game.blueTokens = 0;
        assertThatThrownBy(() -> game.suggest(player0, player1, new CardInfo(WHITE)))
                .isInstanceOf(GameException.class)
                .hasMessage("no_blue_tokens_available");

        game.blueTokens = 3;
        game.suggest(player0, player1, new CardInfo(WHITE));
        assertThat(game.blueTokens).isEqualTo(2);
    }

    @Test
    void testPerformPlayerAction() {
        var game = spy(newGame());
        doNothing().when(game).checkActive();
        doNothing().when(game).checkCurrentPlayer(any());
        doNothing().when(game).startNextTurn();

        var player0 = new Player("p0");
        var actionCalls = new ArrayList<String>();
        game.performPlayerAction(player0, () ->
                actionCalls.add("a0")
        );
        verify(game).checkActive();
        verify(game).checkCurrentPlayer(player0);
        verify(game).startNextTurn();
        assertThat(actionCalls).containsExactly("a0");
    }

    @Test
    void testAddCardToTable() {
        var cards = rangeClosed(0, 5)
                .mapToObj(i -> new Card(WHITE, i))
                .toList();
        checkGame(game -> {
            game.events.clear();
            game.addCardToTable(cards.get(1));
            assertThat(game.table.get(WHITE)).isEqualTo(cards.subList(0, 2));
            assertThat(game.fireworks).isEqualTo(0);
            assertThat(game.blueTokens).isEqualTo(MAX_BLUE_TOKENS);
            assertThat(game.result).isNull();
            assertThat(game.events).isEmpty();

            game.addCardToTable(cards.get(2));
            assertThat(game.table.get(WHITE)).isEqualTo(cards.subList(0, 3));
            assertThat(game.fireworks).isEqualTo(0);
            assertThat(game.blueTokens).isEqualTo(MAX_BLUE_TOKENS);
            assertThat(game.result).isNull();
            assertThat(game.events).isEmpty();
        });
        checkGame(game -> {
            game.events.clear();
            var card5 = cards.get(5);
            game.addCardToTable(card5);
            assertThat(game.table.get(WHITE)).containsExactly(cards.get(0), card5);
            assertThat(game.fireworks).isEqualTo(1);
            assertThat(game.blueTokens).isEqualTo(MAX_BLUE_TOKENS);
            assertThat(game.result).isNull();
            assertThat(game.events).containsExactly(
                    new CreateFireworkEvent(game, card5)
            );
        });
        checkGame(game -> {
            game.events.clear();
            game.fireworks = MAX_FIREWORKS - 1;
            game.addCardToTable(cards.get(5));
            assertThat(game.table.get(WHITE))
                    .containsExactly(cards.get(0), cards.get(5));
            assertThat(game.fireworks).isEqualTo(MAX_FIREWORKS);
            assertThat(game.blueTokens).isEqualTo(MAX_BLUE_TOKENS);
            assertThat(game.result).isEqualTo(GameResult.WIN);
        });

    }

    @Test
    void testAddRedToken() {
        var game = newGame();
        assertThat(game.redTokens).isEqualTo(0);
        assertThat(game.result).isNull();

        game.addRedToken();
        assertThat(game.redTokens).isEqualTo(1);
        assertThat(game.result).isNull();

        game.addRedToken();
        game.addRedToken();
        assertThat(game.redTokens).isEqualTo(3);
        assertThat(game.result).isEqualTo(GameResult.LOSS);
    }

    @Test
    void testTakeCard() {
        var game = newGame();
        var cards = new ArrayList<>(game.deck);
        Collections.reverse(cards);
        var player0 = game.players.get(0);
        player0.cards.clear();
        assertThat(player0.cards).isEmpty();

        game.takeCard(player0);
        assertThat(player0.cards).isEqualTo(cards.subList(0, 1));
        game.takeCard(player0);
        assertThat(player0.cards).isEqualTo(cards.subList(0, 2));
        game.result = GameResult.WIN;
        game.takeCard(player0);
        assertThat(player0.cards).isEqualTo(cards.subList(0, 2));

        game.deck.clear();
        game.result = null;
        game.takeCard(player0);
        assertThat(game.result).isEqualTo(GameResult.LOSS);
    }

    @Test
    void testStartNextTurn() {
        var game = newGame();
        checkGameNotStartedError(game::startNextTurn);
        game.started = true;
        assertThat(game.turn).isEqualTo(0);

        game.events.clear();
        game.startNextTurn();
        assertThat(game.turn).isEqualTo(1);
        assertThat(game.events).containsExactly(
                new TurnStartedEvent(game, 1)
        );

        game.events.clear();
        game.startNextTurn();
        assertThat(game.turn).isEqualTo(2);
        assertThat(game.events).containsExactly(
                new TurnStartedEvent(game, 2)
        );

        game.result = GameResult.WIN;
        checkGameFinishedError(game::startNextTurn);
    }

    @Test
    void testGetCurrentPlayer() {
        var game = newGame();
        var players = game.players;
        game.turn = 1;
        assertThat(game.currentPlayer()).isEqualTo(players.get(0));
        game.turn = 2;
        assertThat(game.currentPlayer()).isEqualTo(players.get(1));
        game.turn = 1;
        assertThat(game.currentPlayer()).isEqualTo(players.get(0));
    }

    @Test
    void testCheckNotStarted() {
        var game = newGame();
        game.checkNotStarted();

        game.started = true;
        checkGameStartedError(game::start);
    }

    @Test
    void testCheckActive() {
        var game = newGame();
        checkGameNotStartedError(game::checkActive);

        game.started = true;
        game.checkActive();

        game.result = GameResult.WIN;
        checkGameFinishedError(game::checkActive);
    }

    @Test
    void testCheckCurrentPlayer() {
        var game = newGame();

        game.turn = 1;
        checkCurrentPlayerError(game, 0, false);
        checkCurrentPlayerError(game, 1, true);

        game.turn = 2;
        checkCurrentPlayerError(game, 0, true);
        checkCurrentPlayerError(game, 1, false);
    }

    @Test
    void testTableCards() {
        var game = newGame();
        assertThat(game.tableCards()).isEmpty();

        game.start();
        assertThat(game.tableCards()).containsExactly(
                new Card(WHITE, 0),
                new Card(RED, 0),
                new Card(GREEN, 0),
                new Card(BLUE, 0),
                new Card(YELLOW, 0)
        );

        game.table.get(RED).add(new Card(RED, 1));
        game.table.get(BLUE).add(new Card(Color.BLUE, 3));
        assertThat(game.tableCards()).containsExactly(
                new Card(WHITE, 0),
                new Card(RED, 1),
                new Card(GREEN, 0),
                new Card(BLUE, 3),
                new Card(YELLOW, 0)
        );
    }

    void checkCurrentPlayerError(Game game, int playerIndex, boolean error) {
        var player = game.players.get(playerIndex);
        if (error) {
            assertThatThrownBy(() -> game.checkCurrentPlayer(player))
                    .isInstanceOfSatisfying(GameException.class, e -> {
                        assertThat(e.getCode()).isEqualTo("player_not_current");
                        assertThat(e.getArguments()).containsExactly(game.currentPlayer());
                    });
        } else {
            game.checkCurrentPlayer(player);
        }
    }

    void checkGameNotStartedError(ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(GameException.class)
                .hasMessage("game_not_started");
    }

    void checkGameStartedError(ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(GameException.class)
                .hasMessage("game_started");
    }

    void checkGameFinishedError(ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(GameException.class)
                .hasMessage("game_finished");
    }

    void checkNotEnoughPlayersError(ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(GameException.class)
                .hasMessage("not_enough_players");
    }

    void checkCardIndexError(ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(GameException.class)
                .hasMessage("invalid_card_index");
    }

    void checkGame(Consumer<Game> action) {
        var game = newGame();
        game.start();
        action.accept(game);
    }

    Game newGame() {
        var cards = new ArrayList<Card>();
        for (Color color : Color.valueList) {
            for (int value = 1; value <= MAX_CARD_VALUE; value++) {
                cards.add(new Card(color, value));
            }
        }
        var game = new Game();
        game.deck.addAll(cards.reversed());
        game.addPlayer(new Player("p0"));
        game.addPlayer(new Player("p1"));
        return game;
    }

}