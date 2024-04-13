package io.github.daring2.hanabi.model;

import io.github.daring2.hanabi.model.event.*;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static io.github.daring2.hanabi.model.Color.*;
import static io.github.daring2.hanabi.model.GameTestUtils.*;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                new AddPlayerEvent(game, game.players.get(0)),
                new AddPlayerEvent(game, game.players.get(1)),
                new StartGameEvent(game),
                new StartTurnEvent(game, 1)
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
        assertThat(game.lastTurn).isEqualTo(-1);

        checkGameStartedError(game::start);
    }

    @Test
    void testIsActive() {
        var game = newGame();
        assertThat(game.isActive()).isFalse();
        game.started = true;
        assertThat(game.isActive()).isTrue();
        game.result = GameResult.CANCEL;
        assertThat(game.isActive()).isFalse();
    }

    @Test
    void testIsFinished() {
        var game = newGame();
        assertThat(game.isFinished()).isFalse();
        game.result = GameResult.CANCEL;
        assertThat(game.isFinished()).isTrue();
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
                new FinishGameEvent(game, GameResult.CANCEL, 0)
        );
    }

    @Test
    void testCalculateResultScore() {
        var game = newGame();
        game.start();
        assertThat(game.calculateResultScore()).isEqualTo(0);
        game.addCardToTable(new Card(WHITE, 1));
        assertThat(game.calculateResultScore()).isEqualTo(1);
        game.addCardToTable(new Card(RED, 2));
        assertThat(game.calculateResultScore()).isEqualTo(3);
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
            assertThat(event).isEqualTo(new AddPlayerEvent(game, player));
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
        assertThat(game.events).containsExactly(new RemovePlayerEvent(game, player1));

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
                new RemovePlayerEvent(game, player1),
                new FinishGameEvent(game, GameResult.CANCEL, 0)
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
    void testGetInitPlayerCardsCount() {
        var game = newGame();
        assertThat(game.getInitPlayerCardsCount()).isEqualTo(5);
        game.addPlayer(new Player("p2"));
        assertThat(game.getInitPlayerCardsCount()).isEqualTo(5);
        game.addPlayer(new Player("p3"));
        assertThat(game.getInitPlayerCardsCount()).isEqualTo(4);
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
                new StartTurnEvent(game, 2)
        );

        var player1 = game.players.get(1);
        game.discardCard(player1, 0);
        assertThat(player1.cards).map(Card::value)
                .containsExactly(3, 5, 7, 9, 11);
        assertThat(game.discard).map(Card::value)
                .containsExactly(0, 1);
    }

    @Test
    void testLaunchFireworks() {
        var game = newGame();
        var player0 = game.players.get(0);
        checkGameNotStartedError(() -> game.launchFireworks(player0));

        game.start();
        assertThatThrownBy(() -> game.launchFireworks(player0))
                .isInstanceOf(GameException.class)
                .hasMessage("deck_not_empty");

        game.events.clear();
        game.deck.clear();
        assertThat(game.result).isNull();
        game.launchFireworks(player0);
        assertThat(game.result).isEqualTo(GameResult.LAUNCH);
        assertThat(game.events).containsExactly(
                new FinishGameEvent(game, GameResult.LAUNCH, 0)
        );
    }

    @Test
    void testAddCardToTable() {
        var cards = rangeClosed(0, 5)
                .mapToObj(i -> new Card(WHITE, i))
                .toList();
        checkGameStart(game -> {
            var card1 = cards.get(1);
            game.events.clear();
            game.addCardToTable(card1);
            assertThat(game.table.get(WHITE)).isEqualTo(cards.subList(0, 2));
            assertThat(game.fireworks).isEqualTo(0);
            assertThat(game.blueTokens).isEqualTo(8);
            assertThat(game.result).isNull();
            assertThat(game.events).containsExactly(
                    new AddCardToTableEvent(game, card1)
            );

            var card2 = cards.get(2);
            game.events.clear();
            game.addCardToTable(card2);
            assertThat(game.table.get(WHITE)).isEqualTo(cards.subList(0, 3));
            assertThat(game.fireworks).isEqualTo(0);
            assertThat(game.blueTokens).isEqualTo(8);
            assertThat(game.result).isNull();
            assertThat(game.events).containsExactly(
                    new AddCardToTableEvent(game, card2)
            );
        });
        checkGameStart(game -> {
            var card5 = cards.get(5);
            game.events.clear();
            game.addCardToTable(card5);
            assertThat(game.table.get(WHITE)).containsExactly(cards.get(0), card5);
            assertThat(game.fireworks).isEqualTo(1);
            assertThat(game.blueTokens).isEqualTo(8);
            assertThat(game.result).isNull();
            assertThat(game.events).containsExactly(
                    new AddCardToTableEvent(game, card5),
                    new CreateFireworkEvent(game, card5)
            );
        });
        checkGameStart(game -> {
            game.events.clear();
            game.fireworks = 4;
            game.addCardToTable(cards.get(5));
            assertThat(game.table.get(WHITE)).containsExactly(cards.get(0), cards.get(5));
            assertThat(game.fireworks).isEqualTo(5);
            assertThat(game.blueTokens).isEqualTo(8);
            assertThat(game.result).isEqualTo(GameResult.LAUNCH);
        });

    }

    @Test
    void testAddRedToken() {
        var game = newGame();
        assertThat(game.redTokens).isEqualTo(0);
        assertThat(game.result).isNull();

        game.events.clear();
        game.addRedToken();
        assertThat(game.redTokens).isEqualTo(1);
        assertThat(game.result).isNull();
        assertThat(game.events).containsExactly(
                new AddRedTokenEvent(game, 1)
        );

        game.events.clear();
        game.addRedToken();
        game.addRedToken();
        assertThat(game.redTokens).isEqualTo(3);
        assertThat(game.result).isEqualTo(GameResult.LOSS);
        assertThat(game.events).containsExactly(
                new AddRedTokenEvent(game, 2),
                new AddRedTokenEvent(game, 3),
                new FinishGameEvent(game, GameResult.LOSS, 0)
        );
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
        game.result = GameResult.LAUNCH;
        game.takeCard(player0);
        assertThat(player0.cards).isEqualTo(cards.subList(0, 2));

        player0.cards.clear();
        game.deck.clear();
        game.result = null;
        game.takeCard(player0);
        assertThat(player0.cards).isEmpty();
    }

    @Test
    void testTakeLastCard() {
        var game = newGame();
        game.settings.lastTurnOnEmptyDeck = true;
        var cards = rangeClosed(1, 2)
                .mapToObj(i -> new Card(WHITE, i))
                .toList();
        game.setDeck(cards.reversed());
        var player0 = game.players.get(0);
        player0.cards.clear();

        game.events.clear();
        game.turn = 10;
        game.takeCard(player0);
        assertThat(player0.cards).isEqualTo(cards.subList(0, 1));
        assertThat(game.lastTurn).isEqualTo(-1);
        assertThat(game.events).isEmpty();

        game.takeCard(player0);
        assertThat(player0.cards).isEqualTo(cards.subList(0, 2));
        assertThat(game.lastTurn).isEqualTo(12);
        assertThat(game.events).containsExactly(
                new DeckEmptyEvent(game, true)
        );

        game.events.clear();
        game.takeCard(player0);
        assertThat(player0.cards).isEqualTo(cards.subList(0, 2));
        assertThat(game.lastTurn).isEqualTo(12);
        assertThat(game.events).isEmpty();
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
        assertThat(game.result).isNull();
        assertThat(game.events).containsExactly(
                new StartTurnEvent(game, 1)
        );

        game.events.clear();
        game.startNextTurn();
        assertThat(game.turn).isEqualTo(2);
        assertThat(game.result).isNull();
        assertThat(game.events).containsExactly(
                new StartTurnEvent(game, 2)
        );

        game.lastTurn = 2;
        game.startNextTurn();
        assertThat(game.turn).isEqualTo(2);
        assertThat(game.result).isEqualTo(GameResult.LAUNCH);

        game.result = GameResult.LAUNCH;
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
        players.clear();
        assertThat(game.currentPlayer()).isEqualTo(null);
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

        game.result = GameResult.LAUNCH;
        checkGameFinishedError(game::checkActive);
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


}