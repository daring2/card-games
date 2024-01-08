package io.github.daring2.hanabi.model;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

import static io.github.daring2.hanabi.model.Color.WHITE;
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
        checkPlayersCountError(game::start, 0);

        game.addPlayer(new Player("p0"));
        checkPlayersCountError(game::start, 1);

        game.addPlayer(new Player("p1"));
        game.start();
        for (Color color : Color.values()) {
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
    void testSetDeck() {
        //TODO implement
    }

    @Test
    void testAddPlayer() {
        //TODO implement
    }

    @Test
    void testValidPlayersCount() {
        var game = newGame();
        game.players.clear();
        assertThat(game.isValidPlayersCount()).isEqualTo(false);
        game.addPlayer(new Player("p0"));
        assertThat(game.isValidPlayersCount()).isEqualTo(false);
        game.addPlayer(new Player("p1"));
        assertThat(game.isValidPlayersCount()).isEqualTo(true);
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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("All blue tokens are in the game");
        assertThat(game.discard).isEmpty();

        game.blueTokens = 1;
        game.started = false;
        checkGameNotStartedError(() -> game.discardCard(player0, 0));
        assertThat(game.discard).isEmpty();

        game.started = true;
        game.discardCard(player0, 0);
        assertThat(player0.cards).map(Card::value)
                .containsExactly(2, 4, 6, 8, 10);
        assertThat(game.discard).map(Card::value)
                .containsExactly(0);

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
        });
        checkGame(it -> {
            var game = spy(it);
            var player0 = game.players.get(0);
            game.playCard(player0, 0); // W-1
            assertThat(player0.cards).hasSize(5)
                    .first().isEqualTo(new Card(WHITE, 3));
            verify(game, times(1))
                    .addCardToTable(new Card(WHITE, 1));
            verify(game, times(0)).discardRedToken();
            assertThat(game.redTokens).isEqualTo(MAX_RED_TOKENS);
            assertThat(game.discard).isEmpty();
            verify(game, times(1)).takeCard(player0);
        });
        checkGame(it -> {
            var game = spy(it);
            var player0 = game.players.get(0);
            game.playCard(player0, 1); // W-3
            assertThat(player0.cards).hasSize(5)
                    .first().isEqualTo(new Card(WHITE, 1));
            verify(game, times(0))
                    .addCardToTable(new Card(WHITE, 1));
            verify(game, times(1)).discardRedToken();
            assertThat(game.discard).containsExactly(new Card(WHITE, 3));
            assertThat(game.redTokens).isEqualTo(MAX_RED_TOKENS - 1);
            verify(game, times(1)).takeCard(player0);
        });
    }

    @Test
    void testShareInfo() {
        var game = newGame();
        var player0 = game.players.get(0);
        var player1 = game.players.get(1);
        checkGameNotStartedError(() -> game.shareInfo(player0, player1, new CardInfo(WHITE)));

        game.start();
        assertThatThrownBy(() -> game.shareInfo(player0, player1, new CardInfo(WHITE, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only one property can be shared");

        game.blueTokens = 0;
        assertThatThrownBy(() -> game.shareInfo(player0, player1, new CardInfo(WHITE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No blue tokens are available");

        game.blueTokens = 3;
        game.shareInfo(player0, player1, new CardInfo(WHITE));
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
            game.addCardToTable(cards.get(1));
            assertThat(game.table.get(WHITE)).isEqualTo(cards.subList(0, 2));
            assertThat(game.fireworks).isEqualTo(0);
            assertThat(game.blueTokens).isEqualTo(MAX_BLUE_TOKENS);
            assertThat(game.result).isNull();

            game.addCardToTable(cards.get(2));
            assertThat(game.table.get(WHITE)).isEqualTo(cards.subList(0, 3));
            assertThat(game.fireworks).isEqualTo(0);
            assertThat(game.blueTokens).isEqualTo(MAX_BLUE_TOKENS);
            assertThat(game.result).isNull();
        });
        checkGame(game -> {
            game.addCardToTable(cards.get(5));
            assertThat(game.table.get(WHITE))
                    .containsExactly(cards.get(0), cards.get(5));
            assertThat(game.fireworks).isEqualTo(1);
            assertThat(game.blueTokens).isEqualTo(MAX_BLUE_TOKENS);
            assertThat(game.result).isNull();
        });
        checkGame(game -> {
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
    void testDiscardRedToken() {
        var game = newGame();
        assertThat(game.redTokens).isEqualTo(3);
        assertThat(game.result).isNull();

        game.discardRedToken();
        assertThat(game.redTokens).isEqualTo(2);
        assertThat(game.result).isNull();

        game.discardRedToken();
        game.discardRedToken();
        assertThat(game.redTokens).isEqualTo(0);
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
        game.startNextTurn();
        assertThat(game.turn).isEqualTo(1);
        game.startNextTurn();
        assertThat(game.turn).isEqualTo(2);
        game.result = GameResult.WIN;
        checkGameFinishedError(game::startNextTurn);
    }

    @Test
    void testGetCurrentPlayer() {
        var game = newGame();
        var players = game.players;
        game.turn = 1;
        assertThat(game.getCurrentPlayer()).isEqualTo(players.get(0));
        game.turn = 2;
        assertThat(game.getCurrentPlayer()).isEqualTo(players.get(1));
        game.turn = 1;
        assertThat(game.getCurrentPlayer()).isEqualTo(players.get(0));
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

    void checkCurrentPlayerError(Game game, int playerIndex, boolean error) {
        var player = game.players.get(playerIndex);
        if (error) {
            assertThatThrownBy(() -> game.checkCurrentPlayer(player))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Player '" + player + "' is not current");
        } else {
            game.checkCurrentPlayer(player);
        }
    }

    void checkGameNotStartedError(ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Game is not started");
    }

    void checkGameStartedError(ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Game is started");
    }

    void checkGameFinishedError(ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Game is finished");
    }

    void checkPlayersCountError(ThrowingCallable action, int count) {
        assertThatThrownBy(action)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid players count: " + count);
    }

    void checkGame(Consumer<Game> action) {
        var game = newGame();
        game.start();
        action.accept(game);
    }

    Game newGame() {
        var cards = new ArrayList<Card>();
        for (Color color : Color.values()) {
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